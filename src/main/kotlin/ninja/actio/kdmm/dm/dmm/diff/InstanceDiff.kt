package ninja.actio.kdmm.dm.dmm.diff

import ninja.actio.kdmm.dm.dmm.DMM
import ninja.actio.kdmm.dm.dmm.TileInstance

class InstanceDiff(
    dmm: DMM,
    val key: String,
    val tileInstance: TileInstance
): DMMDiff(dmm) {
    override fun undo() {
        dmm.instances.remove(key)
        dmm.unusedKeys.add(key)
    }

    override fun redo() {
        dmm.instances[key] = tileInstance
        dmm.unusedKeys.remove(key)
    }
}
