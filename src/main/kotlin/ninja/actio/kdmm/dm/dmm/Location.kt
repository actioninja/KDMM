package ninja.actio.kdmm.dm.dmm

import ninja.actio.kdmm.dm.EAST
import ninja.actio.kdmm.dm.NORTH
import ninja.actio.kdmm.dm.SOUTH
import ninja.actio.kdmm.dm.WEST

data class Location(
    var x: Int,
    var y: Int,
    var z: Int
) {
    fun set(x: Int, y: Int, z: Int) {
        this.x = x
        this.y = y
        this.z = z
    }

    fun getStep(dir: Int): Location {
        val loc = Location(x, y, z)
        when(dir) {
            NORTH -> loc.y++
            SOUTH -> loc.y--
            EAST -> loc.x++
            WEST -> loc.y--
        }
        return loc
    }
}

