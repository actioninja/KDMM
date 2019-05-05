package ninja.actio.kdmm.dm.objecttree

import java.io.PrintStream
import java.nio.file.Path
import java.nio.file.Paths


class ObjectTree {
    val items = mutableMapOf<String, ObjectTreeItem>()
    var dmePath = ""

    val fileDirs = mutableListOf<Path>()

    var iconSize = 32

    init {
        //global
        val global = ObjectTreeItem("")
        addItem(global)

        //default datums

        val datum = ObjectTreeItem("/datum")
        datum.setVar("tag", "null")
        addItem(datum)

        val atom = ObjectTreeItem("/atom", datum)
        atom.setVar("alpha", "255")
        atom.setVar("appearance_flags", "0")
        atom.setVar("blend_mode", "0")
        atom.setVar("color", "null")
        atom.setVar("density", "0")
        atom.setVar("desc", "null")
        atom.setVar("dir", "2")
        atom.setVar("gender", "neuter")
        atom.setVar("icon", "null")
        atom.setVar("icon_state", "null")
        atom.setVar("infra_luminosity", "0")
        atom.setVar("invisibility", "0")
        atom.setVar("layer", "1")
        atom.setVar("luminosity", "0")
        atom.setVar("maptext", "null")
        atom.setVar("maptext_width", "32")
        atom.setVar("maptext_height", "32")
        atom.setVar("maptext_x", "0")
        atom.setVar("maptext_y", "0")
        atom.setVar("mouse_drag_pointer", "0")
        atom.setVar("mouse_drop_pointer", "1")
        atom.setVar("mouse_drop_zone", "0")
        atom.setVar("mouse_opacity", "1")
        atom.setVar("mouse_over_pointer", "0")
        atom.setVar("name", "null")
        atom.setVar("opacity", "0")
        atom.setVar("overlays", "list()")
        atom.setVar("override", "0")
        atom.setVar("pixel_x", "0")
        atom.setVar("pixel_y", "0")
        atom.setVar("pixel_z", "0")
        atom.setVar("plane", "0")
        atom.setVar("suffix", "null")
        atom.setVar("transform", "null")
        atom.setVar("underlays", "list()")
        atom.setVar("verbs", "list()")
        addItem(atom)

        val movable = ObjectTreeItem("/atom/movable", atom)
        movable.setVar("animate_movement", "1")
        movable.setVar("bound_x", "0")
        movable.setVar("bound_y", "0")
        movable.setVar("bound_width", "32")
        movable.setVar("bound_height", "32")
        movable.setVar("glide_size", "0")
        movable.setVar("screen_loc", "null")
        movable.setVar("step_size", "32")
        movable.setVar("step_x", "0")
        movable.setVar("step_y", "0")
        addItem(movable)

        val area = ObjectTreeItem("/area", atom)
        area.setVar("layer", "1")
        area.setVar("luminosity", "1")
        addItem(area)

        val turf = ObjectTreeItem("/turf", atom)
        turf.setVar("layer", "2")
        addItem(turf)

        val obj = ObjectTreeItem("/obj", movable)
        obj.setVar("layer", "3")
        addItem(obj)

        val mob = ObjectTreeItem("/mob", movable)
        mob.setVar("ckey", "null")
        mob.setVar("density", "1")
        mob.setVar("key", "null")
        mob.setVar("layer", "4")
        mob.setVar("see_in_dark", "2")
        mob.setVar("see_infrared", "0")
        mob.setVar("see_invisible", "0")
        mob.setVar("sight", "0")
        addItem(mob)

        val world = ObjectTreeItem("/world", datum)
        world.setVar("turf", "/turf")
        world.setVar("mob", "/mob")
        world.setVar("area", "/area")
        addItem(world)

        // Empty path, this will be resolved as project root by filePath.
        fileDirs.add(Paths.get(""))
    }

    fun get(path: String): ObjectTreeItem {
        return items[path]!! //TODO: make null safe
    }

    fun getOrCreate(path: String): ObjectTreeItem {
        if (items.containsKey(path))
            return items[path]!!

        val parentPath = if (path.indexOf("/") != path.lastIndexOf("/"))
            path.substring(0, path.lastIndexOf("/"))
        else
            "/datum"

        val parentItem = getOrCreate(parentPath)
        val item = ObjectTreeItem(path, parentItem)
        items[path] = item
        return item
    }

    fun addItem(item: ObjectTreeItem) {
        items[item.path] = item
    }

    override fun equals(other: Any?): Boolean {
        if(other !is ObjectTree)
            return false
        if(other === this)
            return true
        if(toString() == other.toString())
            return true
        return false
    }

    override fun toString(): String {
        val builder = StringBuilder()
        //filter out stuff that's the same every time because it's not useful to print it every time
        val filteredMap = mutableMapOf<String, ObjectTreeItem>()
        filteredMap.putAll(items)
        filteredMap.remove("/datum")
        filteredMap.remove("/atom")
        filteredMap.remove("/atom/movable")
        filteredMap.remove("/area")
        filteredMap.remove("/turf")
        filteredMap.remove("/obj")
        filteredMap.remove("/mob")
        filteredMap.remove("/world")
        val sorted = filteredMap.toSortedMap()
        builder.append("Object Tree:{\n")
        for ((path, obj) in sorted) {
            builder.append("    $path {\n")
            val sortedVars = obj.vars.toSortedMap()
            for((key, dmvar) in sortedVars) {
                builder.append("        $key = ${dmvar.value},\n")
            }
            builder.append("    }\n")
        }
        builder.append("\n}")
        return builder.toString()
    }

    fun dumpTree(stream: PrintStream) {
        for (item in items.values) {
            stream.println(item.path)
            for (dmVar in item.vars.entries.toSet()) {
                stream.println("\t${dmVar.key} = ${dmVar.value}")
            }
        }
    }

    val global: ObjectTreeItem
        get() = items[""]!!

    /*
    fun completeTree() {
        for(item in items.values) {
            item.subtypes.clear()

            for(entry in item.vars.entries.toSet()) {
                var value = entry.value
                var oldValue = ""
                try {
                    while(oldValue != value) {
                        oldValue = value
                        val matcher = Pattern.compile()
                    }
                }
            }
        }
    }
    */

}