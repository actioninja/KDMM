package ninja.actio.kdmm.dm.objecttree

import mu.KotlinLogging
import ninja.actio.kdmm.dm.SYSTEM_SEPARATOR
import ninja.actio.kdmm.dm.cleanPath
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
        for (line in lines) {
            if (line.trim().startsWith("#include")) {
                totalIncludes++
            }
        }

        val pathTree = mutableListOf<String>()

        for (l in lines) {
            var line = l
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
                    if (realPath.endsWith(".dm") or realPath.endsWith(".dme")) {
                        val includeFile = File("$parseRootDir$SYSTEM_SEPARATOR$realPath")
                        if (!includeFile.exists()) {
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
                    if (result != null) {
                        macros[result.groupValues[0]] = macroSubstititue(result.groupValues[1].replace("$", "\\$"))
                    } else {
                        result = defineWithParametersRegex.find(line)
                        if (result != null) {
                            //TODO: make this less unsafe for parameterized defines
                            val key = result.groupValues[0]
                            val parametersList = result.groupValues[1].split(',')
                            var content = result.groupValues[3]
                            for ((i, parameter) in parametersList.withIndex()) {
                                content = content.replace(parameter.trim(), "{{{$i}}}")
                            }
                            macros[key] = macroSubstititue(content)
                        } else {
                            logger.error { "#define detected but regex couldn't parse it: $line" }
                        }
                    }
                    continue
                }
                //TODO: ifs and other conditionals
            }
            //indentation
            var indentLevel = 0
            for (char in line) {
                if (char == ' ')
                    indentLevel++
                else
                    break
            }

            //substitute any macros in the current line
            line = macroSubstititue(line)

            //split along semicolons
            val splitSemi = line.split(';')
            for(part in splitSemi) {
                for (i in (pathTree.size..indentLevel))
                    pathTree.add("")
                pathTree[indentLevel] = cleanPath(line.trim())
                if (pathTree.size > indentLevel + 1) {
                    var i = pathTree.lastIndex
                    while (i > indentLevel) {
                        pathTree.removeAt(i)
                        i--
                    }
                }
                val fullPathBuilder = StringBuilder()
                for (pathComponent in pathTree)
                    fullPathBuilder.append(pathComponent)
                var fullPath = fullPathBuilder.toString()
                val divided = fullPath.split('/')
                //rebuild again but with only important shit
                val affectedBuilder = StringBuilder()
                for (string in divided) {
                    if (string.isEmpty()) continue
                    if ((string == "static") or (string == "global") or (string == "tmp")) continue
                    if ((string == "proc") or (string == "verb") or (string == "var")) break
                    if (string.contains('=') or string.contains('(')) break
                    affectedBuilder.append("/$string")
                }
                val item = objectTree.getOrCreate(affectedBuilder.toString())
                if (fullPath.contains('(') and (fullPath.indexOf('(') < fullPath.lastIndexOf('/')))
                    continue
                fullPath = fullPath.replace("/tmp", "")
                fullPath = fullPath.replace("/static", "")
                fullPath = fullPath.replace("/global", "")
                //parse out var definitions
                if (fullPath.contains("var/")) {
                    val removedVar = fullPath.substring(fullPath.lastIndexOf('/') + 1).trim()
                    val splitResult = mutableListOf<String>()
                    val splitComma = removedVar.split(',')
                    for (commaPart in splitComma) {
                        val splitSemi = commaPart.split(';')
                        splitResult.addAll(splitSemi)
                    }
                    for (resultPart in splitResult) {
                        val split = resultPart.split('=', limit = 2)
                        val varName = split[0].trim()
                        if (split.size > 1) {
                            if (split[1].contains('"')) {
                                item.setVar(varName, split[1].trim().removeSurrounding("\"", "\""))
                            } else {
                                item.setVar(varName, split[1].trim(), DMVarType.NUMBER)
                            }
                        } else {
                            item.setVar(varName)
                        }
                    }
                }
            }
        }
    }

    fun subParse(stream: InputStream) {
        logger.debug { "Subparsing..." }
        val parser = ObjectTreeParser(objectTree)
        parser.macros.putAll(macros)
        parser.parseRootDir = parseRootDir
        parser.parse(stream)
    }

    //used for testing
    fun addMacro(key: String, replacement: String) {
        macros[key] = replacement
    }

    fun macroSubstititue(inLine: String): String {
        var line = inLine
        for ((macro, replacement) in macros) {
            var position = line.indexOf(macro)
            while (position >= 0) {
                val possibleLoc = position + macro.length
                line = if (possibleLoc <= line.lastIndex && line[possibleLoc] == '(') {
                    val searchPattern = Regex("($macro\\((.+)\\))")
                    val searchResult = searchPattern.find(line)
                    val result = macroParameterResolve(
                        searchResult!!.groupValues[2],
                        replacement
                    ) //Why 2? I'm not sure, java regex was acting weird
                    line.replace(searchResult.groupValues[0], result)
                } else {
                    line.replace(macro, replacement)
                }
                position = line.indexOf(macro)
            }
        }
        return line
    }

    //TODO
    //There is without a doubt a much, much better way to do this, but we tech debt now bois
    fun macroParameterResolve(parameters: String, content: String): String {
        var working = content
        val parameterList = parameters.split(',')
        for ((i, parameter) in parameterList.withIndex()) {
            val regex = Regex("##\\{\\{\\{$i\\}\\}\\}")
            var target = "{{{$i}}}"
            var replacement = " ${parameter.trim()} "
            if (regex.find(content) != null) {
                target = "##$target"
                replacement = replacement.trim()
            }
            working = working.replace(target, replacement)
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
