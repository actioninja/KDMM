package ninja.actio.kdmm.dm.dmm.diff

import ninja.actio.kdmm.dm.dmm.DMM
import ninja.actio.kdmm.dm.undo.IUndoable

abstract class DMMDiff(val dmm: DMM): IUndoable
