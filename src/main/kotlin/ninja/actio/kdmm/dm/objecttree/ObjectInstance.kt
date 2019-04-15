package ninja.actio.kdmm.dm.objecttree

class ObjectInstance(val vars: MutableMap<String, DMVar>, val parent: ObjectTreeItem): DMObject() {

    val parentType: String
        get() = parent.path

    override fun getVar(key: String): DMVar {
        if(vars.containsKey(key))
            return vars[key]!!
        return parent.getVarValue(key)
    }

    override fun toStringDM(tgm: Boolean): String {
        val out = StringBuilder(parentType)
        out.append('{')
        if(tgm) out.append("\n\t")
        var isFirst = true
        for((key, value) in vars) {
            if(isFirst)
                isFirst = false
            else
                out.append(";")
                if(tgm)
                    out.append("\n\t")
                else
                    out.append(" ")
            out.append(key)
            out.append(" = ")
            out.append(value.value)
        }
        if(tgm) out.append("\n\t")
        out.append('}')
        return out.toString()
    }

    override fun equals(other: Any?): Boolean {
        if(other !is ObjectInstance)
            return false
        if(other == this)
            return true
        if(other.toStringDM() == toStringDM())
            return true
        return false
    }

    override fun isType(typePath: String): Boolean {
        return parent.isType(typePath)
    }



}