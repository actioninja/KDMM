package ninja.actio.kdmm.dm.objecttree

import mu.KotlinLogging
import ninja.actio.kdmm.dm.*
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
        private val defineWithParametersRegex = Regex("#define\\s+([\\w\\d_]+)\\((([_\\w\\d],?\\s?)+)\\)\\s+(.+)")
        private val emptyDefineRegex = Regex("#define\\s+([\\w\\d_]+)\\b")
        //TODO: properly refactor this to not be this hacky
        private val parameterPrefix = "^~\$#%"
    }

    //Used for UI elements
    var currentlyParsing = ""
    var totalIncludes = 0
    var currentInclude = 0

    private val macros = mutableMapOf<String, String>()

    var parseRootDir = ""
    var parseDir = ""

    fun parseDME(file: File) {
        logger.debug { "Parsing DME: $file" }
        subParse(getFileInternal("stddef.dm"))
        parseRootDir = file.parent
        objectTree.dmePath = file.path
        parseFile(file)
    }

    fun parseFile(file: File) {
        currentlyParsing = file.absolutePath
        logger.debug { "Beginning parse on $file" }
        subParse(file.inputStream(), file.parent)
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
                line = line.trim()
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
                        val includeFile = File("$parseDir$SYSTEM_SEPARATOR$realPath")
                        if (!includeFile.exists()) {
                            logger.error { "Just tried to include a nonexistant file: ${includeFile.path}" }
                            continue
                        }
                        currentInclude++
                        subParse(includeFile.inputStream(), includeFile.parent)
                    }
                    continue
                }
                if (line.startsWith("#undef")) {
                    val macroName = line.split(" ")[1]
                    logger.debug { "Undefining: $macroName" }
                    if (macros.containsKey(macroName)) {
                        macros.remove(macroName)
                    } else if (macros.containsKey("$parameterPrefix$macroName")) {
                        macros.remove("$parameterPrefix$macroName")
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
                            //TODO: proper refactor on this
                            macros["$parameterPrefix$key"] = content
                        } else {
                            result = emptyDefineRegex.find(line)
                            if (result != null) {
                                val key = result.groupValues[1]
                                macros[key] = ""
                            } else {
                                logger.error { "#define detected but regex couldn't parse it: $line" }
                            }
                        }
                    }
                    continue
                }
                if (line.startsWith("#error") or line.startsWith("#warn")) {
                    continue //this doesn't need handling
                }
                //TODO: ifs and other conditionals
                logger.error { "Unsupported preprocessor command found: $line" }
                continue
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
            line = macroSubstitute(line)

            //for some reason some html is still slipping through, I just want this working already so we tech debt now bois
            //TODO: fix this hack that manually skips a fucked up case
            if (line.startsWith('<')) continue

            val subsets = mutableListOf<Pair<String, String>>()
            //This means that there's inlined definitions. Time for even more cancerous of parsing
            //The extra !'s are special cases to make sure shit that shouldn't be included isn't
            //Hacky case by case work around with some awful shitcode, but it works for now
            //TODO: figure out a way to make this not a hack
            val ignoredCases = listOf(
                "{\"",
                "\"}",
                "if(",
                "if (",
                "else{",
                "else {",
                ") {",
                "){"
            )
            var ignored = false
            for (case in ignoredCases) {
                if (line.contains(case)) {
                    ignored = true
                    break
                }
            }
            if (!ignored && line.contains('{') && line.contains('}')) {
                logger.debug { "Advanced subparsing line: $line" }
                //First we gotta make sure the {} isn't in a string
                var skipEverything = false
                var inString = false
                var escaped = false
                quoteChecker@ for (char in line) {
                    when (char) {
                        '\\' -> {
                            escaped = true
                            continue@quoteChecker
                        }
                        '{', '}' -> {
                            if (inString) {
                                skipEverything = true
                                break@quoteChecker
                            }
                        }
                        '"' -> {
                            if (!escaped)
                                inString = !inString
                        }
                    }
                    if (escaped) escaped = false
                }
                if (!skipEverything) {
                    //Nasty "do way too much shit" loop
                    var position = 0
                    var openPos = 0
                    var currentObjPath = ""
                    readerloop@ while (true) {
                        when (line[position]) {
                            '{' -> {
                                if (currentObjPath.isNotEmpty()) { //I don't think this is possible but it's here just in case
                                    logger.error { "A line the parser can't handle properly was found:\n $line \n Report this!" }
                                    break@readerloop
                                }
                                //If we find an open parenthesis, we store its position, then backtrack to find the object path
                                openPos = position
                                var backtrackPos = position - 1
                                var preSpaceCleared =
                                    false // the space between the { and the object path needs to be skipped
                                if (line[backtrackPos] != ' ') //if this isn't a space, this means there isn't one and so it doesn't need to be skipped
                                    preSpaceCleared = true
                                while (backtrackPos > 0) { //safety check
                                    if (line[backtrackPos] == ' ') {
                                        if (preSpaceCleared) {
                                            break //since we just want an index, we can break to return the result we want
                                        }
                                    } else {
                                        preSpaceCleared = true
                                    }
                                    backtrackPos--
                                }
                                if (backtrackPos < 0) backtrackPos = 0
                                currentObjPath = line.substring(backtrackPos.until(openPos - 1)).trim()
                            }
                            '}' -> {
                                if (currentObjPath.isEmpty()) { //Again, shouldn't be possible, but here we are.
                                    logger.error { "A line the parser can't handle properly was found:\n $line \n Report this!" }
                                    break@readerloop
                                }
                                val parenthesisContent = line.substring((openPos + 1).until(position - 1)).trim()
                                subsets.add(Pair(currentObjPath, parenthesisContent))
                                currentObjPath = ""
                            }
                        }
                        position++
                        if (position > line.lastIndex) break //failsafe to make sure we don't cause an exception
                    }
                } else {
                    //wasn't a real inline
                    subsets.add(Pair("", line.trim()))
                }
            } else { //no inlines, we can do it the simple(r) way
                subsets.add(Pair("", line.trim()))
            }


            //split along semicolons
            for (pair in subsets) {
                val splitSemi = pair.second.split(';')
                for (part in splitSemi) {
                    if (pair.second.isBlank()) continue
                    for (i in (pathTree.size..(indentLevel + 1)))
                        pathTree.add("")

                    var offsetTopLevel = indentLevel
                    if (pair.first.isEmpty())
                        pathTree[indentLevel] = cleanPath(pair.second)
                    else {
                        pathTree[indentLevel] = cleanPath(pair.first)
                        offsetTopLevel++
                        pathTree[offsetTopLevel] = cleanPath("var/${part.trim()}")
                    }

                    if (pathTree.size > offsetTopLevel) {
                        var i = pathTree.lastIndex
                        while (i > offsetTopLevel) {
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
                    //Here's the spot you may want to change if path trees end up becoming a problem
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
                        //TODO: proper handling for parameters instead of this somewhat hacky solution
                        val splitComma = if (removedVar.contains('('))
                            listOf(removedVar)
                        else
                            removedVar.split(',')
                        for (commaPart in splitComma) {
                            val splitSemiButDeeper = commaPart.split(';')
                            splitResult.addAll(splitSemiButDeeper)
                        }
                        for (resultPart in splitResult) {
                            val split = resultPart.split('=', limit = 2)
                            val varName = split[0].trim()
                            if (split.size > 1) {
                                if (split[1].contains('"') or split[1].contains('(')) {
                                    val concatSplit = split[1].trim().split('+')
                                    if (concatSplit.size <= 1) {
                                        item.setVar(varName, split[1].trim().removeSurrounding("\"", "\""))
                                    } else {
                                        val concatBuilder = StringBuilder()
                                        for (concat in concatSplit) {
                                            concatBuilder.append(concat.trim().removeSurrounding("\"", "\""))
                                        }
                                        item.setVar(varName, concatBuilder.toString())
                                    }
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
    }

    fun subParse(stream: InputStream, path: String = "") {
        val parser = ObjectTreeParser(objectTree)
        parser.macros.putAll(macros)
        parser.parseRootDir = parseRootDir
        parser.parseDir = path
        parser.parse(stream)
    }

    //used for testing
    fun addMacro(key: String, replacement: String) {
        macros[key] = replacement
    }

    fun macroSubstitute(inLine: String): String {
        var line = inLine
        for ((macro, replacement) in macros) {
            if (macro.startsWith(parameterPrefix)) {
                val trueMacro = macro.removePrefix(parameterPrefix)
                val regex = Regex("\\b$trueMacro\\(")
                var result = regex.find(line)
                while (result != null) {
                    val openParenthesisPosition = result.range.first + trueMacro.length
                    var parenLoopPosition = openParenthesisPosition - 1
                    var openBracket = 0
                    val lowLevelCommaLocs = mutableListOf<Int>()
                    do {
                        parenLoopPosition++
                        when (line[parenLoopPosition]) {
                            '(' -> openBracket++
                            ')' -> openBracket--
                            ',' -> if (openBracket == 1) lowLevelCommaLocs.add(parenLoopPosition)
                        }
                    } while (openBracket > 0)
                    val parameters =
                        line.substring(openParenthesisPosition + 1, parenLoopPosition) //without opening parenthesis
                    val parameterList = mutableListOf<String>()
                    if (lowLevelCommaLocs.isEmpty()) {
                        parameterList.add(parameters)
                    } else {
                        val ranges = mutableListOf<IntRange>()
                        var lastEndLoc = 0
                        for (int in lowLevelCommaLocs) {
                            //offset the found int so it's in the range we're looking for, then back by 2 so it's before
                            val adjusted = (int - openParenthesisPosition) - 2
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
                    var replace = macroParameterResolve(parameterList, replacement)
                    replace = macroSubstitute(replace)
                    line = line.replaceRange((result.range.first..parenLoopPosition), replace)
                    result = regex.find(line)
                }
            } else {
                val regex = Regex("\\b$macro\\b")
                line = regex.replace(line, replacement)
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

    //Turns tabs into spaces, strips comments, and single lines multilines, clears blank lines, then returns the file as a list instead of a stream
    fun cleanAndListize(stream: InputStream): List<String> {
        val reader = BufferedReader(InputStreamReader(stream))
        val lines = mutableListOf<String>()
        val runOn = StringBuilder()
        lateinit var line: String
        var inBlockComment = false
        var inBackslash = false
        var firstMultiline = true
        var inString = false

        reader.forEachLine {
            line = it
            line = stripComments(line)
            line = line.replace('\t', ' ')
            line = line.replace("    ", " ")
            if (line.isNotBlank()) {
                if (inBlockComment) {
                    if (line.contains("*/")) {
                        line = line.substring(((line.indexOf("*/") + 2)..line.lastIndex))
                        inBlockComment = false
                    }
                    //I would put a continue here but lambdas like this don't support it
                } else if (!inString && line.contains("/*")) {
                    line = line.substring(0..(line.indexOf("/*") - 1))
                    inBlockComment = true
                } else if (line.endsWith('\\')) {
                    line = line.removeSuffix("\\")
                    inBackslash = true
                    if (firstMultiline) {
                        firstMultiline = false
                    } else {
                        line = line.trimStart()
                    }
                    runOn.append(line)
                } else if (inBackslash) {
                    if (!firstMultiline)
                        line = line.trimStart()
                    runOn.append(line)
                    line = runOn.toString()
                    runOn.clear()
                    firstMultiline = true
                    inBackslash = false
                    lines.add(line)
                } else if (inString or ((line.numberOf('"') > 0) and (line.numberOf('"') % 2 != 0))) {
                    //Count of NONESCAPED "
                    var trueCount = 0
                    var escapeFound = false
                    charLoop@ for (char in line) {
                        when (char) {
                            '"' -> {
                                if (!escapeFound) trueCount++
                            }
                            '\\' -> {
                                if (!escapeFound) {
                                    escapeFound = true
                                    continue@charLoop
                                }
                            }
                        }
                        if (escapeFound) escapeFound = false
                    }
                    if (inString && trueCount == 0) {
                        runOn.append(line.trim())
                    } else {
                        if (trueCount % 2 != 0) {
                            if (inString) {
                                runOn.append(line.trimStart())
                                line = runOn.toString()
                                runOn.clear()
                                lines.add(line)
                            } else {
                                runOn.append(line.trimEnd())
                                inString = true
                            }
                        }
                    }
                } else {
                    lines.add(line)
                }
            }
        }

        return lines.toList()
    }
}
