package com.github.ssssssu12.kakaopay.sprinkle.exception

import java.time.LocalDateTime

open class ErrorResponse(
		val code: String,
		val message: String,
		val timestamp: String = LocalDateTime.now().toString()
)