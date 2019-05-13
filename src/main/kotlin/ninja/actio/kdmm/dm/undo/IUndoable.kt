package ninja.actio.kdmm.dm.undo

/**
 * implement this to make a data object that can be undone
 */
interface IUndoable {
    /**
     * The full set of operations to undo an action
     */
    fun undo()

    /**
     * The full set of operations to redo an action
     */
    fun redo()
}