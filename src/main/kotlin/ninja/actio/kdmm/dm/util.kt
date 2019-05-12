package ninja.actio.kdmm.dm

import ninja.actio.kdmm.KDMM
import java.io.File
import java.io.InputStream

val SYSTEM_SEPARATOR = File.separatorChar

fun getFileInternal(path: String): InputStream {
    return KDMM::class.java.classLoader.getResourceAsStream(path)
}

fun stripComments(string: String): String {
    val singleLineStripper = Regex("//.+")
    val blockStripper = Regex("/\\*(\\*(?!/)|[^*])*\\*/")
    val lineStripped = singleLineStripper.replace(string, "")
    val blockStripped = blockStripper.replace(lineStripped, "")
    return blockStripped.trimEnd()
}

fun cleanPath(string: String): String {
    var working = string
    if (!working.startsWith('/'))
        working = "/$working"
    working = working.removeSuffix("/")
    return working
}

fun String.numberOf(char: Char): Int {
    var count = 0
    for (character in this)
        if (character == char) count++
    return count
}

fun String.reverseCase(): String {
    val charArray = this.toCharArray()
    for ((i, char) in charArray.withIndex()) {
        if (char.isUpperCase()) {
            charArray[i] = char.toLowerCase()
        } else if (char.isLowerCase()) {
            charArray[i] = char.toUpperCase()
        }
    }
    return String(charArray)
}