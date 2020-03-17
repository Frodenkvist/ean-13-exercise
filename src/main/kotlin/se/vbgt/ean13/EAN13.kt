package se.vbgt.ean13

import kotlin.math.ceil

fun String.mapToModules(group: String) = mapIndexed { i, c ->
    EAN13.mapModule(group[i], c)
}.joinToString("")

class EAN13(number: String) {
    private val parsedNumbers: String = number.replace("\\s+".toRegex(), "").also {
        require(it.length == 13)
        validateCheckDigit(it)
    }

    companion object {
        private const val START_MARKER = "| |"
        private const val MIDDLE_MARKER = " | | "
        private const val END_MARKER = "| |"

        private val GROUPS = mapOf(
            '0' to "LLLLLLRRRRRR",
            '1' to "LLGLGGRRRRRR",
            '2' to "LLGGLGRRRRRR",
            '3' to "LLGGGLRRRRRR",
            '4' to "LGLLGGRRRRRR",
            '5' to "LGGLLGRRRRRR",
            '6' to "LGGGLLRRRRRR",
            '7' to "LGLGLGRRRRRR",
            '8' to "LGLGGLRRRRRR",
            '9' to "LGGLGLRRRRRR"
        )

        private val R_CODES = mapOf(
            '0' to "|||  | ",
            '1' to "||  || ",
            '2' to "|| ||  ",
            '3' to "|    | ",
            '4' to "| |||  ",
            '5' to "|  ||| ",
            '6' to "| |    ",
            '7' to "|   |  ",
            '8' to "|  |   ",
            '9' to "||| |  "
        )

        private val G_CODES = R_CODES.map { it.key to it.value.reversed() }.toMap()
        private val L_CODES = R_CODES.map {
            it.key to it.value.map {
                c -> if(c == '|') ' ' else '|'
            }.joinToString("")
        }.toMap()

        fun mapModule(group: Char, digit: Char): String = when(group) {
            'L' -> L_CODES[digit] ?: throw IllegalArgumentException()
            'G' -> G_CODES[digit] ?: throw IllegalArgumentException()
            'R' -> R_CODES[digit] ?: throw IllegalArgumentException()
            else -> throw IllegalArgumentException()
        }
    }

    fun groups(): String = GROUPS[parsedNumbers[0]] ?: throw IllegalArgumentException()
    fun modules(): String =
        START_MARKER +
        parsedNumbers.substring(1, 7).mapToModules(groups().substring(0, 6)) +
        MIDDLE_MARKER +
        parsedNumbers.substring(7, 13).mapToModules(groups().substring(6, 12)) +
        END_MARKER

    fun saveImageTo(path: String): Unit = TODO("bonus")

    private fun validateCheckDigit(number: String) {
        val checkDigit = number.substring(0, 12)
            .map(Character::getNumericValue)
            .mapIndexed { i, digit ->
                digit * if(i % 2 == 0) 1 else 3
            }.sum().let {
                (ceil(it / 10.0) * 10) - it
            }.toString()[0]

        require(checkDigit == number[12])
    }
}
