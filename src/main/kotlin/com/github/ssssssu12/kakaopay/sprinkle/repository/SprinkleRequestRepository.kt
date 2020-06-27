package com.github.ssssssu12.kakaopay.sprinkle.repository

import com.github.ssssssu12.kakaopay.sprinkle.entity.QSprinkleRequests
import com.github.ssssssu12.kakaopay.sprinkle.entity.SprinkleRequests
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import javax.persistence.EntityManager

@Repository
class SprinkleRequestRepository(
		entityManager: EntityManager
) : SimpleJpaRepository<SprinkleRequests, Long>(SprinkleRequests::class.java, entityManager) {
	private val query = JPAQueryFactory(entityManager)
	private val sprinkleRequests = QSprinkleRequests.sprinkleRequests

	fun findSprinkleRequest(userId: Long, token: String) = query
			.selectFrom(sprinkleRequests)
			.where(
					sprinkleRequests.userId.eq(userId),
					sprinkleRequests.token.eq(token),
					sprinkleRequests.requestedAt.after(LocalDateTime.now().minusDays(8L))
			)
			.fetchOne()

	fun findByToken(token: String) = query
			.selectFrom(sprinkleRequests)
			.where(sprinkleRequests.token.eq(token))
			.fetchOne()
}