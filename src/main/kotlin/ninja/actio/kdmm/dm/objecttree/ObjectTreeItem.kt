package ninja.actio.kdmm.dm.objecttree

import java.awt.Color
import java.util.*

class ObjectTreeItem(var path: String, val parent: ObjectTreeItem? = null) : DMObject() {

    override val dir: Int by lazy { getVarValue("dir").toInt() }
    override val pixelX: Int by lazy { getVarValue("pixel_x").toInt() }
    override val pixelY: Int by lazy { getVarValue("pixel_y").toInt() }
    override val plane: Int by lazy { getVarValue("plane").toInt() }
    override val layer: Float by lazy { getVarValue("layer").toFloat() }
    override val icon: String by lazy { getVarValue("icon") }
    override val iconState: String by lazy { getVarValue("icon_state") }
    override val dmColor: Color by lazy { getColor(getVarValue("color")) }

    val subtypes = mutableListOf<ObjectTreeItem>()
    val vars = TreeMap<String, DMVar>()
    val instances = mutableListOf<ObjectInstance>()

    init {
        path = path.trim()
        setVar("type", path)
        if (parent != null) {
            parent.subtypes.add(this)
            setVar("parentType", parent.path)
        }
        instances.add(InstanceFactory.deriveFrom(this))
    }


    override fun getVar(key: String): DMVar {
        if (vars.containsKey(key))
            return vars[key]!!
        if (parent != null)
            return parent.getVar(key)
        return DMVar("null")
    }

    fun setVar(key: String, value: String = "", type: DMVarType = DMVarType.STRING) {
        if (!vars.contains(key)) {
            vars[key] = DMVar(value, type)
        } else {
            vars[key]!!.value = value
        }
    }

    fun setVar(key: String, value: Number) {
        if (!vars.containsKey(key)) {
            vars[key] = DMVar(value.toString(), DMVarType.NUMBER)
        } else {
            vars[key]!!.value = value.toString()
        }
    }

    fun getAllVars(): Map<String, DMVar> {
        val allVars = mutableMapOf<String, DMVar>()
        if (parent != null)
            allVars.putAll(parent.getAllVars())
        allVars.putAll(vars)
        return allVars
    }

    override fun isType(typePath: String): Boolean {
        if (path == typePath)
            return true
        if (parent != null)
            return parent.isType(typePath)
        return false
    }

    fun addInstance(instance: ObjectInstance) {
        if (instances.contains(instance))
            return
        instances.add(instance)
        instances.sortBy { it.toStringDMM() }
    }

    fun removeInstance(instance: ObjectInstance) {
        val index = instances.indexOf(instance)
        if (index == -1)
            return
        instances.remove(instance)
    }

    val parentlessName: String
        get() = when {
            (parent != null && path.startsWith(parent.path)) -> path.substring(parent.path.length)
            else -> path
        }

    override fun toStringDMM(tgm: Boolean): String {
        return path
    }
}

