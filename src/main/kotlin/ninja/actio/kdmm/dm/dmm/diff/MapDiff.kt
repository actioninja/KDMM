package ninja.actio.kdmm.dm.dmm.diff

import ninja.actio.kdmm.dm.dmm.DMM
import ninja.actio.kdmm.dm.dmm.Location


class MapDiff(
    dmm: DMM,
    val location: Location,
    val newInstance: String,
    val oldInstance: String
): DMMDiff(dmm){
    override fun undo() {
        dmm.putMap(location, oldInstance, false)
    }

    override fun redo() {
        dmm.putMap(location, newInstance, false)
    }
}