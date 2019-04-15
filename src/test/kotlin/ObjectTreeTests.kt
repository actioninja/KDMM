import ninja.actio.kdmm.dm.objecttree.ObjectTree
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ObjectTreeTests {
    private val emptyTree = ObjectTree()

    @Test
    fun `Default Types Are Set Correctly`() {
        assertNotNull(emptyTree.get("/datum"))
        assertEquals("null", emptyTree.get("/datum").getVarValue("tag"))

        assertNotNull(emptyTree.get("/atom"))
        assertEquals("255", emptyTree.get("/atom").getVarValue("alpha"))
        assertEquals("0", emptyTree.get("/atom").getVarValue("appearance_flags"))
        assertEquals("0", emptyTree.get("/atom").getVarValue("blend_mode"))
        assertEquals("null", emptyTree.get("/atom").getVarValue("color"))
        assertEquals("0", emptyTree.get("/atom").getVarValue("density"))
        assertEquals("2", emptyTree.get("/atom").getVarValue("dir"))
        assertEquals("neuter", emptyTree.get("/atom").getVarValue("gender"))
        assertEquals("null", emptyTree.get("/atom").getVarValue("icon"))
        assertEquals("null", emptyTree.get("/atom").getVarValue("icon_state"))
        assertEquals("0", emptyTree.get("/atom").getVarValue("infra_luminosity"))
        assertEquals("0", emptyTree.get("/atom").getVarValue("invisibility"))
        assertEquals("1", emptyTree.get("/atom").getVarValue("layer"))
        assertEquals("0", emptyTree.get("/atom").getVarValue("luminosity"))
        assertEquals("null", emptyTree.get("/atom").getVarValue("maptext"))
        assertEquals("32", emptyTree.get("/atom").getVarValue("maptext_width"))
        assertEquals("32", emptyTree.get("/atom").getVarValue("maptext_height"))
        assertEquals("0", emptyTree.get("/atom").getVarValue("maptext_x"))
        assertEquals("0", emptyTree.get("/atom").getVarValue("maptext_y"))
        assertEquals("0", emptyTree.get("/atom").getVarValue("mouse_drag_pointer"))
        assertEquals("1", emptyTree.get("/atom").getVarValue("mouse_drop_pointer"))
        assertEquals("0", emptyTree.get("/atom").getVarValue("mouse_drop_zone"))
        assertEquals("1", emptyTree.get("/atom").getVarValue("mouse_opacity"))
        assertEquals("0", emptyTree.get("/atom").getVarValue("mouse_over_pointer"))
        assertEquals("null", emptyTree.get("/atom").getVarValue("name"))
        assertEquals("0", emptyTree.get("/atom").getVarValue("opacity"))
        assertEquals("list()", emptyTree.get("/atom").getVarValue("overlays"))
        assertEquals("0", emptyTree.get("/atom").getVarValue("override"))
        assertEquals("0", emptyTree.get("/atom").getVarValue("pixel_x"))
        assertEquals("0", emptyTree.get("/atom").getVarValue("pixel_y"))
        assertEquals("0", emptyTree.get("/atom").getVarValue("pixel_z"))
        assertEquals("0", emptyTree.get("/atom").getVarValue("plane"))
        assertEquals("null", emptyTree.get("/atom").getVarValue("suffix"))
        assertEquals("null", emptyTree.get("/atom").getVarValue("transform"))
        assertEquals("list()", emptyTree.get("/atom").getVarValue("underlays"))
        assertEquals("list()", emptyTree.get("/atom").getVarValue("verbs"))

        assertNotNull(emptyTree.get("/atom/movable"))
    }
}