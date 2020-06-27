package com.github.ssssssu12.kakaopay.sprinkle.service

import com.github.ssssssu12.kakaopay.sprinkle.dto.SprinkleDTO
import com.github.ssssssu12.kakaopay.sprinkle.entity.SprinkleDistributionInfos
import com.github.ssssssu12.kakaopay.sprinkle.entity.SprinkleRequests
import com.github.ssssssu12.kakaopay.sprinkle.enums.SprinkleDistributionStatus
import com.github.ssssssu12.kakaopay.sprinkle.exception.BadRequestException
import com.github.ssssssu12.kakaopay.sprinkle.repository.SprinkleDistributionInfoRepository
import com.github.ssssssu12.kakaopay.sprinkle.repository.SprinkleRequestRepository
import org.assertj.core.api.BDDAssertions.then
import org.assertj.core.api.BDDAssertions.thenThrownBy
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestConstructor
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import javax.persistence.EntityManager

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Transactional
class SprinkleReceiveTest(
		private val sprinkleService: SprinkleService,
		private val sprinkleRequestRepository: SprinkleRequestRepository,
		private val sprinkleDistributionInfoRepository: SprinkleDistributionInfoRepository,
		private val entityManager: EntityManager
) {
	@Test
	fun `받기 테스트 - 요청자와 동일`() {
		// given
		val sprinkleRequest = SprinkleRequests(
				token = "Aa1",
				userId = 1000L,
				roomId = "room001",
				amount = 100L,
				divide = 3,
				requestedAt = LocalDateTime.now()
		)
		sprinkleRequestRepository.save(sprinkleRequest)

		val userId = sprinkleRequest.userId
		val roomId = sprinkleRequest.roomId
		val request = SprinkleDTO.Receive.Request(
				token = sprinkleRequest.token
		)

		// when then
		thenThrownBy { sprinkleService.receive(userId, roomId, request) }
				.isExactlyInstanceOf(BadRequestException::class.java)
				.hasMessageContaining("자신이 뿌리기 한 건은 자신이 받을 수 없습니다.")
	}

	@Test
	fun `받기 테스트 - 대화방 오류`() {
		// given
		val sprinkleRequest = SprinkleRequests(
				token = "Aa1",
				userId = 1000L,
				roomId = "room001",
				amount = 100L,
				divide = 3,
				requestedAt = LocalDateTime.now()
		)
		sprinkleRequestRepository.save(sprinkleRequest)

		val userId = 1001L
		val roomId = "room015"
		val request = SprinkleDTO.Receive.Request(
				token = sprinkleRequest.token
		)

		// when then
		thenThrownBy { sprinkleService.receive(userId, roomId, request) }
				.isExactlyInstanceOf(BadRequestException::class.java)
				.hasMessageContaining("대화방이 동일하지 않습니다.")
	}

	@Test
	fun `받기 테스트 - 뿌리기 타임아웃`() {
		// given
		val sprinkleRequest = SprinkleRequests(
				token = "Aa1",
				userId = 1000L,
				roomId = "room001",
				amount = 100L,
				divide = 3,
				requestedAt = LocalDateTime.now().minusMinutes(10L)
		)
		sprinkleRequestRepository.save(sprinkleRequest)

		val userId = 1001L
		val roomId = sprinkleRequest.roomId
		val request = SprinkleDTO.Receive.Request(
				token = sprinkleRequest.token
		)

		// when then
		thenThrownBy { sprinkleService.receive(userId, roomId, request) }
				.isExactlyInstanceOf(BadRequestException::class.java)
				.hasMessageContaining("유효기간이 지난 요청입니다.")
	}

	@Test
	fun `받기 테스트 - 이미 받음`() {
		// given
		val sprinkleRequest = SprinkleRequests(
				token = "Aa1",
				userId = 1000L,
				roomId = "room001",
				amount = 100L,
				divide = 3,
				requestedAt = LocalDateTime.now()
		)
		sprinkleRequestRepository.save(sprinkleRequest)

		val userId = 1001L
		val roomId = sprinkleRequest.roomId
		val request = SprinkleDTO.Receive.Request(
				token = sprinkleRequest.token
		)

		val distributionInfo = SprinkleDistributionInfos(
				sprinkleRequestSeq = sprinkleRequest.seq,
				dividedAmount = 30,
				receivedUserId = userId,
				status = SprinkleDistributionStatus.RECEIVED,
				receivedAt = LocalDateTime.now()
		)
		sprinkleDistributionInfoRepository.save(distributionInfo)

		// when then
		thenThrownBy { sprinkleService.receive(userId, roomId, request) }
				.isExactlyInstanceOf(BadRequestException::class.java)
				.hasMessageContaining("이미 받은 이력이 있습니다.")
	}

	@Test
	fun `받기 테스트 - 분배 종료`() {
		// given
		val sprinkleRequest = SprinkleRequests(
				token = "Aa1",
				userId = 1000L,
				roomId = "room001",
				amount = 100L,
				divide = 3,
				requestedAt = LocalDateTime.now()
		)
		sprinkleRequestRepository.save(sprinkleRequest)

		val userId = 1001L
		val roomId = sprinkleRequest.roomId
		val request = SprinkleDTO.Receive.Request(
				token = sprinkleRequest.token
		)

		for (index in 1..sprinkleRequest.divide) {
			val distributionInfo = SprinkleDistributionInfos(
					sprinkleRequestSeq = sprinkleRequest.seq,
					dividedAmount = if (index == 1) sprinkleRequest.amount - 2 else 1,
					receivedUserId = userId + index,
					status = SprinkleDistributionStatus.RECEIVED,
					receivedAt = LocalDateTime.now()
			)
			sprinkleDistributionInfoRepository.save(distributionInfo)
		}

		// when then
		thenThrownBy { sprinkleService.receive(userId, roomId, request) }
				.isExactlyInstanceOf(BadRequestException::class.java)
				.hasMessageContaining("이미 종료되었습니다.")
	}

	@Test
	fun `받기 테스트 - 정상`() {
		// given
		val sprinkleRequest = SprinkleRequests(
				token = "Aa1",
				userId = 1000L,
				roomId = "room001",
				amount = 100L,
				divide = 3,
				requestedAt = LocalDateTime.now()
		)
		sprinkleRequestRepository.save(sprinkleRequest)

		val userId = 1001L
		val roomId = sprinkleRequest.roomId
		val request = SprinkleDTO.Receive.Request(
				token = sprinkleRequest.token
		)

		for (index in 1..sprinkleRequest.divide) {
			val distributionInfo = SprinkleDistributionInfos(
					sprinkleRequestSeq = sprinkleRequest.seq,
					dividedAmount = if (index == 1) sprinkleRequest.amount - 2 else 1,
					status = SprinkleDistributionStatus.NOT_RECEIVED
			)
			sprinkleDistributionInfoRepository.save(distributionInfo)
		}

		// when
		val response = sprinkleService.receive(userId, roomId, request)

		// then
		then(response.amount).isEqualTo(sprinkleRequest.amount - 2)
		entityManager.clear()
		val distributionInfo = sprinkleDistributionInfoRepository.findById(1L).get()
		then(distributionInfo.receivedUserId).isEqualTo(userId)
		then(distributionInfo.status).isEqualTo(SprinkleDistributionStatus.RECEIVED)
		then(distributionInfo.receivedAt).isBetween(LocalDateTime.now().minusSeconds(5L), LocalDateTime.now())
	}

}