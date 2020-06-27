package com.github.ssssssu12.kakaopay.sprinkle.controller

import com.github.ssssssu12.kakaopay.sprinkle.dto.SprinkleDTO
import com.github.ssssssu12.kakaopay.sprinkle.service.SprinkleService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/sprinkles")
class SprinkleController(
		private val sprinkleService: SprinkleService
) {
	companion object {
		private const val X_USER_ID = "X-USER-ID"
		private const val X_ROOM_ID = "X-ROOM-ID"
	}

	@GetMapping("/{token}")
	fun getSprinkle(
			@RequestHeader(X_USER_ID) userId: Long,
			@PathVariable token: String
	): SprinkleDTO.Get.Response = sprinkleService.getSprinkle(userId, token)

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	fun sprinkle(
			@RequestHeader(X_USER_ID) userId: Long,
			@RequestHeader(X_ROOM_ID) roomId: String,
			@RequestBody request: SprinkleDTO.Sprinkle.Request
	): SprinkleDTO.Sprinkle.Response = sprinkleService.sprinkle(userId, roomId, request)

	@PutMapping
	fun receive(
			@RequestHeader(X_USER_ID) userId: Long,
			@RequestHeader(X_ROOM_ID) roomId: String,
			@RequestBody request: SprinkleDTO.Receive.Request
	): SprinkleDTO.Receive.Response = sprinkleService.receive(userId, roomId, request)
}