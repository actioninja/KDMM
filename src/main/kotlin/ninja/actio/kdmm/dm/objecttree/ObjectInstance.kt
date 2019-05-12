package ninja.actio.kdmm.dm.objecttree

import java.awt.Color

class ObjectInstance(
    val parent: ObjectTreeItem,
    val vars: MutableMap<String, DMVar> = mutableMapOf()
) : DMObject() {

    override val dir: Int by lazy { getVarValue("dir").toInt() }
    override val pixelX: Int by lazy { getVarValue("pixel_x").toInt() }
    override val pixelY: Int by lazy { getVarValue("pixel_y").toInt() }
    override val plane: Int by lazy { getVarValue("plane").toInt() }
    override val layer: Float by lazy { getVarValue("layer").toFloat() }
    override val icon: String by lazy { getVarValue("icon") }
    override val iconState: String by lazy { getVarValue("icon_state") }
    override val dmColor: Color by lazy { getColor(getVarValue("color")) }

    val parentType: String
        get() = parent.path

    override fun getVar(key: String): DMVar {
        if (vars.containsKey(key))
            return vars[key]!!
        return parent.getVar(key)
    }

    override fun toStringDMM(tgm: Boolean): String {
        val out = StringBuilder(parentType)
        if (vars.isNotEmpty()) {
            out.append('{')
            if (tgm) out.append("\n\t")
            var isFirst = true
            for ((key, value) in vars) {
                if (isFirst)
                    isFirst = false
                else
                    out.append(";")
                if (tgm) out.append("\n\t")
                else
                    out.append(" ")
                out.append(key)
                out.append(" = ")
                when (value.type) {
                    DMVarType.STRING -> out.append("\"${value.value}\"")
                    DMVarType.BOOLEAN, DMVarType.NUMBER -> out.append(value.value)
                }
            }
            if (tgm) out.append("\n\t")
            out.append('}')
        }
        return out.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is ObjectInstance)
            return false
        if (other === this)
            return true
        if (other.toStringDMM() == toStringDMM())
            return true
        return false
    }

    override fun isType(typePath: String): Boolean {
        return parent.isType(typePath)
    }
}