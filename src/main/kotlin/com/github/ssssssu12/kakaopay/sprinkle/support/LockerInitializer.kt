package com.github.ssssssu12.kakaopay.sprinkle.support

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory

@Configuration
class LockerInitializer(
		private val redisConnectionFactory: RedisConnectionFactory
) {
	@Bean
	fun receiveLocker() = Locker(
			registryKey = "receive",
			redisConnectionFactory = redisConnectionFactory
	)
}