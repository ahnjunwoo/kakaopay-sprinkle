package com.github.ssssssu12.kakaopay.sprinkle.exception

import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import javax.servlet.http.HttpServletRequest

@RestControllerAdvice
class GlobalExceptionHandler {
	companion object : KLogging()

	@ExceptionHandler(Exception::class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	fun handleException(request: HttpServletRequest, e: Exception): ErrorResponse {
		logger.error(e) { "[INTERNAL_SERVER_ERROR] [${request.requestURI}] EXCEPTION -> $e" }
		return InternalServerException().toResponse()
	}

	@ExceptionHandler(CommonException::class)
	fun handleMonopolyException(request: HttpServletRequest, e: CommonException): ResponseEntity<*> {
		logger.error(e) { "[${e.errorType.code}] [${request.requestURI}] EXCEPTION -> $e" }
		return ResponseEntity(e.toResponse(), HttpStatus.valueOf(e.status))
	}

}