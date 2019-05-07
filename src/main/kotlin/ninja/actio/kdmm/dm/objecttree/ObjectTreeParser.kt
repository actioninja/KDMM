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
        private val defineWithParametersRegex = Regex("#define\\s+([\\w\\d]+)\\((([_\\w\\d],?\\s?)+)\\)\\s+(.+)")

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
        parseRootDir = file.parent

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
                        macros[result.groupValues[1]] = result.groupValues[2].replace("$", "\\$")
                    } else {
                        result = defineWithParametersRegex.find(line)
                        if (result != null) {
                            //TODO: make this less unsafe for parameterized defines
                            val key = result.groupValues[1]
                            val parametersList = result.groupValues[2].split(',')
                            var content = result.groupValues[4]
                            for ((i, parameter) in parametersList.withIndex()) {
                                content = content.replace(parameter.trim(), "{{{$i}}}")
                            }
                            macros[key] = content
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
            for (part in splitSemi) {
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
        logger.debug { "Finished parse, resulting object tree: $objectTree" }
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
                logger.debug { "Found macro \"$macro\" in line: $line" }
                val possibleOpenLoc = position + macro.length
                lateinit var target: String
                lateinit var replace: String
                if (possibleOpenLoc <= line.lastIndex && line[possibleOpenLoc] == '(') {
                    var parenLoopPosition = possibleOpenLoc - 1
                    var openBracket = 0
                    val lowLevelCommaLocs = mutableListOf<Int>()
                    do {
                        parenLoopPosition++
                        if (line[parenLoopPosition] == '(') openBracket++
                        else if (line[parenLoopPosition] == ')') openBracket--
                        else if (line[parenLoopPosition] == ',' && openBracket == 1)
                            lowLevelCommaLocs.add(parenLoopPosition)
                    } while (openBracket > 0)
                    val parameters = line.substring(possibleOpenLoc + 1, parenLoopPosition) //without opening parenthesis
                    val parameterList = mutableListOf<String>()
                    if (lowLevelCommaLocs.isEmpty()) {
                        parameterList.add(parameters)
                    } else {
                        val ranges = mutableListOf<IntRange>()
                        var lastEndLoc = 0
                        for (int in lowLevelCommaLocs) {
                            //offset the found int so it's in the range we're looking for, then back by 2 so it's before
                            val adjusted = (int - possibleOpenLoc) - 2
                            if (lastEndLoc > 0)
                                ranges.add((lastEndLoc + 2)..adjusted)
                            else
                                ranges.add(lastEndLoc..adjusted)
                            lastEndLoc = adjusted
                        }
                        ranges.add((lastEndLoc + 2)..parameters.lastIndex)
                        for (range in ranges) {
                            parameterList.add(parameters.substring(range).trim())
                        }
                    }
                    val result = macroParameterResolve(parameterList, replacement)
                    target = line.substring(position..parenLoopPosition)
                    replace = result
                } else {
                    target = macro
                    replace = replacement
                }
                logger.debug { "attempting to recurse..." }
                replace = macroSubstititue(replace)
                line = line.replace(target, replace)
                position = line.indexOf(macro)
            }
        }
        return line
    }

    //TODO
    //There is without a doubt a much, much better way to do this, but we tech debt now bois
    fun macroParameterResolve(parameters: List<String>, content: String): String {
        var working = content
        for ((i, parameter) in parameters.withIndex()) {
            val target = "{{{$i}}}"
            val targetNoSpace = "##$target"
            working = working.replace(targetNoSpace, parameter)
            working = working.replace(target, " $parameter ")
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
