import ninja.actio.kdmm.dm.objecttree.ObjectTree
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ObjectTreeTests {
    private var testTree = ObjectTree()

    @Test
    fun `Default Types Are Set Correctly`() {
        assertNotNull(testTree.get("/datum"))
        assertEquals("null", testTree.get("/datum").getVarValue("tag"))

        assertNotNull(testTree.get("/atom"))
        assertEquals("255", testTree.get("/atom").getVarValue("alpha"))
        assertEquals("0", testTree.get("/atom").getVarValue("appearance_flags"))
        assertEquals("0", testTree.get("/atom").getVarValue("blend_mode"))
        assertEquals("null", testTree.get("/atom").getVarValue("color"))
        assertEquals("0", testTree.get("/atom").getVarValue("density"))
        assertEquals("2", testTree.get("/atom").getVarValue("dir"))
        assertEquals("neuter", testTree.get("/atom").getVarValue("gender"))
        assertEquals("null", testTree.get("/atom").getVarValue("icon"))
        assertEquals("null", testTree.get("/atom").getVarValue("icon_state"))
        assertEquals("0", testTree.get("/atom").getVarValue("infra_luminosity"))
        assertEquals("0", testTree.get("/atom").getVarValue("invisibility"))
        assertEquals("1", testTree.get("/atom").getVarValue("layer"))
        assertEquals("0", testTree.get("/atom").getVarValue("luminosity"))
        assertEquals("null", testTree.get("/atom").getVarValue("maptext"))
        assertEquals("32", testTree.get("/atom").getVarValue("maptext_width"))
        assertEquals("32", testTree.get("/atom").getVarValue("maptext_height"))
        assertEquals("0", testTree.get("/atom").getVarValue("maptext_x"))
        assertEquals("0", testTree.get("/atom").getVarValue("maptext_y"))
        assertEquals("0", testTree.get("/atom").getVarValue("mouse_drag_pointer"))
        assertEquals("1", testTree.get("/atom").getVarValue("mouse_drop_pointer"))
        assertEquals("0", testTree.get("/atom").getVarValue("mouse_drop_zone"))
        assertEquals("1", testTree.get("/atom").getVarValue("mouse_opacity"))
        assertEquals("0", testTree.get("/atom").getVarValue("mouse_over_pointer"))
        assertEquals("null", testTree.get("/atom").getVarValue("name"))
        assertEquals("0", testTree.get("/atom").getVarValue("opacity"))
        assertEquals("list()", testTree.get("/atom").getVarValue("overlays"))
        assertEquals("0", testTree.get("/atom").getVarValue("override"))
        assertEquals("0", testTree.get("/atom").getVarValue("pixel_x"))
        assertEquals("0", testTree.get("/atom").getVarValue("pixel_y"))
        assertEquals("0", testTree.get("/atom").getVarValue("pixel_z"))
        assertEquals("0", testTree.get("/atom").getVarValue("plane"))
        assertEquals("null", testTree.get("/atom").getVarValue("suffix"))
        assertEquals("null", testTree.get("/atom").getVarValue("transform"))
        assertEquals("list()", testTree.get("/atom").getVarValue("underlays"))
        assertEquals("list()", testTree.get("/atom").getVarValue("verbs"))

        assertNotNull(testTree.get("/atom/movable"))
        assertEquals("1", testTree.get("/atom/moveable").getVarValue("animate_movement"))
        assertEquals("0", testTree.get("/atom/moveable").getVarValue("bound_x"))
        assertEquals("0", testTree.get("/atom/moveable").getVarValue("bound_y"))
        assertEquals("32", testTree.get("/atom/moveable").getVarValue("bound_width"))
        assertEquals("32", testTree.get("/atom/moveable").getVarValue("bound_height"))
        assertEquals("0", testTree.get("/atom/moveable").getVarValue("glide_size"))
        assertEquals("null", testTree.get("/atom/moveable").getVarValue("screen_loc"))
        assertEquals("32", testTree.get("/atom/moveable").getVarValue("step_size"))
        assertEquals("0", testTree.get("/atom/moveable").getVarValue("step_x"))
        assertEquals("0", testTree.get("/atom/moveable").getVarValue("step_y"))

        assertNotNull(testTree.get("/area"))
        assertEquals("1", testTree.get("/area").getVarValue("layer"))
        assertEquals("1", testTree.get("/area").getVarValue("luminosity"))

        assertNotNull(testTree.get("/turf"))
        assertEquals("2", testTree.get("/turf").getVarValue("layer"))

        assertNotNull(testTree.get("/obj"))
        assertEquals("3", testTree.get("/obj").getVarValue("layer"))

        assertNotNull(testTree.get("/mob"))
        assertEquals("null", testTree.get("/mob").getVarValue("ckey"))
        assertEquals("1", testTree.get("/mob").getVarValue("density"))
        assertEquals("null", testTree.get("/mob").getVarValue("key"))
        assertEquals("4", testTree.get("/mob").getVarValue("layer"))
        assertEquals("2", testTree.get("/mob").getVarValue("see_in_dark"))
        assertEquals("0", testTree.get("/mob").getVarValue("see_infrared"))
        assertEquals("0", testTree.get("/mob").getVarValue("see_invisible"))
        assertEquals("0", testTree.get("/mob").getVarValue("sight"))

        assertNotNull(testTree.get("/world"))
        assertEquals("/turf", testTree.get("/world").getVarValue("turf"))
        assertEquals("/mob", testTree.get("/world").getVarValue("mob"))
        assertEquals("/area", testTree.get("/world").getVarValue("area"))
    }
}