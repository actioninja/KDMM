package ninja.actio.kdmm.dm.dmm

import com.uchuhimo.collections.MutableBiMap
import com.uchuhimo.collections.mutableBiMapOf
import mu.KotlinLogging
import ninja.actio.kdmm.dm.dmm.diff.DMMDiff
import ninja.actio.kdmm.dm.dmm.diff.ExpandedKeysDiff
import ninja.actio.kdmm.dm.dmm.diff.InstanceDiff
import ninja.actio.kdmm.dm.dmm.diff.MapDiff
import ninja.actio.kdmm.dm.objecttree.ObjectInstance
import ninja.actio.kdmm.dm.objecttree.ObjectTree
import ninja.actio.kdmm.dm.objecttree.ObjectTreeItem
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
                instance.refCount++
            map[loc] = key
        }
    }

    /**
     * Gets a key for a [TileInstance]. If the [TileInstance] is already in instances, it will return that one
     * If it is not, it will grab a new one from the unused keys pool
     *
     * @param tileInstance instance to get a key for
     * @return The resulting key for the instance
     */
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

    private fun generateKeys(set: MutableSet<String>, length: Int, prefix: String = "") {
        if (length <= 0) {
            set.add(prefix)
            return
        }
        for (char in ('a' until 'z'))
            generateKeys(set, length - 1, "$prefix$char")
        for (char in ('A' until 'Z'))
            generateKeys(set, length - 1, "$prefix$char")
    }

    /**
     * Expands the possible key pool.
     * This will likely result in a lot of merge conflicts and a huge diff, this should be kept to minimal usage
     */
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

    /**
     * Expands/shrinks the map
     */
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

        val tileInstance = TileInstance.fromString("${objectTree.defaultTurf}, ${objectTree.defaultArea}", objectTree)
        val defaultInstance = getKeyForInstance(tileInstance)

        val toRemove = mutableSetOf<Location>()
        for ((location, key) in map) {
            if (location.x in (minX..maxX) && location.y in (minY..maxY) && location.z in (minZ..maxZ)) continue
            instances[key]!!.refCount--
            toRemove.add(location)
        }
        for (location in toRemove) {
            map.remove(location)
        }
        for (x in (minX..maxX)) {
            for (y in (minY..maxY)) {
                for (z in (minZ..maxZ)) {
                    val location = Location(x, y ,z)
                    if (!map.containsKey(location))
                        putMap(location, defaultInstance)
                }
            }
        }
    }

    //These functions all make new TileInstances instead of modifying existing ones
    //These are here instead of in the TileInstance class because all of them require a DMM reference to work correctly
    //Encapsulation, testability, and all that jazz

    /**
     * Adds an object to an instance, then returns the new key of the resulting instance
     *
     * @param oldTileInstance A [TileInstance] to be modified
     * @param obj An [ObjectInstance] to be added to the tile instance
     * @return The new key of the resulting instance
     */
    fun addObjectToInstance(oldTileInstance: TileInstance, obj: ObjectInstance): String {
        val tileInstance = TileInstance(oldTileInstance.objs.toMutableList()) //cast is to copy instead of ref
        if (obj.isType("/area")) {
            for (lobj in tileInstance.objs) {
                if(lobj.isType("/area")) {
                    tileInstance.objs.remove(lobj) //Instances should only ever have one area
                }
            }
        }
        tileInstance.objs.add(obj)
        tileInstance.sortObjects()
        return getKeyForInstance(tileInstance)
    }


    /**
     * Removes an object from an instance, then returns the new key of the resulting instance
     *
     * @param oldTileInstance A [TileInstance] to be modified
     * @param obj An [ObjectInstance] to be removed from the tile instance
     * @param removeSubtypes If set to true, subtypes can also be targeted
     * @return the key of the resulting instance
     */
    fun removeObjectFromInstance(oldTileInstance: TileInstance, obj: ObjectInstance, removeSubtypes: Boolean = false): String {
        val tileInstance = TileInstance(oldTileInstance.objs.toMutableList()) //cast is to copy instead of ref
        val replacement = if (obj.isType("/area")) {
            objectTree.defaultArea
        } else if (obj.isType("/turf")) {
            var turfCount = 0
            for (instance in tileInstance.objs) {
                if(instance.isType("/turf"))
                    turfCount++
            }
            if (turfCount <= 1) objectTree.defaultTurf else ObjectTreeItem("")
        } else {
            ObjectTreeItem("")
        }
        if (removeSubtypes) {
            var toDel = ObjectInstance(ObjectTreeItem(""))
            for (objIn in tileInstance.objs) {
                if (objIn == obj || objIn.isType(obj.toStringDMM())) {
                    toDel = objIn
                    break
                }
            }
            if (toDel == ObjectInstance(ObjectTreeItem(""))) return getKeyForInstance(oldTileInstance)
            if (replacement != ObjectTreeItem(""))
                tileInstance.objs[tileInstance.objs.indexOf(toDel)] = replacement.instances[0]
            else
                tileInstance.objs.remove(toDel)
            return getKeyForInstance(tileInstance)
        } else {
            if (replacement != ObjectTreeItem(""))
                tileInstance.objs[tileInstance.objs.indexOf(obj)] = replacement.instances[0]
            else
                tileInstance.objs.remove(obj)
            return getKeyForInstance(tileInstance)
        }
    }

    /**
     * Moves an object to the top of an instance
     * @param oldTileInstance Instance to be modified
     * @param obj object to be moved to top
     * @return Resulting key for the new instance
     */
    fun moveObjectToTopOfInstance(oldTileInstance: TileInstance, obj: ObjectInstance): String {
        val tileInstance = TileInstance(oldTileInstance.objs.toMutableList())
        tileInstance.objs.remove(obj)
        tileInstance.objs.add(obj)
        tileInstance.sortObjects()
        return getKeyForInstance(tileInstance)
    }

    /**
     * Moves an object to the bottom of an instance
     * @param oldTileInstance Instance to be modified
     * @param obj object to be moved to top
     * @return Resulting key for the new instance
     */
    fun moveObjectToBottomOfInstance(oldTileInstance: TileInstance, obj: ObjectInstance): String {
        val tileInstance = TileInstance(oldTileInstance.objs.toMutableList())
        tileInstance.objs.remove(obj)
        tileInstance.objs.add(0, obj)
        tileInstance.sortObjects()
        return getKeyForInstance(tileInstance)
    }

    fun replaceObjectInInstance(oldTileInstance: TileInstance, oldObj: ObjectInstance, newObj: ObjectInstance): String {
        val tileInstance = TileInstance(oldTileInstance.objs.toMutableList())
        if (!tileInstance.objs.contains(oldObj))
            logger.error { "Tried to replace $oldObj, but it wasn't in the TileInstance" }
        tileInstance.objs[tileInstance.objs.indexOf(oldObj)] = newObj
        return getKeyForInstance(tileInstance)
    }
}
