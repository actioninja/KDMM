package ninja.actio.kdmm.test.objecttree

import ninja.actio.kdmm.dm.objecttree.ObjectTreeParser
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ObjectTreeParserTests {

    private val classLoader = javaClass.classLoader
    private val toBeCleanedStream = classLoader.getResourceAsStream("clean_and_listize_test.txt")
    private val toBeCleanedSpacesStream = classLoader.getResourceAsStream("clean_and_listize_test_spaces.txt")
    private val genericParser = ObjectTreeParser()

    private val expectedCleanedAndListized = listOf(
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

    @Test
    fun `Clean and Listize Test`() {
        val cleaned = genericParser.cleanAndListize(toBeCleanedStream)
        assertEquals(expectedCleanedAndListized, cleaned)
        val cleanedSpaces = genericParser.cleanAndListize(toBeCleanedSpacesStream)
        assertEquals(expectedCleanedAndListized, cleanedSpaces)
    }
}