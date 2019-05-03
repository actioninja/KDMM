package ninja.actio.kdmm.test.util

import ninja.actio.kdmm.dm.cleanPath
import ninja.actio.kdmm.dm.stripComments
import org.junit.jupiter.api.Test
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.test.assertEquals

class UtilFunctionsTests {
    val classLoader = javaClass.classLoader
    val commentStream = classLoader.getResourceAsStream("comment_stripper_test.txt")
    val expectedStrippedResult = listOf(
        "non comment text",
        "test"
    )

    @Test
    fun `Comment stripper test`() {
        val br = BufferedReader(InputStreamReader(commentStream))
        val list = mutableListOf<String>()
        lateinit var line: String
        br.forEachLine {
            line = stripComments(it)
            if (line.isNotBlank()) {
                list.add(line)
            }
        }

        val outList = list.toList()

        assertEquals(expectedStrippedResult, outList)
    }

    @Test
    fun `Path cleaner test`() {
        var goodPath = "/test/test1"
        var badPathPrefix = "test/test1"
        var badPathSuffix = "/test/test1/"
        var badPathBoth = "test/test1/"

        goodPath = cleanPath(goodPath)
        badPathPrefix = cleanPath(badPathPrefix)
        badPathSuffix = cleanPath(badPathSuffix)
        badPathBoth = cleanPath(badPathBoth)

        val goalPath = "/test/test1"
        assertEquals(goalPath, goodPath)
        assertEquals(goalPath, badPathPrefix)
        assertEquals(goalPath, badPathSuffix)
        assertEquals(goalPath, badPathBoth)
    }
}