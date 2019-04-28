package ninja.actio.kdmm.dm

import ninja.actio.kdmm.KDMM
import java.io.InputStream

fun getFileInternal(path: String): InputStream {
    return KDMM::class.java.classLoader.getResourceAsStream(path)
}
