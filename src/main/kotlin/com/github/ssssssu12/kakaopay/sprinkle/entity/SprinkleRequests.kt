package com.github.ssssssu12.kakaopay.sprinkle.entity

import com.github.ssssssu12.kakaopay.sprinkle.dto.SprinkleDTO
import java.time.LocalDateTime
import javax.persistence.*

@Entity(name = "sprinkle_requests")
class SprinkleRequests(
		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		var seq: Long = 0L,

		@Column(name = "token", length = 3, nullable = false, unique = true)
		var token: String = "",

		@Column(name = "user_id", nullable = false)
		var userId: Long = 0L,

		@Column(name = "room_id", length = 256, nullable = false)
		var roomId: String = "",

		@Column(name = "amount", nullable = false)
		var amount: Long = 0L,

		@Column(name = "divide", nullable = false)
		var divide: Int = 0,

		@Column(name = "requested_at", nullable = false, columnDefinition = "DATETIME")
		var requestedAt: LocalDateTime = LocalDateTime.now()
) {
	fun toDTO(sprinkleReceivedList: List<SprinkleDistributionInfos>) = SprinkleDTO.Get.Response(
			sprinkledAt = this.requestedAt,
			amount = this.amount,
			totalReceivedAmount = sprinkleReceivedList.map { it.dividedAmount }.sum(),
			receivedList = sprinkleReceivedList.map { it.toDTO() }
	)
}