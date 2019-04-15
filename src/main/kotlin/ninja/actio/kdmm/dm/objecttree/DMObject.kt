package ninja.actio.kdmm.dm.objecttree

import java.awt.Color
import java.util.regex.Pattern

abstract class DMObject {

    abstract fun getVar(key: String): DMVar
    abstract fun isType(typePath: String): Boolean
    abstract fun toStringDM(tgm: Boolean = false): String

    fun getVarValue(key: String): String {
        return getVar(key).value

    }

    val dir: Int by lazy { getVarValue("dir").toInt() }
    val pixelX: Int by lazy { getVarValue("pixel_x").toInt() }
    val pixelY: Int by lazy { getVarValue("pixel_y").toInt() }
    val plane: Int by lazy { getVarValue("plane").toInt() }
    val layer: Float by lazy { getVarValue("layer").toFloat() }
    val icon: String by lazy { getVarValue("icon") }
    val iconState: String by lazy { getVarValue("icon_state") }
    val dmColor: Color by lazy {
        val varValue = getVarValue("color")
        if(varValue.startsWith('#'))
            Color.decode(varValue)
        var m = Pattern.compile("rgb ?\\( ?([\\d]+) ?, ?([\\d]+) ?, ?([\\d]+) ?\\)").matcher(varValue)
        if(m.find()) {
            val r = m.group(1).toInt()
            val g = m.group(2).toInt()
            val b = m.group(3).toInt()
            Color(r, g, b)
        }
        m = Pattern.compile("\"(black|silver|grey|gray|white|maroon|red|purple|fuchsia|magenta|green|lime|olive|gold|yellow|navy|blue|teal|aqua|cyan)\"")
            .matcher(
                varValue
            )
        if(m.find())
            when (m.group(1)) {
                "black" -> Color.decode("#000000")
                "silver" -> Color.decode("#C0C0C0")
                "gray" -> Color.decode("#808080")
                "grey" -> Color.decode("#808080")
                "white" -> Color.decode("#FFFFFF")
                "maroon" -> Color.decode("#800000")
                "red" -> Color.decode("#FF0000")
                "purple" -> Color.decode("#800080")
                "fuchsia" -> Color.decode("#FF00FF")
                "magenta" -> Color.decode("#FF00FF")
                "green" -> Color.decode("#00C000")
                "lime" -> Color.decode("#00FF00")
                "olive" -> Color.decode("#808000")
                "gold" -> Color.decode("#808000")
                "yellow" -> Color.decode("#FFFF00")
                "navy" -> Color.decode("#000080")
                "blue" -> Color.decode("#0000FF")
                "teal" -> Color.decode("#008080")
                "aqua" -> Color.decode("#00FFFF")
                "cyan" -> Color.decode("#00FFFF")
                else -> Color.decode("#FFFFFF")
            }
        Color(255, 255, 255)

    }
}

