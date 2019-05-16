package ninja.actio.kdmm.dm.dmm

import ninja.actio.kdmm.dm.*

/**
 * Represents a position in a DMM file
 */
data class Location(
    var x: Int,
    var y: Int,
    var z: Int
) {
    /**
     * Updates the location with the given coordinates
     */
    fun set(x: Int, y: Int, z: Int) {
        this.x = x
        this.y = y
        this.z = z
    }

    /**
     * Takes a step in the given direction, then returns the Location from the new coordinates
     * @param dir The direction to step into.
     * @return The new location.
     */
    fun getStep(dir: Int): Location {
        val loc = Location(x, y, z)
        if ((dir and NORTH) != 0)
            loc.y++
        if ((dir and SOUTH) != 0)
            loc.y--
        if ((dir and EAST) != 0)
            loc.x++
        if ((dir and WEST) != 0)
            loc.x--
        if ((dir and UP) != 0)
            loc.z++
        if ((dir and DOWN) != 0)
            loc.z--
        return loc
    }
}

