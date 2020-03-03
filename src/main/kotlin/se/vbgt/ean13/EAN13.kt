package se.vbgt.ean13

import java.lang.IllegalArgumentException

class EAN13(number: String) {

    // I'm lazy, so I throw exceptions in alot in when():s.
    // If for real use, do better!

    // remove whitespace
    private val theNumber: String = number.replace("""\s+""".toRegex(), "")

    init {
        // validate length and check digit
        require(theNumber.length == 13) { "Bad length" }
        val checkDigit = checkDigit(theNumber.take(12))
        require(checkDigit == theNumber.last()) { "Bad check digit" }
    }

    fun groups(): String =
        // The first digit determines the group of the next six digits
        // The last six digits always have group R
        when (theNumber.first()) {
            '0' -> "LLLLLLRRRRRR"
            '1' -> "LLGLGGRRRRRR"
            '2' -> "LLGGLGRRRRRR"
            '3' -> "LLGGGLRRRRRR"
            '4' -> "LGLLGGRRRRRR"
            '5' -> "LGGLLGRRRRRR"
            '6' -> "LGGGLLRRRRRR"
            '7' -> "LGLGLGRRRRRR"
            '8' -> "LGLGGLRRRRRR"
            '9' -> "LGGLGLRRRRRR"
            else -> throw IllegalArgumentException("Not a number")
        }

    fun modules(): String {
        val groups = groups()

        // The first digit determines the group of the next six digits
        val p1 = theNumber.drop(1).take(6)
            .mapIndexed { i: Int, c: Char -> mapModule(groups[i], c) }
            .joinToString(separator = "")

        // The last six digits always have group R
        val p2 = theNumber.drop(7)
            .map { mapModule('R', it) }
            .joinToString(separator = "")

        return "| |${p1} | | ${p2}| |"
    }

    companion object {
        fun mapModule(group: Char, digit: Char): String =
            when (group) {
                'L' -> mapModuleL(digit)
                'G' -> mapModuleG(digit)
                'R' -> mapModuleR(digit)
                else -> throw IllegalArgumentException("Not a group")

                // To lazy to manually replace 1 with | and 0 with space from the copy-pasted data from Wikipedia
            }.replace('1', '|').replace('0', ' ')


        private fun mapModuleR(digit: Char): String =
            when (digit) {
                '0' -> "1110010"
                '1' -> "1100110"
                '2' -> "1101100"
                '3' -> "1000010"
                '4' -> "1011100"
                '5' -> "1001110"
                '6' -> "1010000"
                '7' -> "1000100"
                '8' -> "1001000"
                '9' -> "1110100"
                else -> throw IllegalArgumentException("Not a digit")
            }


        private fun mapModuleG(digit: Char): String =
            when (digit) {
                '0' -> "0100111"
                '1' -> "0110011"
                '2' -> "0011011"
                '3' -> "0100001"
                '4' -> "0011101"
                '5' -> "0111001"
                '6' -> "0000101"
                '7' -> "0010001"
                '8' -> "0001001"
                '9' -> "0010111"
                else -> throw IllegalArgumentException("Not a digit")
            }


        private fun mapModuleL(digit: Char): String =
            when (digit) {
                '0' -> "0001101"
                '1' -> "0011001"
                '2' -> "0010011"
                '3' -> "0111101"
                '4' -> "0100011"
                '5' -> "0110001"
                '6' -> "0101111"
                '7' -> "0111011"
                '8' -> "0110111"
                '9' -> "0001011"
                else -> throw IllegalArgumentException("Not a digit")
            }
    }

    fun checkDigit(value: String): Char = '0' + (10 - value.reversed()
        .mapIndexed { i, c -> checkDigitMultiplier(i, c) }
        .sum()
        .rem(10)
            ).rem(10) // 10 - the remainder can become 10, so we do a remainder again to zero out 10

    fun checkDigitMultiplier(i: Int, c: Char): Int =
        if (i % 2 == 0) {
            3 * (c - '0')
        } else {
            c - '0'
        }

    fun saveImageTo(path: String): Unit = TODO("Bonus")
}
