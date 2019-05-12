package ninja.actio.kdmm.dm.dmm

import com.uchuhimo.collections.MutableBiMap
import com.uchuhimo.collections.mutableBiMapOf
import mu.KotlinLogging
import ninja.actio.kdmm.dm.dmm.diff.DMMDiff
import ninja.actio.kdmm.dm.dmm.diff.ExpandedKeysDiff
import ninja.actio.kdmm.dm.dmm.diff.InstanceDiff
import ninja.actio.kdmm.dm.dmm.diff.MapDiff
import ninja.actio.kdmm.dm.objecttree.ObjectTree
import java.io.File
import java.util.*

private val logger = KotlinLogging.logger {}

data class DMM(
    var minX: Int = 1,
    var minY: Int = 1,
    var minZ: Int = 1,
    var maxX: Int = 1,
    var maxY: Int = 1,
    var maxZ: Int = 1,
    var keyLength: Int = 0,
    val unusedKeys: MutableList<String> = mutableListOf(),
    val instances: MutableBiMap<String, TileInstance> = mutableBiMapOf(),
    val map: MutableMap<Location, String> = mutableMapOf(),
    val file: File = File(""),
    val objectTree: ObjectTree = ObjectTree(),
    val diffStack: Stack<DMMDiff> = Stack()
) {
    fun putMap(loc: Location, key: String, addDiff: Boolean = true) {
        val oldKey = map[loc]
        if (addDiff && oldKey != null) {
            diffStack.push(MapDiff(this, loc, key, oldKey))
        }
        if (oldKey != null) {
            val instance = instances[oldKey]
            if (instance != null)
                instance.refCount--
        }
        if (instances.containsKey(key)) {
            val instance = instances[key]
            if (instance != null)
                instance.refCount--
            map[loc] = key
        }
    }

    fun getKeyForInstance(tileInstance: TileInstance): String {
        if (instances.inverse.containsKey(tileInstance)) {
            return instances.inverse[tileInstance]!!
        }
        if (unusedKeys.size == 0)
            expandKeys()
        if (unusedKeys.size > 0) {
            //Pick a random key, since it reduces the likelihood of a merge conflict
            val key = unusedKeys[(0..unusedKeys.lastIndex).random()]
            unusedKeys.remove(key)
            diffStack.push(InstanceDiff(this, key, tileInstance))
            instances[key] = tileInstance
            return key
        }
        logger.error { "Something went wrong with getting an new key for an instance" }
        return ""
    }

    fun generateKeys(set: MutableSet<String>, length: Int, prefix: String = "") {
        if (length <= 0) {
            set.add(prefix)
            return
        }
        for (char in ('a' until 'z'))
            generateKeys(set, length - 1, "$prefix$char")
        for (char in ('A' until 'Z'))
            generateKeys(set, length - 1, "$prefix$char")
    }

    fun expandKeys() {
        keyLength++
        val unusedKeysSet = mutableSetOf<String>()
        generateKeys(unusedKeysSet, keyLength)
        val newUnusedKeys = unusedKeysSet.toMutableList()
        val newInstances = mutableBiMapOf<String, TileInstance>()
        val newMap = mutableMapOf<Location, String>()
        val substitutions = mutableMapOf<String, String>()
        for ((key, instance) in instances) {
            val newKey = newUnusedKeys[(0..newUnusedKeys.lastIndex).random()]
            newUnusedKeys.remove(newKey)
            substitutions[key] = newKey
            newInstances[newKey] = instance
        }
        for ((location, key) in map) {
            newMap[location] = substitutions[key]!!
        }

        diffStack.push(
            ExpandedKeysDiff(
                this,
                map,
                newMap,
                instances,
                newInstances,
                unusedKeys,
                newUnusedKeys,
                keyLength - 1,
                keyLength
            )
        )
        instances.clear()
        instances.putAll(newInstances)
        map.clear()
        map.putAll(newMap)
        unusedKeys.clear()
        unusedKeys.addAll(newUnusedKeys)
    }

    fun setSize(
        newMaxX: Int,
        newMaxY: Int,
        newMaxZ: Int,
        newMinX: Int = minX,
        newMinY: Int = minY,
        newMinZ: Int = minZ
    ) {
        maxX = newMaxX
        maxY = newMaxY
        maxZ = newMaxZ
        minX = newMinX
        minY = newMinY
        minZ = newMinZ

        val world = objectTree.get("/world")

    }
}
