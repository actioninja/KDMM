package ninja.actio.kdmm.dm.dmm.diff

import com.uchuhimo.collections.BiMap
import ninja.actio.kdmm.dm.dmm.DMM
import ninja.actio.kdmm.dm.dmm.Location
import ninja.actio.kdmm.dm.dmm.TileInstance

class ExpandedKeysDiff(
    dmm: DMM,
    private val oldMap: Map<Location, String>,
    private val newMap: Map<Location, String>,
    private val oldInstances: BiMap<String, TileInstance>,
    private val newInstances: BiMap<String, TileInstance>,
    private val oldUnusedKeys: List<String>,
    private val newUnusedKeys: List<String>,
    private val oldKeyLength: Int,
    private val newKeyLength: Int
) : DMMDiff(dmm) {
    override fun undo() {
        dmm.map.clear()
        dmm.map.putAll(oldMap)
        dmm.instances.clear()
        dmm.instances.putAll(oldInstances)
        dmm.unusedKeys.clear()
        dmm.unusedKeys.addAll(oldUnusedKeys)
        dmm.keyLength = oldKeyLength
    }

    override fun redo() {
        dmm.map.clear()
        dmm.map.putAll(newMap)
        dmm.instances.clear()
        dmm.instances.putAll(newInstances)
        dmm.unusedKeys.clear()
        dmm.unusedKeys.addAll(newUnusedKeys)
        dmm.keyLength = newKeyLength
    }
}