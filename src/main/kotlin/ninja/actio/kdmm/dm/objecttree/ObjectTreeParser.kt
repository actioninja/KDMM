package ninja.actio.kdmm.dm.objecttree

import ninja.actio.kdmm.dm.getFileInternal
import ninja.actio.kdmm.dm.stripComments
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.file.Path

class ObjectTreeParser(val objectTree: ObjectTree = ObjectTree()) {
    companion object {
        //Taken from FastDMM
        private val QUOTES_PATTERN = Regex("^\"(.*)\"$")
        private val DEFINE_PATTERN = Regex("#define +([\\d\\w]+) +(.+)")
        private val UNDEF_PATTERN = Regex("#undef[ \\t]*([\\d\\w]+)")
        private val MACRO_PATTERN = Regex("(?<![\\d\\w\"])\\w+(?![\\d\\w\"])")
    }

    private var isCommenting = false
    private var isMultilineString = false
    private var multilineStringDepth = 0
    private var parenthesisDepth = 0
    private var stringDepth = 0
    private var stringExpDepth = 0

    private val macros = mutableMapOf<String, String>()

    fun parseDME(file: File) {
        subParse(getFileInternal("stddef.dm"))
    }

    fun parseFile(file: File) {
        subParse(file.inputStream())
    }

    fun parse(stream: InputStream) {
        val lines = cleanAndListize(stream)
    }

    fun subParse(stream: InputStream) {
        val parser = ObjectTreeParser(objectTree)
        parser.macros.putAll(macros)
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
            if(line.isNotBlank()) {
                if(line.endsWith('\\')) {
                    line = line.removeSuffix("\\")
                    if(firstMultiline) {
                        firstMultiline = false
                    } else {
                        line = line.trimStart()
                    }
                    runOn.append(line)

                } else {
                    if(!firstMultiline)
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
