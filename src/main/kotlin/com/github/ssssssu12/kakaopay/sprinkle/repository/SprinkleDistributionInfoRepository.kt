package com.github.ssssssu12.kakaopay.sprinkle.repository

import com.github.ssssssu12.kakaopay.sprinkle.entity.QSprinkleDistributionInfos
import com.github.ssssssu12.kakaopay.sprinkle.entity.SprinkleDistributionInfos
import com.github.ssssssu12.kakaopay.sprinkle.enums.SprinkleDistributionStatus
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import javax.persistence.EntityManager

@Repository
class SprinkleDistributionInfoRepository(
		entityManager: EntityManager
) : SimpleJpaRepository<SprinkleDistributionInfos, Long>(SprinkleDistributionInfos::class.java, entityManager) {
	private val query = JPAQueryFactory(entityManager)
	private val sprinkleDistributionInfos = QSprinkleDistributionInfos.sprinkleDistributionInfos

	fun findReceivedList(sprinkleRequestSeq: Long) = query
			.selectFrom(sprinkleDistributionInfos)
			.where(
					sprinkleDistributionInfos.sprinkleRequestSeq.eq(sprinkleRequestSeq),
					sprinkleDistributionInfos.status.eq(SprinkleDistributionStatus.RECEIVED)
			)
			.fetch()

	fun findAllByRequestSeq(sprinkleRequestSeq: Long) = query
			.selectFrom(sprinkleDistributionInfos)
			.where(sprinkleDistributionInfos.sprinkleRequestSeq.eq(sprinkleRequestSeq))
			.fetch()

	fun findNotReceived(sprinkleRequestSeq: Long) = query
			.selectFrom(sprinkleDistributionInfos)
			.where(
					sprinkleDistributionInfos.sprinkleRequestSeq.eq(sprinkleRequestSeq),
					sprinkleDistributionInfos.status.eq(SprinkleDistributionStatus.NOT_RECEIVED)
			)
			.orderBy(sprinkleDistributionInfos.seq.asc())
			.limit(1L)
			.fetchOne()

	fun updateReceivedInfo(seq: Long, receivedUserId: Long) = query
			.update(sprinkleDistributionInfos)
			.set(sprinkleDistributionInfos.receivedUserId, receivedUserId)
			.set(sprinkleDistributionInfos.status, SprinkleDistributionStatus.RECEIVED)
			.set(sprinkleDistributionInfos.receivedAt, LocalDateTime.now())
			.where(sprinkleDistributionInfos.seq.eq(seq))
			.execute()
}