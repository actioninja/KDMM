package ninja.actio.kdmm.test

import ninja.actio.kdmm.dm.objecttree.ObjectTree
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ObjectTreeTests {
    private val emptyTree = ObjectTree()

    @Test
    fun `Default Types Are Set Correctly`() {
        assertNotNull(emptyTree.get("/datum"))
        assertEquals("null", emptyTree.get("/datum").getVar("tag"))

        assertNotNull(emptyTree.get("/atom"))
        assertEquals("255", emptyTree.get("/atom").getVar("alpha"))
        assertEquals("0", emptyTree.get("/atom").getVar("appearance_flags"))
        assertEquals("0", emptyTree.get("/atom").getVar("blend_mode"))
        assertEquals("null", emptyTree.get("/atom").getVar("color"))
        assertEquals("0", emptyTree.get("/atom").getVar("density"))
        assertEquals("2", emptyTree.get("/atom").getVar("dir"))
        assertEquals("neuter", emptyTree.get("/atom").getVar("gender"))
        assertEquals("null", emptyTree.get("/atom").getVar("icon"))
        assertEquals("null", emptyTree.get("/atom").getVar("icon_state"))
        assertEquals("0", emptyTree.get("/atom").getVar("infra_luminosity"))
        assertEquals("0", emptyTree.get("/atom").getVar("invisibility"))
        assertEquals("1", emptyTree.get("/atom").getVar("layer"))
        assertEquals("0", emptyTree.get("/atom").getVar("luminosity"))
        assertEquals("null", emptyTree.get("/atom").getVar("maptext"))
        assertEquals("32", emptyTree.get("/atom").getVar("maptext_width"))
        assertEquals("32", emptyTree.get("/atom").getVar("maptext_height"))
        assertEquals("0", emptyTree.get("/atom").getVar("maptext_x"))
        assertEquals("0", emptyTree.get("/atom").getVar("maptext_y"))
        assertEquals("0", emptyTree.get("/atom").getVar("mouse_drag_pointer"))
        assertEquals("1", emptyTree.get("/atom").getVar("mouse_drop_pointer"))
        assertEquals("0", emptyTree.get("/atom").getVar("mouse_drop_zone"))
        assertEquals("1", emptyTree.get("/atom").getVar("mouse_opacity"))
        assertEquals("0", emptyTree.get("/atom").getVar("mouse_over_pointer"))
        assertEquals("null", emptyTree.get("/atom").getVar("name"))
        assertEquals("0", emptyTree.get("/atom").getVar("opacity"))
        assertEquals("list()", emptyTree.get("/atom").getVar("overlays"))
        assertEquals("0", emptyTree.get("/atom").getVar("override"))
        assertEquals("0", emptyTree.get("/atom").getVar("pixel_x"))
        assertEquals("0", emptyTree.get("/atom").getVar("pixel_y"))
        assertEquals("0", emptyTree.get("/atom").getVar("pixel_z"))
        assertEquals("0", emptyTree.get("/atom").getVar("plane"))
        assertEquals("null", emptyTree.get("/atom").getVar("suffix"))
        assertEquals("null", emptyTree.get("/atom").getVar("transform"))
        assertEquals("list()", emptyTree.get("/atom").getVar("underlays"))
        assertEquals("list()", emptyTree.get("/atom").getVar("verbs"))

    }
}