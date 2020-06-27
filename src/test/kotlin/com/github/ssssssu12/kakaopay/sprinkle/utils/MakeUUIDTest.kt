package com.github.ssssssu12.kakaopay.sprinkle.utils

import com.github.ssssssu12.kakaopay.sprinkle.utils.RandomUtils.makeUUID
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test

class MakeUUIDTest {

	@Test
	fun `UUID 테스트`() {
		for (index: Long in 0L..(64.pow(3))) {
			then(makeUUID(index, 3)).hasSize(3)
		}
	}
}