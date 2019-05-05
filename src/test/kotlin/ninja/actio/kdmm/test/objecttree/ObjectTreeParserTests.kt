package ninja.actio.kdmm.test.objecttree

import ninja.actio.kdmm.dm.objecttree.ObjectTreeParser
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ObjectTreeParserTests {
    private val classLoader = javaClass.classLoader
    private val genericParser = ObjectTreeParser()


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
        val parameters = "test1, test2"
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
            "PARAMETERIZED",
            "(THIS IS A REPLACEMENT WITH PARAMETERS: {{{0}}}, ##{{{1}}}, {{{2}}})"
        )
        val preSub =
            "var/not_a_macro = NO_PARAMETER; var/not_a_macro_2 = PARAMETERIZED(\"param1\", \"param2\", \"param3\")"
        val expected =
            "var/not_a_macro = (THIS IS A REPLACEMENT); var/not_a_macro_2 = (THIS IS A REPLACEMENT WITH PARAMETERS:  \"param1\" , \"param2\",  \"param3\" )"
        val result = parserWithMacros.macroSubstititue(preSub)
        assertEquals(expected, result)
    }

    @Test
    fun `Parser Minimal Test`() {
        val unitTestParser = ObjectTreeParser()
        val testList
        unitTestParser.parse()
    }
}