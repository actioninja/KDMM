package ninja.actio.kdmm.dm.objecttree

import java.util.*

class ObjectTreeItem(var path: String, val parent: ObjectTreeItem? = null) : DMObject() {

    val subtypes = mutableListOf<ObjectTreeItem>()
    val vars = TreeMap<String, String>()
    val instances = mutableListOf<ObjectInstance>()

    init {
        path = path.trim()
        vars["type"] = path
        if(parent != null) {
            parent.subtypes.add(this)
            vars["parentType"] = parent.path
        }
        //TODO: default instance
    }


    override fun getVar(key: String): String {
        if(vars.containsKey(key))
            return vars[key]!!
        if(parent != null)
            return parent.getVar(key)
        return "null"
    }

    fun setVar(key: String, value: String) {
        vars[key] = value
    }

    fun getAllVars(): Map<String, String> {
        val allVars = mutableMapOf<String, String>()
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

