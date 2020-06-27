package com.github.ssssssu12.kakaopay.sprinkle.service

import com.github.ssssssu12.kakaopay.sprinkle.dto.SprinkleDTO
import com.github.ssssssu12.kakaopay.sprinkle.enums.SprinkleDistributionStatus
import com.github.ssssssu12.kakaopay.sprinkle.repository.SprinkleDistributionInfoRepository
import com.github.ssssssu12.kakaopay.sprinkle.repository.SprinkleRequestRepository
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestConstructor
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Transactional
class SprinkleRequestTest(
		private val sprinkleService: SprinkleService,
		private val sprinkleRequestRepository: SprinkleRequestRepository,
		private val sprinkleDistributionInfoRepository: SprinkleDistributionInfoRepository
) {
	@Test
	fun `뿌리기 테스트`() {
		// given
		val userId = 1000L
		val roomId = "room001"
		val request = SprinkleDTO.Sprinkle.Request(
				amount = 100L,
				divide = 3
		)

		// when
		sprinkleService.sprinkle(userId, roomId, request)

		// then
		val sprinkle = sprinkleRequestRepository.findById(1L)
		then(sprinkle).isPresent
		then(sprinkle.get().token).hasSize(3)
		then(sprinkle.get().userId).isEqualTo(userId)
		then(sprinkle.get().roomId).isEqualTo(roomId)
		then(sprinkle.get().amount).isEqualTo(request.amount)
		then(sprinkle.get().divide).isEqualTo(request.divide)
		then(sprinkle.get().requestedAt).isBetween(LocalDateTime.now().minusSeconds(2L), LocalDateTime.now())
		val distributionInfos = sprinkleDistributionInfoRepository.findAll()
		then(distributionInfos).hasSize(request.divide)
		then(distributionInfos.first().status).isEqualTo(SprinkleDistributionStatus.NOT_RECEIVED)
		then(distributionInfos.map { it.dividedAmount }.sum()).isEqualTo(request.amount)
	}
}