package com.example.morseapp

object MorseConverter {
    val MORSE_CODE_DICT = mapOf(
        'A' to ".-", 'B' to "-...", 'C' to "-.-.", 'D' to "-..", 'E' to ".",
        'F' to "..-.", 'G' to "--.", 'H' to "....", 'I' to "..", 'J' to ".---",
        'K' to "-.-", 'L' to ".-..", 'M' to "--", 'N' to "-.", 'O' to "---",
        'P' to ".--.", 'Q' to "--.-", 'R' to ".-.", 'S' to "...", 'T' to "-",
        'U' to "..-", 'V' to "...-", 'W' to ".--", 'X' to "-..-", 'Y' to "-.--", 'Z' to "--..",
        '1' to ".----", '2' to "..---", '3' to "...--", '4' to "....-", '5' to ".....",
        '6' to "-....", '7' to "--...", '8' to "---..", '9' to "----.", '0' to "-----",
        '.' to ".-.-.-", ',' to "--..--", '?' to "..--..", '!' to "-.-.--", ':' to "---...",
        '"' to ".-..-.", '\'' to ".----.", '/' to "-..-.", '(' to "-.--.", ')' to "-.--.-",
        '&' to ".-...", ';' to "-.-.-.", '=' to "-...-", '+' to ".-.-.", '-' to "-....-",
        '_' to "..--.-", '$' to "...-..-", '@' to ".--.-.",
        ' ' to "/"
    )

    val REVERSE_DICT = MORSE_CODE_DICT.entries.associate { it.value to it.key }

    private val EXTENDED_LATIN_MAP = mapOf(
        'Š' to "S", 'š' to "s", 'Đ' to "Dj", 'đ' to "dj",
        'Č' to "C", 'č' to "c", 'Ć' to "C", 'ć' to "c",
        'Ž' to "Z", 'ž' to "z"
    )

    fun textToMorse(text: String): String {
        val lines = text.uppercase().split("\n")
        return lines.joinToString("\n") { line ->
            val mapped = line.map { ch ->
                val normalized = EXTENDED_LATIN_MAP[ch] ?: ch.toString()
                normalized.uppercase().map { c -> MORSE_CODE_DICT[c] ?: "" }.joinToString(" ")
            }
            mapped.filter { it.isNotEmpty() }.joinToString(" ")
        }
    }

    fun morseToText(morse: String): String {
        val lines = morse.split("\n")
        return lines.joinToString("\n") { line ->
            line.split(" ").joinToString("") { token ->
                if (token == "/") " " else REVERSE_DICT[token]?.toString() ?: ""
            }
        }
    }
}