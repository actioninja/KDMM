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
