package com.github.ssssssu12.kakaopay.sprinkle.entity

import com.github.ssssssu12.kakaopay.sprinkle.dto.SprinkleDTO
import com.github.ssssssu12.kakaopay.sprinkle.enums.SprinkleDistributionStatus
import java.time.LocalDateTime
import javax.persistence.*

@Entity(name = "sprinkle_distribution_infos")
class SprinkleDistributionInfos(
		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		var seq: Long = 0L,

		@Column(name = "sprinkle_request_seq", nullable = false)
		var sprinkleRequestSeq: Long = 0L,

		@Column(name = "divided_amount", nullable = false)
		var dividedAmount: Long = 0L,

		@Column(name = "received_user_id")
		var receivedUserId: Long? = null,

		@Column(name = "status", length = 32, nullable = false)
		@Enumerated(EnumType.STRING)
		var status: SprinkleDistributionStatus = SprinkleDistributionStatus.NOT_RECEIVED,

		@Column(name = "received_at", columnDefinition = "TIMESTAMP")
		var receivedAt: LocalDateTime? = null
) {
	fun toDTO() = SprinkleDTO.Get.Response.DistributionInfo(
			receivedUserId = receivedUserId!!,
			receivedAmount = dividedAmount
	)
}