package com.github.ssssssu12.kakaopay.sprinkle.service

import com.github.ssssssu12.kakaopay.sprinkle.entity.SprinkleDistributionInfos
import com.github.ssssssu12.kakaopay.sprinkle.entity.SprinkleRequests
import com.github.ssssssu12.kakaopay.sprinkle.enums.SprinkleDistributionStatus
import com.github.ssssssu12.kakaopay.sprinkle.exception.NotFoundException
import com.github.ssssssu12.kakaopay.sprinkle.repository.SprinkleDistributionInfoRepository
import com.github.ssssssu12.kakaopay.sprinkle.repository.SprinkleRequestRepository
import org.assertj.core.api.BDDAssertions.then
import org.assertj.core.api.BDDAssertions.thenThrownBy
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestConstructor
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Transactional
class SprinkleGetTest(
		private val sprinkleService: SprinkleService,
		private val sprinkleRequestRepository: SprinkleRequestRepository,
		private val sprinkleDistributionInfoRepository: SprinkleDistributionInfoRepository
) {
	@Test
	fun `뿌리기 정보 조회 테스트 - 다른 사람의 뿌리기건 요청`() {
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

		val userId = sprinkleRequest.userId + 1
		val token = sprinkleRequest.token

		// when then
		thenThrownBy { sprinkleService.getSprinkle(userId, token) }
				.isExactlyInstanceOf(NotFoundException::class.java)
	}

	@Test
	fun `뿌리기 정보 조회 테스트 - 잘못된 토큰`() {
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
		val token = "room025"

		// when then
		thenThrownBy { sprinkleService.getSprinkle(userId, token) }
				.isExactlyInstanceOf(NotFoundException::class.java)
	}

	@Test
	fun `뿌리기 정보 조회 테스트 - 7일 이전 건 조회`() {
		// given
		val sprinkleRequest = SprinkleRequests(
				token = "Aa1",
				userId = 1000L,
				roomId = "room001",
				amount = 100L,
				divide = 3,
				requestedAt = LocalDateTime.now().minusDays(8L)
		)
		sprinkleRequestRepository.save(sprinkleRequest)

		val userId = sprinkleRequest.userId
		val token = sprinkleRequest.token

		// when then
		thenThrownBy { sprinkleService.getSprinkle(userId, token) }
				.isExactlyInstanceOf(NotFoundException::class.java)
	}

	@Test
	fun `뿌리기 정보 조회 테스트 - 정상`() {
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

		for (index in 1..sprinkleRequest.divide) {
			val distributionInfo = SprinkleDistributionInfos(
					sprinkleRequestSeq = sprinkleRequest.seq,
					dividedAmount = if (index == 1) sprinkleRequest.amount - 2 else 1,
					receivedUserId = sprinkleRequest.userId + index,
					status = SprinkleDistributionStatus.RECEIVED,
					receivedAt = LocalDateTime.now()
			)
			sprinkleDistributionInfoRepository.save(distributionInfo)
		}

		val userId = sprinkleRequest.userId
		val token = sprinkleRequest.token

		// when
		val response = sprinkleService.getSprinkle(userId, token)

		// then
		then(response.sprinkledAt).isEqualTo(sprinkleRequest.requestedAt)
		then(response.amount).isEqualTo(sprinkleRequest.amount)
		then(response.totalReceivedAmount).isEqualTo(sprinkleRequest.amount)
		then(response.receivedList[0].receivedUserId).isEqualTo(sprinkleRequest.userId + 1)
		then(response.receivedList[0].receivedAmount).isEqualTo(sprinkleRequest.amount - 2)
	}
}