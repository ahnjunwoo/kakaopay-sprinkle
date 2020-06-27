package com.github.ssssssu12.kakaopay.sprinkle.support

import mu.KLogging
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.integration.redis.util.RedisLockRegistry
import java.util.concurrent.TimeUnit

class Locker(
		private val registryKey: String,
		private val defaultLockWaitingTimeoutSec: Int = 5,
		defaultRedisKeyTtlSec: Int = 30,
		redisConnectionFactory: RedisConnectionFactory
) {
	companion object : KLogging() {
		const val REGISTRY_PREFIX = "locks"
	}

	/**
	 * https://docs.spring.io/spring-integration/api/org/springframework/integration/redis/util/RedisLockRegistry.html
	 * https://docs.spring.io/spring-integration/reference/html/redis.html#redis-lock-registry
	 */
	private val redisLockRegistry = RedisLockRegistry(
			redisConnectionFactory,
			"$REGISTRY_PREFIX:$registryKey",
			defaultRedisKeyTtlSec * 1000L
	)

	fun <T> run(
			key: Any,
			lockWaitingTimeoutSec: Int = defaultLockWaitingTimeoutSec,
			runnable: () -> T?
	): T? {
		var t: T? = null
		val lock = redisLockRegistry.obtain(key.toString())
		if (lock.tryLock(lockWaitingTimeoutSec.toLong(), TimeUnit.SECONDS)) {
			try {
				logger.debug("LockStart - {}:{}", registryKey, key)
				t = runnable()
				logger.debug("LockEnd - {}:{}", registryKey, key)
			} finally {
				logger.debug("Unlock - {}:{}", registryKey, key)
				lock.unlock()
			}
		} else {
			logger.error("LockFailed - {}:{}", registryKey, key)
		}
		return t
	}
}