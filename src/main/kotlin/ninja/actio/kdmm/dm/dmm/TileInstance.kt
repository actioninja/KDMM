package ninja.actio.kdmm.dm.dmm

import mu.KotlinLogging
import ninja.actio.kdmm.dm.objecttree.ObjectInstance
import ninja.actio.kdmm.dm.objecttree.ObjectTreeItem
import kotlin.math.sign

private val logger = KotlinLogging.logger {}

data class TileInstance(
    val objs: MutableList<ObjectInstance>,
    var refCount: Int = 0
) {
    companion object {
        fun fromString(string: String): TileInstance {
            TODO()
        }
    }

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

    val layerSorted: List<ObjectInstance> by lazy {
        val cachedSorted = mutableListOf<ObjectInstance>()
        cachedSorted.addAll(objs)
        cachedSorted.sortWith( Comparator { a, b ->
            var layerA = a.plane.toFloat()
            var layerB = b.plane.toFloat()
            if (layerA == layerB) {
                layerA = a.layer
                layerB = b.layer
            }
            if (layerA == layerB) {
                layerA = when {
                    a.isType("/obj") -> 1f
                    a.isType("/mob") -> 2f
                    a.isType("/turf") -> 3f
                    a.isType("/area") -> 4f
                    else -> 0f
                }
                layerB = when {
                    b.isType("/obj") -> 1f
                    b.isType("/mob") -> 2f
                    b.isType("/turf") -> 3f
                    b.isType("/turf") -> 4f
                    else -> 0f
                }
            }
            sign(layerA - layerB).toInt()
        })
        cachedSorted.toList()
    }

    fun sortObjects() {
        objs.sortWith( Comparator { a, b ->
            val iA = when  {
                a.isType("/obj") -> 1
                a.isType("/mob") -> 2
                a.isType("/turf") -> 3
                a.isType("/area") -> 4
                else -> 0
            }
            val iB = when {
                b.isType("/obj") -> 1
                b.isType("/mob") -> 2
                b.isType("/turf") -> 3
                b.isType("/turf") -> 4
                else -> 0
            }
            Integer.compare(iA, iB)
        })
    }

    val area: ObjectInstance by lazy {
        var returnValue = ObjectInstance(ObjectTreeItem(""))
        for (obj in objs) {
            if (obj.isType("/area")) returnValue = obj
        }
        if (returnValue.equals(ObjectInstance(ObjectTreeItem(""))))
            logger.error { "Tile Instance $this just tried to get it's area, but it didn't have one" }
        returnValue
    }
}
