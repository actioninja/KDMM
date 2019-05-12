package ninja.actio.kdmm.dm.dmm

import ninja.actio.kdmm.dm.objecttree.ObjectInstance

data class TileInstance(
    val objs: MutableList<ObjectInstance>,
    var refCount: Int = 0
) {
    fun toStringDMM(tgm: Boolean = false): String {
        val base = if (tgm) "\n" else ""
        val builder = StringBuilder(base)
        var isFirst = true
        for (obj in objs) {
            if (isFirst)
                isFirst = false
            else
                builder.append(",")
            if (tgm) builder.append("\n")
            builder.append(obj.toStringDMM(tgm))
        }
        return builder.toString()
    }
}
