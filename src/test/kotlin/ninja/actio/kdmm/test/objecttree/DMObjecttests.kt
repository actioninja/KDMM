package ninja.actio.kdmm.test.objecttree

import ninja.actio.kdmm.dm.objecttree.*
import org.junit.jupiter.api.Test
import java.awt.Color
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DMObjecttests {
    var testTree = ObjectTree()

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
    fun `Parse instance string with no vars`() {
        testTree = ObjectTree()
        val ogInstance = testTree.getOrCreate("/obj/test")

        val newInstance = InstanceFactory.parseStringToInstace(testTree, "/obj/test")
        assertEquals(ogInstance.toStringDM(), newInstance.toStringDM())
    }

    @Test
    fun `Parse instance string with vars`() {
        testTree = ObjectTree()
        val testObject = testTree.getOrCreate("/obj/test")
        testObject.setVar("test1", "test1")
        testObject.setVar("test2", "test2")

        val parsedInstance = InstanceFactory.parseStringToInstace(
            testTree,
            "/obj/test{test2 = \"OVERRIDDEN\"; test3 = \"test3\"; test4 = 9}"
        )
        assertEquals("test1", parsedInstance.getVarValue("test1"))
        assertEquals("OVERRIDDEN", parsedInstance.getVarValue("test2"))
        assertEquals("test3", parsedInstance.getVarValue("test3"))
        assertEquals("9", parsedInstance.getVarValue("test4"))
    }

    @Test
    fun `Parser rejects bad strings`() {
        testTree = ObjectTree()
        val badInstance = InstanceFactory.parseStringToInstace(testTree, "aaaaaa/{{")

        assertEquals(ObjectInstance(ObjectTreeItem(""), mutableMapOf()), badInstance)
    }

    @Test
    fun `getVar uses parents properly`() {
        val parent = ObjectTreeItem("/datum")
        val child = ObjectTreeItem("/datum/test", parent)

        parent.setVar("test1", "test1")
        child.setVar("test2", "test2")

        assertEquals("test1", parent.getVarValue("test1"))
        assertEquals("test1", child.getVarValue("test1"))
        assertEquals("test2", child.getVarValue("test2"))
    }

    @Test
    fun `isType works`() {
        val parent = ObjectTreeItem("/datum")
        val child = ObjectTreeItem("/datum/test", parent)

        assert(parent.isType("/datum"))
        assert(child.isType("/datum/test"))
        assert(child.isType("/datum"))
    }
}