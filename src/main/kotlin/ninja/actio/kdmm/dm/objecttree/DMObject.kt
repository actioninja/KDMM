package ninja.actio.kdmm.dm.objecttree

import java.awt.Color
import java.util.regex.Pattern

abstract class DMObject {
    abstract fun getVar(key: String): String
    abstract fun isType(typePath: String): Boolean
    abstract fun toStringDM(tgm: Boolean = false): String

    val dirDelegate = DMVarNumberDelegate { getVar("dir").toInt() }
    val dir: Int by dirDelegate
    val pixelX: Int by DMVarNumberDelegate { getVar("pixel_x").toInt() }
    val pixelY: Int by DMVarNumberDelegate { getVar("pixel_y").toInt() }
    val plane: Int by DMVarNumberDelegate { getVar("plane").toInt() }
    val layer: Float by DMVarNumberDelegate { getVar("layer").toFloat() }
    val icon: String by DMVarStringDelegate { getVar("icon") }
    val iconState: String by DMVarStringDelegate { getVar("icon_state") }

    var dirty: Boolean = false
        set(x) {
            dirDelegate.valid = false
            field = x
        }



    //TODO: convert this to a delegate property as well
    var cachedColor: Color? = null
    fun getColor(): Color {
        if (cachedColor == null) {
            val varValue = getVar("color")
            var m = Pattern.compile("(#[\\d\\w][\\d\\w][\\d\\w][\\d\\w][\\d\\w][\\d\\w])").matcher(varValue)
            if (m.find())
                cachedColor = Color.decode(m.group(1))
            m = Pattern.compile("rgb ?\\( ?([\\d]+) ?, ?([\\d]+) ?, ?([\\d]+) ?\\)").matcher(varValue)
            if (m.find()) {
                var r = Integer.parseInt(m.group(1))
                var g = Integer.parseInt(m.group(2))
                var b = Integer.parseInt(m.group(3))
                if (r > 255)
                    r = 255
                if (g > 255)
                    g = 255
                if (b > 255)
                    b = 255
                cachedColor = Color(r, g, b)
            }
            m =
                Pattern.compile("\"(black|silver|grey|gray|white|maroon|red|purple|fuchsia|magenta|green|lime|olive|gold|yellow|navy|blue|teal|aqua|cyan)\"")
                    .matcher(
                        varValue
                    )
            if (m.find()) {
                cachedColor = when (m.group(1)) {
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
            }
            if (varValue != "null") {
                System.err.println("Unrecognized color $varValue")
                cachedColor = Color(255, 255, 255)
            }
        }
        return cachedColor as Color
    }

}

