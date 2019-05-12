package ninja.actio.kdmm.dm.objecttree

import java.awt.Color
import java.util.regex.Pattern

abstract class DMObject {

    abstract fun getVar(key: String): DMVar
    abstract fun isType(typePath: String): Boolean
    abstract fun toStringDMM(tgm: Boolean = false): String

    fun getVarValue(key: String): String {
        return getVar(key).value

    }

    //lazy works kind of weird with inheritance, this is a hack to make sure it works as expected
    abstract val dir: Int
    abstract val pixelX: Int
    abstract val pixelY: Int
    abstract val plane: Int
    abstract val layer: Float
    abstract val icon: String
    abstract val iconState: String
    fun getColor(string: String): Color {
        if (string.startsWith('#'))
            return Color.decode(string)
        var m = Pattern.compile("rgb ?\\( ?([\\d]+) ?, ?([\\d]+) ?, ?([\\d]+) ?\\)").matcher(string)
        if (m.find()) {
            val r = m.group(1).toInt()
            val g = m.group(2).toInt()
            val b = m.group(3).toInt()
            return Color(r, g, b)
        }
        m =
            Pattern.compile("(black|silver|grey|gray|white|maroon|red|purple|fuchsia|magenta|green|lime|olive|gold|yellow|navy|blue|teal|aqua|cyan)")
                .matcher(
                    string
                )
        if (m.find())
            return when (m.group(1)) {
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
        return Color(255, 255, 255)
    }

    abstract val dmColor: Color
}

