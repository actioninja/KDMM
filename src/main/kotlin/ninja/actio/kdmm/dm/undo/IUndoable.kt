package ninja.actio.kdmm.dm.undo

interface IUndoable {
    fun undo()
    fun redo()
}