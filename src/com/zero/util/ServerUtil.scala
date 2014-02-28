package com.zero.util

object ServerUtil {
	val BASE = 62;
	val UPPERCASE_OFFSET = 55
	val LOWERCASE_OFFSET = 61
	val DIGIT_OFFSET = 48

	def dehydrate(number: Int): String = {
		var tempNum = number

		val strBuilder = new StringBuffer;
		while (tempNum > 0) {
			val remainder = tempNum % BASE;
			strBuilder.append(numToChar(remainder))
			tempNum = tempNum / BASE;
		}
		strBuilder.toString()
	}

	def saturate(short: String): Int = {
		var sum: Int = 0
		val chars = short.toCharArray()
		List.range(0, chars.length).foreach(i => sum += (charToNum(chars(i)) * (Math.pow(BASE, i)).toInt))
		sum
	}

	def numToChar(num: Int): Char = {
		if (num < 10)
			(num + DIGIT_OFFSET).toChar
		else if (10 <= num && num <= 35)
			(num + UPPERCASE_OFFSET).toChar
		else if (36 <= num && num < 62)
			(num + LOWERCASE_OFFSET).toChar
		else
			'#'
	}

	def charToNum(char: Char): Int = {
		if (Character.isDigit(char))
			char.toInt - DIGIT_OFFSET
		else if ('A' <= char && char <= 'Z')
			char.toInt - UPPERCASE_OFFSET
		else if ('a' <= char && char <= 'z')
			char.toInt - LOWERCASE_OFFSET
		else
			0
	}
}
