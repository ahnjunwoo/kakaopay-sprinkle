package com.github.ssssssu12.kakaopay.sprinkle.utils

import kotlin.math.pow

object RandomUtils {

	private val digits = mutableListOf(
			"0", "1", "2", "3", "4", "5", "6", "7",
			"8", "9", "a", "b", "c", "d", "e", "f",
			"g", "h", "i", "j", "k", "l", "m", "n",
			"o", "p", "q", "r", "s", "t", "u", "v",
			"w", "x", "y", "z", "A", "B", "C", "D",
			"E", "F", "G", "H", "I", "J", "K", "L",
			"M", "N", "O", "P", "Q", "R", "S", "T",
			"U", "V", "W", "X", "Y", "Z", "!", "@"
	)

	fun makeUUID(source: Long, targetLength: Int): String {
		val min = 64.pow(targetLength - 1)
		val max = 64.pow(targetLength)
		var adjustedSource = source % max
		if (adjustedSource < min) {
			adjustedSource += min
		}
		digits.shuffle()

		// 64 진수 변환 로직
		val shift = 6
		val buf = CharArray(64)
		var charPos = 64
		val radix = 1 shl shift
		val mask = radix - 1L
		var number: Long = adjustedSource
		do {
			buf[--charPos] = digits[(number and mask).toInt()][0]
			number = number ushr shift
		} while (number != 0L)
		return String(buf, charPos, 64 - charPos)
	}
}

fun Int.pow(x: Int) = this.toDouble().pow(x.toDouble()).toLong()