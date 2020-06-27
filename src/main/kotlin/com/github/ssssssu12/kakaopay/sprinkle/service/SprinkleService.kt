package com.github.ssssssu12.kakaopay.sprinkle.service

import com.github.ssssssu12.kakaopay.sprinkle.dto.SprinkleDTO
import com.github.ssssssu12.kakaopay.sprinkle.entity.SprinkleDistributionInfos
import com.github.ssssssu12.kakaopay.sprinkle.entity.SprinkleRequests
import com.github.ssssssu12.kakaopay.sprinkle.enums.SprinkleDistributionStatus
import com.github.ssssssu12.kakaopay.sprinkle.exception.BadRequestException
import com.github.ssssssu12.kakaopay.sprinkle.exception.ErrorType
import com.github.ssssssu12.kakaopay.sprinkle.exception.NotFoundException
import com.github.ssssssu12.kakaopay.sprinkle.repository.SprinkleDistributionInfoRepository
import com.github.ssssssu12.kakaopay.sprinkle.repository.SprinkleRequestRepository
import com.github.ssssssu12.kakaopay.sprinkle.support.Locker
import com.github.ssssssu12.kakaopay.sprinkle.utils.RandomUtils.makeUUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class SprinkleService(
		private val sprinkleRequestRepository: SprinkleRequestRepository,
		private val sprinkleDistributionInfoRepository: SprinkleDistributionInfoRepository,
		private val receiveLocker: Locker
) {

	/**
	 * 조회
	 * @param userId 조회 요청한 유저 ID
	 * @param token 뿌리기 토큰
	 * @return [token] 에 해당하는 뿌리기 현재 정보
	 */
	fun getSprinkle(userId: Long, token: String): SprinkleDTO.Get.Response {
		val sprinkleRequest = sprinkleRequestRepository.findSprinkleRequest(userId, token)
				?: throw NotFoundException()
		val sprinkleReceivedList = sprinkleDistributionInfoRepository.findReceivedList(sprinkleRequest.seq)
		return sprinkleRequest.toDTO(sprinkleReceivedList)
	}

	/**
	 * 뿌리기
	 * @param userId 뿌리기 요청한 유저 ID
	 * @param roomId 뿌리기 요청한 룸 ID
	 * @param request 뿌리기 요청 정보
	 * @return 뿌리기 토큰
	 */
	fun sprinkle(userId: Long, roomId: String, request: SprinkleDTO.Sprinkle.Request): SprinkleDTO.Sprinkle.Response {
		val sprinkle = sprinkleRequestRepository.save(SprinkleRequests(
				token = makeUUID(System.nanoTime(), 3),
				userId = userId,
				roomId = roomId,
				amount = request.amount,
				divide = request.divide,
				requestedAt = LocalDateTime.now()
		))
		val dividedAmountList = divideSprinkleAmount(sprinkle.amount, sprinkle.divide)
		for (index: Int in 0 until request.divide) {
			sprinkleDistributionInfoRepository.save(SprinkleDistributionInfos(
					sprinkleRequestSeq = sprinkle.seq,
					dividedAmount = dividedAmountList[index],
					status = SprinkleDistributionStatus.NOT_RECEIVED
			))
		}
		return SprinkleDTO.Sprinkle.Response(
				token = sprinkle.token
		)
	}

	/**
	 * 받기
	 * @param userId 받기 요청한 유저 ID
	 * @param roomId 받기 요청한 룸 ID
	 * @param request 받기 요청정보
	 * @return 받은 금액
	 */
	fun receive(userId: Long, roomId: String, request: SprinkleDTO.Receive.Request): SprinkleDTO.Receive.Response {
		val sprinkleRequestSeq = checkReceive(userId, roomId, request.token)
		val receivedAmount = receiveLocker.run(request.token) {
			processReceive(userId, sprinkleRequestSeq)
		}!!
		return SprinkleDTO.Receive.Response(
				amount = receivedAmount
		)
	}

	private fun checkReceive(userId: Long, roomId: String, token: String): Long {
		val sprinkleRequest = sprinkleRequestRepository.findByToken(token) ?: throw BadRequestException()

		// 요청자 체크
		if (sprinkleRequest.userId == userId) {
			throw BadRequestException(ErrorType.SAME_SPRINKLE_USER)
		}

		// 대화방 체크
		if (sprinkleRequest.roomId != roomId) {
			throw BadRequestException(ErrorType.NOT_SAME_ROOM)
		}

		// 뿌리기 타임아웃 체크
		if (sprinkleRequest.requestedAt.isBefore(LocalDateTime.now().minusMinutes(10L))) {
			throw BadRequestException(ErrorType.SPRINKLE_REQUEST_TIMEOUT)
		}

		val distributionInfoList = sprinkleDistributionInfoRepository.findAllByRequestSeq(sprinkleRequest.seq)

		// 이미 받았는지 체크
		distributionInfoList.find {
			it.receivedUserId == userId
		}?.run {
			throw BadRequestException(ErrorType.ALREADY_RECEIVED)
		}

		return sprinkleRequest.seq
	}

	private fun processReceive(userId: Long, sprinkleRequestSeq: Long): Long {
		// 미할당 분배 건 조회
		val distributionInfo = sprinkleDistributionInfoRepository.findNotReceived(sprinkleRequestSeq)
				?: throw BadRequestException(ErrorType.ALREADY_FINISHED)

		// 할당
		sprinkleDistributionInfoRepository.updateReceivedInfo(distributionInfo.seq, userId)

		return distributionInfo.dividedAmount
	}

	/**
	 * 뿌리기 금액 분배
	 */
	private fun divideSprinkleAmount(amount: Long, divideCount: Int): List<Long> {
		if (divideCount == 1) {
			return listOf(amount)
		}

		// 모든 인원이 최소 1원씩은 받아야 한다.
		val dividedAmountList = List(divideCount) { 1L }.toMutableList()

		var totalDividedAmount = 0L
		var availableAmount: Long
		for ((index, _) in dividedAmountList.withIndex()) {
			// 분배가능금액 = 뿌린금액 - 인원수 - 누적분배금액
			availableAmount = (amount - divideCount) - totalDividedAmount

			// 이미 분배 완료된 경우
			if (availableAmount == 0L) {
				break
			}

			// 마지막 차례는 남은금액 전부 분배받아야 한다.
			if (index == dividedAmountList.lastIndex) {
				dividedAmountList[index] += availableAmount
				continue
			}

			// 0 부터 분배가능금액 사이의 랜덤한 금액
			val dividedAmount = (0..availableAmount).random()
			dividedAmountList[index] += dividedAmount

			totalDividedAmount += dividedAmount
		}

		// 섞어준다.
		dividedAmountList.shuffle()
		return dividedAmountList
	}
}