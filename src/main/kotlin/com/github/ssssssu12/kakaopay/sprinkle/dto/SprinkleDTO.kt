package com.github.ssssssu12.kakaopay.sprinkle.dto

import java.time.LocalDateTime

class SprinkleDTO {
	class Sprinkle {
		data class Request(
				val amount: Long,
				val divide: Int
		)
		data class Response(
				val token: String
		)
	}
	class Receive {
		data class Request(
				val token: String
		)
		data class Response(
				val amount: Long
		)
	}
	class Get {
		data class Response(
				val sprinkledAt: LocalDateTime,
				val amount: Long,
				val totalReceivedAmount: Long,
				val receivedList: List<DistributionInfo>
		) {
			data class DistributionInfo(
					val receivedAmount: Long,
					val receivedUserId: Long
			)
		}
	}
}