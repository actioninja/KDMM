package ninja.actio.kdmm.dm.objecttree

import java.util.regex.Pattern

object InstanceFactory {
    fun deriveFrom(obj: DMObject, diffVars: Map<String, DMVar> = mapOf()): ObjectInstance {
        return if (obj is ObjectInstance) {
            val newInstance = ObjectInstance(obj.parent, obj.vars)
            newInstance.vars.putAll(diffVars)
            obj.parent.addInstance(newInstance)
            newInstance
        } else {
            val castParent = obj as ObjectTreeItem
            val newInstance = ObjectInstance(castParent)
            newInstance.vars.putAll(diffVars)
            castParent.addInstance(newInstance)
            newInstance
        }
    }

    fun parseStringToInstace(tree: ObjectTree, string: String): ObjectInstance {
        if (!string.contains('{'))
            return deriveFrom(tree.get(string))

        val m = Pattern.compile("([\\w/]+)\\{(.*)\\}").matcher(string)
        if (m.find()) {
            val vars = mutableMapOf<String, DMVar>()
            val varMatcher =
                Pattern.compile("([\\w]+) ?= ?((?:\"(?:\\\\\"|[^\"])*\"|[^;])*)(?:$|;)").matcher(m.group(2))
            while (varMatcher.find()) {
                var value = varMatcher.group(2)
                val type = if (value.startsWith('"') and value.endsWith('"')) {
                    value = value.removeSurrounding("\"", "\"")
                    DMVarType.STRING
                } else {
                    DMVarType.NUMBER
                }
                vars[varMatcher.group(1)] = DMVar(value, type)
            }
            val parent = tree.get(m.group(1))
            return deriveFrom(parent, vars)
        }

        System.out.println("Bad string passed to instance parser")
        return ObjectInstance(ObjectTreeItem(""), mutableMapOf()) //If you reach here, bad news
    }
}