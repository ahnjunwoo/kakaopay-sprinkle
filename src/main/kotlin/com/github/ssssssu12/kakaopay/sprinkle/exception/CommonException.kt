package com.github.ssssssu12.kakaopay.sprinkle.exception

open class CommonException(
		val status: Int,
		val errorType: ErrorType
) : RuntimeException(errorType.message) {
	fun toResponse() = ErrorResponse(
			code = errorType.code,
			message = errorType.message
	)
}

class BadRequestException(error: ErrorType = ErrorType.BAD_REQUEST) : CommonException(400, error)
class NotFoundException(error: ErrorType = ErrorType.NOT_FOUND) : CommonException(404, error)
class InternalServerException(error: ErrorType = ErrorType.INTERNAL_SERVER_ERROR) : CommonException(500, error)