package ninja.actio.kdmm.dm.objecttree

import java.util.*

class ObjectTreeItem(var path: String, val parent: ObjectTreeItem? = null) : DMObject() {

    val subtypes = mutableListOf<ObjectTreeItem>()
    val vars = TreeMap<String, DMVar>()
    val instances = mutableListOf<ObjectInstance>()

    init {
        path = path.trim()
        setVar("type", "path")
        if(parent != null) {
            parent.subtypes.add(this)
            setVar("parentType", parent.path)
        }
        //TODO: default instance
    }


    override fun getVarValue(key: String): DMVar {
        if(vars.containsKey(key))
            return vars[key]!!
        if(parent != null)
            return parent.getVarValue(key)
        return DMVar("null")
    }

    fun setVar(key: String, value: String) {
        if(!vars.contains(key)) {
            vars[key] = DMVar(value)
        } else {
            vars[key]!!.value = value
        }
    }

    fun getAllVars(): Map<String, DMVar> {
        val allVars = mutableMapOf<String, DMVar>()
        if(parent != null)
            allVars.putAll(parent.getAllVars())
        allVars.putAll(vars)
        return allVars
    }

    override fun isType(typePath: String): Boolean {
        if(path == typePath)
            return true
        if(parent != null)
            return parent.isType(typePath)
        return false
    }

    fun addInstance(instance: ObjectInstance) {
        if(instances.contains(instance))
            return
        instances.add(instance)
        instances.sortBy { it.toStringDM() }
        //TODO: listeners
    }

    fun removeInstance(instance: ObjectInstance) {
        val index = instances.indexOf(instance)
        if(index == -1)
            return
        instances.remove(instance)
        //TODO: listeners
    }

    val parentlessName: String
        get() = when {
            (parent != null && path.startsWith(parent.path)) -> path.substring(parent.path.length)
            else -> path
        }

    override fun toStringDM(tgm: Boolean): String {
        return path
    }

    //TODO: listeners

}

