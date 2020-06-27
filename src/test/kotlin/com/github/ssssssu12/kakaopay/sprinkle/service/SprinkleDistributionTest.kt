package com.github.ssssssu12.kakaopay.sprinkle.service

import mu.KLogging
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestConstructor
import org.springframework.test.util.ReflectionTestUtils

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class SprinkleDistributionTest(
		private val sprinkleService: SprinkleService
) {
	companion object : KLogging()

	@Test
	fun `뿌린 금액에 대한 분배 테스트 - 1명`() {
		// given
		val amount = 100L
		val divide = 1

		// when
		val dividedAmountList = ReflectionTestUtils.invokeMethod<List<Long>>(
				sprinkleService, "divideSprinkleAmount", amount, divide
		)!!

		// then
		logger.info(dividedAmountList.toString())
		then(dividedAmountList.size).isEqualTo(divide)
		then(dividedAmountList.sum()).isEqualTo(amount)
		then(dividedAmountList.min()).isGreaterThanOrEqualTo(1L)
	}

	@Test
	fun `뿌린 금액에 대한 분배 테스트 - 적은 수로 분배`() {
		// given
		val amount = 100L
		val divide = 8

		// when
		val dividedAmountList = ReflectionTestUtils.invokeMethod<List<Long>>(
				sprinkleService, "divideSprinkleAmount", amount, divide
		)!!

		// then
		logger.info(dividedAmountList.toString())
		then(dividedAmountList.size).isEqualTo(divide)
		then(dividedAmountList.sum()).isEqualTo(amount)
		then(dividedAmountList.min()).isGreaterThanOrEqualTo(1L)
	}

	@Test
	fun `뿌린 금액에 대한 분배 테스트 - 많은 수로 분배`() {
		// given
		val amount = 100L
		val divide = 88

		// when
		val dividedAmountList = ReflectionTestUtils.invokeMethod<List<Long>>(
				sprinkleService, "divideSprinkleAmount", amount, divide
		)!!

		// then
		logger.info(dividedAmountList.toString())
		then(dividedAmountList.size).isEqualTo(divide)
		then(dividedAmountList.sum()).isEqualTo(amount)
		then(dividedAmountList.min()).isGreaterThanOrEqualTo(1L)
	}

}