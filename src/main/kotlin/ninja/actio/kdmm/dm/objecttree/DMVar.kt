package ninja.actio.kdmm.dm.objecttree

enum class DMVarType {
    STRING,
    NUMBER,
    BOOLEAN
}

data class DMVar(
    var value: String,
    var type: DMVarType = DMVarType.STRING,
    var dangerous: Boolean = false
)
