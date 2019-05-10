package ninja.actio.kdmm.test.objecttree

import ninja.actio.kdmm.dm.getFileInternal
import ninja.actio.kdmm.dm.objecttree.ObjectTree
import ninja.actio.kdmm.dm.objecttree.ObjectTreeParser
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals

class ObjectTreeParserTests {
    private val classLoader = javaClass.classLoader
    private val genericParser = ObjectTreeParser()

    val expectedstddefTree = ObjectTree()


    private fun initializeExpectedStddefs() {
        val glob = expectedstddefTree.global
        glob.setVar("const")
        glob.setVar("NORTH", 1)
        glob.setVar("SOUTH", 2)
        glob.setVar("EAST", 4)
        glob.setVar("WEST", 8)
        glob.setVar("NORTHEAST", 5)
        glob.setVar("NORTHWEST", 9)
        glob.setVar("SOUTHEAST", 6)
        glob.setVar("SOUTHWEST", 10)
        glob.setVar("UP", 16)
        glob.setVar("DOWN", 32)
        glob.setVar("BLIND", 1)
        glob.setVar("SEE_MOBS", 4)
        glob.setVar("SEE_OBJS", 8)
        glob.setVar("SEE_TURFS", 16)
        glob.setVar("SEE_SELF", 32)
        glob.setVar("SEE_INFRA", 64)
        glob.setVar("SEE_PIXELS", 256)
        glob.setVar("SEE_THRU", 512)
        glob.setVar("SEE_BLACKNESS", 1024)
        glob.setVar("MOB_PERSPECTIVE", 0)
        glob.setVar("EYE_PERSPECTIVE", 1)
        glob.setVar("EDGE_PERSPECTIVE", 2)
        glob.setVar("FLOAT_LAYER", -1)
        glob.setVar("AREA_LAYER", 1)
        glob.setVar("TURF_LAYER", 2)
        glob.setVar("OBJ_LAYER", 3)
        glob.setVar("MOB_LAYER", 4)
        glob.setVar("FLY_LAYER", 5)
        glob.setVar("EFFECTS_LAYER", 5000)
        glob.setVar("TOPDOWN_LAYER", 10000)
        glob.setVar("BACKGROUND_LAYER", 20000)
        glob.setVar("FLOAT_PLANE", -32767)
        glob.setVar("TOPDOWN_MAP", 0)
        glob.setVar("ISOMETRIC_MAP", 1)
        glob.setVar("SIDE_MAP", 2)
        glob.setVar("TILED_ICON_MAP", 32768)
        glob.setVar("TRUE", 1)
        glob.setVar("FALSE", 0)
        glob.setVar("MALE", "male")
        glob.setVar("FEMALE", "female")
        glob.setVar("NEUTER", "neuter")
        glob.setVar("PLURAL", "plural")
        glob.setVar("MOUSE_INACTIVE_POINTER", 0)
        glob.setVar("MOUSE_ACTIVE_POINTER", 1)
        glob.setVar("MOUSE_DRAG_POINTER", 3)
        glob.setVar("MOUSE_DROP_POINTER", 4)
        glob.setVar("MOUSE_ARROW_POINTER", 5)
        glob.setVar("MOUSE_CROSSHAIRS_POINTER", 6)
        glob.setVar("MOUSE_HAND_POINTER", 7)
        glob.setVar("MOUSE_LEFT_BUTTON", 1)
        glob.setVar("MOUSE_RIGHT_BUTTON", 2)
        glob.setVar("MOUSE_MIDDLE_BUTTON", 4)
        glob.setVar("MOUSE_CTRL_KEY", 8)
        glob.setVar("MOUSE_SHIFT_KEY", 16)
        glob.setVar("MOUSE_ALT_KEY", 32)
        glob.setVar("MS_WINDOWS", "MS Windows")
        glob.setVar("UNIX", "UNIX")
        glob.setVar("BLEND_DEFAULT", 0)
        glob.setVar("BLEND_OVERLAY", 1)
        glob.setVar("BLEND_ADD", 2)
        glob.setVar("BLEND_SUBTRACT", 3)
        glob.setVar("BLEND_MULTIPLY", 4)
        glob.setVar("SOUND_MUTE", 1)
        glob.setVar("SOUND_PAUSED", 2)
        glob.setVar("SOUND_STREAM", 4)
        glob.setVar("SOUND_UPDATE", 16)
        val sound = expectedstddefTree.getOrCreate("/sound")
        sound.setVar("file")
        sound.setVar("repeat")
        sound.setVar("wait")
        sound.setVar("channel")
        sound.setVar("frequency", 0)
        sound.setVar("pan", 0)
        sound.setVar("volume", 100)
        sound.setVar("priority", 0)
        sound.setVar("status", 0)
        sound.setVar("environment", -1)
        sound.setVar("echo")
        sound.setVar("x", 0)
        sound.setVar("y", 0)
        sound.setVar("z", 0)
        sound.setVar("falloff", 1)
        val icon = expectedstddefTree.getOrCreate("/icon")
        icon.setVar("icon")
        val matrix = expectedstddefTree.getOrCreate("/matrix")
        matrix.setVar("a", 1)
        matrix.setVar("b", 0)
        matrix.setVar("c", 0)
        matrix.setVar("d", 0)
        matrix.setVar("e", 1)
        matrix.setVar("f", 0)
        val database = expectedstddefTree.getOrCreate("/database")
        database.setVar("_binobj")
        val databaseQuery = expectedstddefTree.getOrCreate("/database/query")
        databaseQuery.setVar("database")
        val exception = expectedstddefTree.getOrCreate("/exception")
        exception.setVar("desc")
        exception.setVar("file")
        exception.setVar("line")
        exception.setVar("name")
        val regex = expectedstddefTree.getOrCreate("/regex")
        regex.setVar("_binobj")
        regex.setVar("flags")
        regex.setVar("group")
        regex.setVar("index")
        regex.setVar("match")
        regex.setVar("name")
        regex.setVar("next")
        regex.setVar("text")
        val mutableAppearance = expectedstddefTree.getOrCreate("/mutable_appearance")
    }

    @Test
    fun `Clean and Listize Test`() {
        val toBeCleanedStream = classLoader.getResourceAsStream("clean_and_listize_test.txt")
        val toBeCleanedSpacesStream = classLoader.getResourceAsStream("clean_and_listize_test_spaces.txt")

        val expectedCleanedAndListized = listOf(
            "/obj/effect/abstract/ripple",
            " name = \"hyperspace ripple\"",
            " desc = \"Something is coming through hyperspace, you can see the visual disturbances. It's probably best not to be on top of these when whatever is tunneling comes through.\"",
            " icon = 'icons/effects/effects.dmi'",
            " icon_state = \"medi_holo\"",
            " anchored = TRUE",
            " density = FALSE",
            " layer = RIPPLE_LAYER",
            " mouse_opacity = MOUSE_OPACITY_ICON",
            " alpha = 0",
            "/obj/effect/abstract/ripple/Initialize(mapload, time_left)",
            " . = ..()",
            " animate(src, alpha=255, time=time_left)",
            " addtimer(CALLBACK(src, .proc/stop_animation), 8, TIMER_CLIENT_TIME)",
            "/obj/effect/abstract/ripple/proc/stop_animation()",
            " icon_state = \"medi_holo_no_anim\""
        )

        val cleaned = genericParser.cleanAndListize(toBeCleanedStream)
        assertEquals(expectedCleanedAndListized, cleaned)
        val cleanedSpaces = genericParser.cleanAndListize(toBeCleanedSpacesStream)
        assertEquals(expectedCleanedAndListized, cleanedSpaces)
    }

    @Test
    fun `Macro Parameter Resolution`() {
        //First test
        val parameters = listOf("test1", "test2")
        val content = "({{{0}}} + asdfasdf + ##{{{1}}})"
        val expectedResult = "( test1  + asdfasdf + test2)"
        val result = genericParser.macroParameterResolve(parameters, content)
        assertEquals(expectedResult, result)
    }

    @Test
    fun `Macro Substitution`() {
        val parserWithMacros = ObjectTreeParser()
        parserWithMacros.addMacro("NO_PARAMETER", "(THIS IS A REPLACEMENT)")
        parserWithMacros.addMacro(
            "^~\$#%PARAMETERIZED",
            "(THIS IS A REPLACEMENT WITH PARAMETERS: {{{0}}}, ##{{{1}}}, {{{2}}})"
        )
        val preSub =
            "var/not_a_macro = NO_PARAMETER; var/not_a_macro_2 = PARAMETERIZED(\"param1\", \"param2\", \"param3\")"
        val expected =
            "var/not_a_macro = (THIS IS A REPLACEMENT); var/not_a_macro_2 = (THIS IS A REPLACEMENT WITH PARAMETERS:  \"param1\" , \"param2\",  \"param3\" )"
        val result = parserWithMacros.macroSubstitute(preSub)
        assertEquals(expected, result)
    }

    @Test
    fun `stddef Parse Test`() {
        val stddefParser = ObjectTreeParser()
        stddefParser.subParse(getFileInternal("stddef.dm"))
        initializeExpectedStddefs()
        assertEquals(expectedstddefTree, stddefParser.objectTree)
    }

    //Minimal environment is basically just a "things are in fact parsing without runtiming test"
    @Test
    fun `Parser Minimal Test`() {
        val minimalTestParser = ObjectTreeParser()
        val minimalDME = classLoader.getResource("environments/minimal/minimal.dme")
        initializeExpectedStddefs()
        val expectedObjectTree = expectedstddefTree
        val testItem = expectedObjectTree.getOrCreate("/obj/test")
        testItem.setVar("test_var", 10)
        testItem.setVar("test_text", "blah")
        minimalTestParser.parseDME(File(minimalDME.path))
        assertEquals(expectedObjectTree, minimalTestParser.objectTree)
    }

    //Basic environment is "this has more complicated of things to parse, but still isn't a full environment"
    //It uses some more complicated cases from tgcode to see if the parser handles them correctly
    @Test
    fun `Parser Basic Test`() {
        val basicTestParser = ObjectTreeParser()
        val basicDME = classLoader.getResource("environments/basic/basic.dme")
        initializeExpectedStddefs()
        val expectedObjectTree = expectedstddefTree
        val testItem = expectedObjectTree.getOrCreate("/obj/test")
        testItem.setVar("test_var", 10)
        testItem.setVar("test_text", "blah")
        fun helperPartial(type: String, iconBase: String, color: String) {
            val base = expectedObjectTree.getOrCreate(type)
            base.setVar("pipe_color", color)
            base.setVar("color", color)
            val visible = expectedObjectTree.getOrCreate("$type/visible")
            visible.setVar("level", "PIPE_VISIBLE_LEVEL")
            visible.setVar("layer", "GAS_PIPE_VISIBLE_LAYER")
            val visible1 = expectedObjectTree.getOrCreate("$type/visible/layer1")
            visible1.setVar("piping_layer", 1)
            visible1.setVar("icon_state", "$iconBase-1")
            val visible3 = expectedObjectTree.getOrCreate("$type/visible/layer3")
            visible3.setVar("piping_layer", 3)
            visible3.setVar("icon_state", "$iconBase-3")
            val hidden = expectedObjectTree.getOrCreate("$type/hidden")
            hidden.setVar("level", "PIPE_HIDDEN_LEVEL")
            val hidden1 = expectedObjectTree.getOrCreate("$type/hidden/layer1")
            hidden1.setVar("piping_layer", 1)
            hidden1.setVar("icon_state", "$iconBase-1")
            val hidden3 = expectedObjectTree.getOrCreate("$type/hidden/layer3")
            hidden3.setVar("piping_layer", 3)
            hidden3.setVar("icon_state", "$iconBase-3")
        }
        fun helperNamed(type: String, iconBase: String, name: String, color: String) {
            helperPartial(type, iconBase, color)
            val typeInTree = expectedObjectTree.getOrCreate(type)
            typeInTree.setVar("name", name)
        }
        val unnamed = mapOf(
            "general" to "null",
            "yellow" to "rgb(255, 198, 0)",
            "cyan" to "rgb(0, 255, 249)",
            "green" to "rgb(30, 255, 0)",
            "orange" to "rgb(255, 129, 25)",
            "purple" to "rgb(128, 0, 182)",
            "dark" to "rgb(69, 69, 69)",
            "brown" to "rgb(178, 100, 56)",
            "violet" to "rgb(64, 0, 128)"
        )
        val named = mapOf(
            "scrubbers" to Pair("scrubbers pipe", "rgb(255, 0, 0)"),
            "supply" to Pair("air supply pipe", "rgb(0, 0, 255)"),
            "supplymain" to Pair("main air supply pipe", "rgb(130, 43, 255)")
        )
        for ((type, color) in unnamed) {
            helperPartial("/obj/machinery/atmospherics/pipe/simple/$type", "pipe11", color)
            helperPartial("/obj/machinery/atmospherics/pipe/manifold/$type", "manifold", color)
            helperPartial("/obj/machinery/atmospherics/pipe/manifold4w/$type", "manifold4w", color)
        }

        for ((type, pair) in named) {
            helperNamed("/obj/machinery/atmospherics/pipe/simple/$type", "pipe11", pair.first, pair.second)
            helperNamed("/obj/machinery/atmospherics/pipe/manifold/$type", "manifold", pair.first, pair.second)
            helperNamed("/obj/machinery/atmospherics/pipe/manifold4w/$type", "manifold4w", pair.first, pair.second)
        }
        basicTestParser.parseDME(File(basicDME.path))
        //string being compared to make the unit test result nicer
        assertEquals(expectedObjectTree.toString(), basicTestParser.objectTree.toString())
    }

    //Live TG is what it sounds like, it's a live version of a tgcode repo. Since it's too large to easily know what the
    //result should be, this is a pure unit test
    @Test
    fun `Parser TG Live Test`() {
        val tgLiveTestParser = ObjectTreeParser()
        val liveDME = classLoader.getResource("environments/livetg/tgstation.dme")
        tgLiveTestParser.parseDME(File(liveDME.path))
    }
}