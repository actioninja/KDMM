package ninja.actio.kdmm.test.dmm

import ninja.actio.kdmm.dm.*
import ninja.actio.kdmm.dm.dmm.Location
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class LocationTests {
    @Test
    fun `Location Basic Test`() {
        val location = Location(0, 0 ,0)
        location.set(1, 1, 1)
        assertEquals(Location(1, 1, 1), location)
    }

    @Test
    fun `Location step test`() {
        val location = Location(0, 0,0)
        //EVERY POSSIBLE STEP
        val northStep = location.getStep(NORTH)
        val southStep = location.getStep(SOUTH)
        val eastStep = location.getStep(EAST)
        val westStep = location.getStep(WEST)
        val northWestStep = location.getStep(NORTHWEST)
        val northEastStep = location.getStep(NORTHEAST)
        val southWestStep = location.getStep(SOUTHWEST)
        val southEastStep = location.getStep(SOUTHEAST)
        val upStep = location.getStep(UP)
        val upNorthStep = location.getStep(UP or NORTH)
        val upSouthStep = location.getStep(UP or SOUTH)
        val upEastStep = location.getStep(UP or EAST)
        val upWestStep = location.getStep(UP or WEST)
        val upNorthWestStep = location.getStep(UP or NORTHWEST)
        val upNorthEastStep = location.getStep(UP or NORTHEAST)
        val upSouthWestStep = location.getStep(UP or SOUTHWEST)
        val upSouthEastStep = location.getStep(UP or SOUTHEAST)
        val downStep = location.getStep(DOWN)
        val downNorthStep = location.getStep(DOWN or NORTH)
        val downSouthStep = location.getStep(DOWN or SOUTH)
        val downEastStep = location.getStep(DOWN or EAST)
        val downWestStep = location.getStep(DOWN or WEST)
        val downNorthWestStep = location.getStep(DOWN or NORTHWEST)
        val downNorthEastStep = location.getStep(DOWN or NORTHEAST)
        val downSouthEastStep = location.getStep(DOWN or SOUTHEAST)
        val downSouthWestStep = location.getStep(DOWN or SOUTHWEST)
        assertEquals(Location(0, 1, 0), northStep)
        assertEquals(Location(0, -1, 0), southStep)
        assertEquals(Location(1, 0, 0), eastStep)
        assertEquals(Location(-1, 0, 0), westStep)
        assertEquals(Location(-1, 1, 0), northWestStep)
        assertEquals(Location(1, 1, 0), northEastStep)
        assertEquals(Location(1, -1, 0), southEastStep)
        assertEquals(Location(-1, -1, 0), southWestStep)
        assertEquals(Location(0, 0, 1), upStep)
        assertEquals(Location(0, 1, 1), upNorthStep)
        assertEquals(Location(0, -1, 1), upSouthStep)
        assertEquals(Location(1, 0, 1), upEastStep)
        assertEquals(Location(-1, 0, 1), upWestStep)
        assertEquals(Location(1, 1, 1), upNorthEastStep)
        assertEquals(Location(-1, 1, 1), upNorthWestStep)
        assertEquals(Location(1, -1, 1), upSouthEastStep)
        assertEquals(Location(-1, -1, 1), upSouthWestStep)
        assertEquals(Location(0, 0, -1), downStep)
        assertEquals(Location(0, 1, -1), downNorthStep)
        assertEquals(Location(0, -1, -1), downSouthStep)
        assertEquals(Location(1, 0, -1), downEastStep)
        assertEquals(Location(-1, 0, -1), downWestStep)
        assertEquals(Location(-1, 1, -1), downNorthWestStep)
        assertEquals(Location(1, 1, -1), downNorthEastStep)
        assertEquals(Location(1, -1, -1), downSouthEastStep)
        assertEquals(Location(-1, -1, -1), downSouthWestStep)
    }
}