package ninja.actio.kdmm.dm.objecttree

import mu.KotlinLogging
import ninja.actio.kdmm.dm.SYSTEM_SEPARATOR
import ninja.actio.kdmm.dm.getFileInternal
import ninja.actio.kdmm.dm.stripComments
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader

private val logger = KotlinLogging.logger {}

//This parser is terrible
class ObjectTreeParser(var objectTree: ObjectTree = ObjectTree()) {

    companion object {
        //based off of FastDMM's regexes
        private val defineRegex = Regex("#define\\s+([\\d\\w]+)\\s+(.+)")
        private val defineWithParametersRegex = Regex("#define\\s+([\\w\\d]+\\((([_\\w\\d],?\\s?)+)\\))\\s+(.+)")

    }

    //Used for UI elements
    var currentlyParsing = ""
    var totalIncludes = 0
    var currentInclude = 0

    private val macros = mutableMapOf<String, String>()

    var parseRootDir = ""

    fun parseDME(file: File) {
        logger.debug { "Parsing DME: $file" }
        subParse(getFileInternal("stddef.dm"))
        parseRootDir = file.path

        parseFile(file)
    }

    fun parseFile(file: File) {
        currentlyParsing = file.absolutePath
        logger.debug { "Beginning parse on $file" }
        subParse(file.inputStream())
    }

    //This is largely based off of fastdmm's parser, but with much better macro support
    //It's still pretty bad of code
    fun parse(stream: InputStream) {
        val lines = cleanAndListize(stream)
        //precount defines for progress bars and the like
        for(line in lines) {
            if(line.trim().startsWith("#include")) {
                totalIncludes++
            }
        }

        var skipProcs = false
        for (line in lines) {
            //TODO: turn this into an actual preprocessor instead of whatever this cobbled together mess is
            logger.debug { "Starting preprocessing..." }
            //Preprocesser commands, look for #define, #inclue, etc.
            if (line.trim().startsWith('#')) {
                logger.debug { "Found preprocessor command: $line" }
                if (line.startsWith("#include")) {
                    var include = line.split(" ")[1]
                    var prefix = ""
                    include = include.removeSurrounding("\"")
                    if (include.contains('<') or include.contains('>')) {
                        include = include.removeSurrounding("<", ">")
                        prefix = "lib$SYSTEM_SEPARATOR"
                    }
                    logger.debug { "Including: \"$include\"" }
                    val realPath = "$prefix$include"
                    if(realPath.endsWith(".dm") or realPath.endsWith(".dme")) {
                        val includeFile = File("$parseRootDir$SYSTEM_SEPARATOR$realPath")
                        if(!includeFile.exists()) {
                            logger.error { "Just tried to include a nonexistant file: ${includeFile.path}" }
                            continue
                        }
                        currentInclude++
                        subParse(includeFile.inputStream())
                    }
                    continue
                }
                if (line.startsWith("#undef")) {
                    val macroName = line.split(" ")[1]
                    logger.debug { "Undefining: $macroName" }
                    if (macros.containsKey(macroName)) {
                        macros.remove(macroName)
                    } else {
                        logger.error { "Tried to remove $macroName, but it wasn't in defines" }
                    }
                    continue
                }
                if (line.startsWith("#define")) {
                    var result = defineRegex.find(line)
                    if(result != null) {
                        macros[result.groupValues[0]] = result.groupValues[1].replace("$", "\\$")
                    } else {
                        result = defineWithParametersRegex.find(line)
                        if(result != null) {
                            //TODO: make this less unsafe for parameterized defines
                            val key = result.groupValues[0]
                            val parametersList = result.groupValues[1].split(',')
                            var content = result.groupValues[3]
                            for((i, parameter) in parametersList.withIndex()) {
                               content = content.replace(parameter.trim(), "{{{$i}}}")
                            }
                            macros[key] = content
                        } else {
                            logger.error { "#define detected but regex couldn't parse it: $line" }
                        }
                    }
                }
                //TODO: ifs and other conditionals
            }
            //indentation
            var indentLevel = 0
            for(char in line) {
                if(char == ' ')
                    indentLevel++
                else
                    break
            }
            //Skip proc definitions because we don't care about them
            //This won't work with some "byond syntax" but that style is absolutely garbage and I don't care enough to support it
            if(indentLevel == 0) {
                if(line.contains("proc/")) {
                    skipProcs = true
                    continue
                } else {
                    skipProcs = false
                }
            } else if(skipProcs) continue
        }
    }

    fun subParse(stream: InputStream) {
        logger.debug { "Subparsing..." }
        val parser = ObjectTreeParser(objectTree)
        parser.macros.putAll(macros)
        parser.parseRootDir = parseRootDir
        parser.parse(stream)
    }

    fun defineParameterResolve(parameters: String, content: String): String {
        var working = content
        val parameterList = parameters.split(',')
        for((i, parameter) in parameterList.withIndex()) {
            val regex = Regex("##{{{$i}}}")
            var pad = " "
            if(regex.find(content) != null) {
                pad = ""
            }
            working = working.replace("{{{$i}}}", "$pad${parameter.trim()}$pad")
        }
        return working
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
