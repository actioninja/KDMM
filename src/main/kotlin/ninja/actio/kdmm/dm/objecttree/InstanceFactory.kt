package ninja.actio.kdmm.dm.objecttree

import java.util.regex.Pattern

object InstanceFactory {
    fun deriveFrom(obj: DMObject, diffVars: Map<String, DMVar> = mapOf()): ObjectInstance {
        return if(obj is ObjectInstance) {
            val newInstance = ObjectInstance(obj.vars, obj.parent)
            newInstance.vars.putAll(diffVars)
            obj.parent.addInstance(newInstance)
            newInstance
        } else {
            val castParent = obj as ObjectTreeItem
            val newInstance = ObjectInstance(castParent.vars, castParent)
            castParent.addInstance(newInstance)
            newInstance
        }
    }

    fun parseStringToInstace(string: String, tree: ObjectTree): ObjectInstance {
        if(!string.contains('{'))
            return deriveFrom(tree.get(string))

        val m = Pattern.compile("([\\w/]+)\\{(.*)\\}").matcher(string)
        if(m.find()) {
            val vars = mutableMapOf<String, DMVar>()
            val varMatcher =
                Pattern.compile("([\\w]+) ?= ?((?:\"(?:\\\\\"|[^\"])*\"|[^;])*)(?:$|;)").matcher(m.group(2))
            while(varMatcher.find()) {
                vars[varMatcher.group(1)] = DMVar(varMatcher.group(2))
            }
            val parent = tree.get(m.group(1))
            val outInstance = ObjectInstance(vars, parent)
            parent.addInstance(outInstance)
        }

        System.out.println("Bad string passed to instance parser")
        return ObjectInstance(mutableMapOf(), ObjectTreeItem("")) //If you reach here, bad news
    }
}