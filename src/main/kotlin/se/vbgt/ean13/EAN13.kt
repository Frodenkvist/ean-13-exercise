package se.vbgt.ean13

import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
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

        @JvmStatic
        fun main(args: Array<String>) {
            EAN13("5901234123457").saveImageTo("barcode.png")
        }
    }

    fun groups(): String = GROUPS[parsedNumbers[0]] ?: throw IllegalArgumentException()
    fun modules(): String =
        START_MARKER +
        parsedNumbers.substring(1, 7).mapToModules(groups().substring(0, 6)) +
        MIDDLE_MARKER +
        parsedNumbers.substring(7, 13).mapToModules(groups().substring(6, 12)) +
        END_MARKER

    fun saveImageTo(path: String): Unit {
        val width = 328
        val height = 194

        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

        image.createGraphics().run {
            color = Color.WHITE
            fillRect(0, 0, width, height)
            color = Color.BLACK

            drawNumbers(this, height)
            drawBars(this, height)
        }

        ImageIO.write(image, "png", File(path))
    }

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

    private fun drawNumbers(graphics: Graphics2D, height: Int) {
        graphics.font = Font("Arial Black", Font.BOLD, 25)
        "$parsedNumbers>".forEachIndexed { i, number ->
            when(i) {
                0 -> graphics.drawString(number.toString(), 2, height - 4)
                in 1..6 -> graphics.drawString(number.toString(), i * 21 + 11, height - 4)
                in 7..12 -> graphics.drawString(number.toString(), i * 21 + 24, height - 4)
                13 -> graphics.drawString(number.toString(), i * 21 + 33, height - 4)
            }
        }
    }

    private fun drawBars(graphics: Graphics2D, height: Int) {
        modules().forEachIndexed { i, module ->
            if(module == ' ') {
                return@forEachIndexed
            }

            val barHeight = when(i) {
                in 0..2, in 45..49, in 92..94 -> height - 6
                else -> height - 30
            }

            graphics.fillRect(i * 3 + 20, 3, 3, barHeight)
        }
    }
}
