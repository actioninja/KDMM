import ninja.actio.kdmm.dm.objecttree.*
import org.junit.jupiter.api.Test
import java.awt.Color
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ObjectTreeTests {
    private var testTree = ObjectTree()

    @Test
    fun `Default types are set correctly`() {
        testTree = ObjectTree()

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
        assertEquals("1", testTree.get("/atom/movable").getVarValue("animate_movement"))
        assertEquals("0", testTree.get("/atom/movable").getVarValue("bound_x"))
        assertEquals("0", testTree.get("/atom/movable").getVarValue("bound_y"))
        assertEquals("32", testTree.get("/atom/movable").getVarValue("bound_width"))
        assertEquals("32", testTree.get("/atom/movable").getVarValue("bound_height"))
        assertEquals("0", testTree.get("/atom/movable").getVarValue("glide_size"))
        assertEquals("null", testTree.get("/atom/movable").getVarValue("screen_loc"))
        assertEquals("32", testTree.get("/atom/movable").getVarValue("step_size"))
        assertEquals("0", testTree.get("/atom/movable").getVarValue("step_x"))
        assertEquals("0", testTree.get("/atom/movable").getVarValue("step_y"))

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

    @Test
    fun `Tree is structured correctly`() {
        testTree = ObjectTree()
        val testObject = testTree.getOrCreate("/obj/test")

        assertNotNull(testTree.get("/obj/test"))
        assertEquals(testTree.get("/obj"), testObject.parent)
    }

    @Test
    fun `getOrCreate recurses properly`() {
        testTree = ObjectTree()
        testTree.getOrCreate("/obj/test/test2/test3/test4")

        assertNotNull(testTree.get("/obj/test"))
        assertNotNull(testTree.get("/obj/test/test2"))
        assertNotNull(testTree.get("/obj/test/test2/test3"))
        assertNotNull(testTree.get("/obj/test/test2/test3/test4"))
    }

    @Test
    fun `deriveInstance inherits properly`() {
        testTree = ObjectTree()
        val testObject1 = testTree.getOrCreate("/obj/test")
        val testObjectDefaultInstance = testObject1.instances[0]
        val diffVars1 = mapOf(
            "test1" to DMVar("test1"),
            "test2" to DMVar("test2")
        )
        val instance2 = InstanceFactory.deriveFrom(testObjectDefaultInstance, diffVars1)
        assertEquals("test1", instance2.getVarValue("test1"))
        assertEquals("test2", instance2.getVarValue("test2"))
        val diffVars2 = mapOf(
            "test2" to DMVar("OVERRIDDEN"),
            "test3" to DMVar("test3")
        )
        val instance3 = InstanceFactory.deriveFrom(instance2, diffVars2)
        assertEquals("test1", instance3.getVarValue("test1"))
        assertEquals("OVERRIDDEN", instance3.getVarValue("test2"))
        assertEquals("test3", instance3.getVarValue("test3"))
    }

    @Test
    fun `Color decodes properly`() {
        var testObject = ObjectTreeItem("/test")
        testObject.setVar("color", "#DFDFDF")
        assertEquals(Color.decode("#DFDFDF"), testObject.dmColor)
        testObject = ObjectTreeItem("/test")
        testObject.setVar("color", "rgb (80, 90, 100)")
        assertEquals(Color(80, 90, 100), testObject.dmColor)
        val namedColorList = mapOf(
            "black" to Color.decode("#000000"),
            "silver" to Color.decode("#C0C0C0"),
            "gray" to Color.decode("#808080"),
            "grey" to Color.decode("#808080"),
            "white" to Color.decode("#FFFFFF"),
            "maroon" to Color.decode("#800000"),
            "red" to Color.decode("#FF0000"),
            "purple" to Color.decode("#800080"),
            "fuchsia" to Color.decode("#FF00FF"),
            "magenta" to Color.decode("#FF00FF"),
            "green" to Color.decode("#00C000"),
            "lime" to Color.decode("#00FF00"),
            "olive" to Color.decode("#808000"),
            "gold" to Color.decode("#808000"),
            "yellow" to Color.decode("#FFFF00"),
            "navy" to Color.decode("#000080"),
            "blue" to Color.decode("#0000FF"),
            "teal" to Color.decode("#008080"),
            "aqua" to Color.decode("#00FFFF"),
            "cyan" to Color.decode("#00FFFF")
        )
        for ((colorName, color) in namedColorList) {
            testObject = ObjectTreeItem("/test")
            testObject.setVar("color", colorName)
            assertEquals(color, testObject.dmColor)
        }
    }

    @Test
    fun `Parse no var instance string`() {
        testTree = ObjectTree()
        val ogInstance = testTree.getOrCreate("/obj/test")

        val newInstance = InstanceFactory.parseStringToInstace(testTree, "/obj/test")
        assertEquals(ogInstance.toStringDM(), newInstance.toStringDM())
    }

    @Test
    fun `Parse var instance string`() {
        testTree = ObjectTree()
        val testObject = testTree.getOrCreate("/obj/test")
        testObject.setVar("test1", "test1")
        testObject.setVar("test2", "test2")

        val parsedInstance = InstanceFactory.parseStringToInstace(
            testTree,
            "/obj/test{test2 = \"OVERRIDDEN\"; test3 = \"test3\"}"
        )
        assertEquals("test1", parsedInstance.getVarValue("test1"))
        assertEquals("OVERRIDDEN", parsedInstance.getVarValue("test2"))
        assertEquals("test3", parsedInstance.getVarValue("test3"))
    }

    @Test
    fun `Parser rejects bad strings`() {
        testTree = ObjectTree()
        val badInstance = InstanceFactory.parseStringToInstace(testTree, "aaaaaa/{{")

        assertEquals(ObjectInstance(ObjectTreeItem(""), mutableMapOf()), badInstance)
    }
}