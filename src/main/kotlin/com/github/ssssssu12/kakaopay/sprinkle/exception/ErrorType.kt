package com.github.ssssssu12.kakaopay.sprinkle.exception

enum class ErrorType(val code: String, val message: String) {
	INTERNAL_SERVER_ERROR("SERVER_ERROR", "시스템 오류가 발생하였습니다."),
	BAD_REQUEST("BAD_REQUEST", "잘못된 요청입니다."),
	NOT_FOUND("NOT_FOUND", "요청하신 내용을 찾을 수 없습니다."),

	ALREADY_FINISHED("ALREADY_FINISHED", "이미 종료되었습니다."),
	ALREADY_RECEIVED("ALREADY_RECEIVED", "이미 받은 이력이 있습니다."),
	SAME_SPRINKLE_USER("SAME_SPRINKLE_USER", "자신이 뿌리기 한 건은 자신이 받을 수 없습니다."),
	NOT_SAME_ROOM("NOT_SAME_ROOM", "대화방이 동일하지 않습니다."),
	SPRINKLE_REQUEST_TIMEOUT("SPRINKLE_REQUEST_TIMEOUT", "유효기간이 지난 요청입니다.")
}