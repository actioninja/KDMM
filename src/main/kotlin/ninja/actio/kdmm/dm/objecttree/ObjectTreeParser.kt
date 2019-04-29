package ninja.actio.kdmm.dm.objecttree

import ninja.actio.kdmm.dm.getFileInternal
import ninja.actio.kdmm.dm.stripComments
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader

class ObjectTreeParser(var objectTree: ObjectTree = ObjectTree()) {
    init {

    }

    companion object {
        //Taken from FastDMM
        private val QUOTES_PATTERN = Regex("^\"(.*)\"$")
        private val DEFINE_PATTERN = Regex("#define +([\\d\\w]+) +(.+)")
        private val UNDEF_PATTERN = Regex("#undef[ \\t]*([\\d\\w]+)")
        private val MACRO_PATTERN = Regex("(?<![\\d\\w\"])\\w+(?![\\d\\w\"])")
    }


    private val macros = mutableMapOf<String, String>()

    var parseRootDir = ""

    fun parseDME(file: File) {
        subParse(getFileInternal("stddef.dm"))
        parseRootDir = file.path
        parseFile(file)
    }

    fun parseFile(path: String) {
        subParse(File(path).inputStream())
    }

    fun parseFile(file: File) {
        subParse(file.inputStream())
    }

    fun parse(stream: InputStream) {
        var parenthesisDepth = 0
        var stringDepth = 0
        var stringExpDepth = 0

        val includesFound = mutableListOf<String>()

        val lines = cleanAndListize(stream)

        for (line in lines) {
            //Preprocess, look for #define, #inclue, etc.
            if (line.trim().startsWith('#')) {
                if (line.startsWith())
            }

        }

        for (found in includesFound) {
            parseFile("")
        }
    }

    fun subParse(stream: InputStream) {
        val parser = ObjectTreeParser(objectTree)
        parser.macros.putAll(macros)
        parser.parseRootDir = parseRootDir
        parser.parse(stream)
    }

    //Turns spaces into tabs, strips comments, and single lines multilines, clears blank lines, then returns the file as a list instead of a stream
    fun cleanAndListize(stream: InputStream): List<String> {
        val reader = BufferedReader(InputStreamReader(stream))
        val lines = mutableListOf<String>()
        val runOn = StringBuilder()
        lateinit var line: String
        var firstMultiline = true

        reader.forEachLine {
            line = it
            line = stripComments(line)
            line = line.replace('\t', ' ')
            line = line.replace("    ", " ")
            if (line.isNotBlank()) {
                if (line.endsWith('\\')) {
                    line = line.removeSuffix("\\")
                    if (firstMultiline) {
                        firstMultiline = false
                    } else {
                        line = line.trimStart()
                    }
                    runOn.append(line)

                } else {
                    if (!firstMultiline)
                        line = line.trimStart()
                    runOn.append(line)
                    line = runOn.toString()
                    runOn.clear()
                    firstMultiline = true
                    lines.add(line)
                }
            }
        }

        return lines.toList()
    }
}
