package dev.mskelton.versly.persistence

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class PassageIdCodecTest {
    // region encodePassageId

    @Test
    fun encode_noRange_usesWildcard() {
        val id = PassageId(book = "GEN", chapter = "1", translation = "ESV")
        assertEquals("GEN.1.*.ESV", encodePassageId(id))
    }

    @Test
    fun encode_withRange_joinsWithDash() {
        val id = PassageId(book = "ROM", chapter = "3", translation = "NIV", range = listOf("23", "28"))
        assertEquals("ROM.3.23-28.NIV", encodePassageId(id))
    }

    @Test
    fun encode_preservesBookAndChapter() {
        val id = PassageId(book = "REV", chapter = "22", translation = "KJV")
        val encoded = encodePassageId(id)
        assert(encoded.startsWith("REV.22.")) { "Expected encoded to start with REV.22., got: $encoded" }
    }

    // endregion

    // region decodePassageId

    @Test
    fun decode_noRange_returnsNullRange() {
        val decoded = decodePassageId("GEN.1.*.ESV")
        assertNotNull(decoded)
        assertEquals("GEN", decoded!!.book)
        assertEquals("1", decoded.chapter)
        assertEquals("ESV", decoded.translation)
        assertNull(decoded.range)
    }

    @Test
    fun decode_withRange_parsesRange() {
        val decoded = decodePassageId("ROM.3.23-28.NIV")
        assertNotNull(decoded)
        assertEquals("ROM", decoded!!.book)
        assertEquals("3", decoded.chapter)
        assertEquals("NIV", decoded.translation)
        assertEquals(listOf("23", "28"), decoded.range)
    }

    @Test
    fun decode_tooFewParts_returnsNull() {
        assertNull(decodePassageId("GEN.1.ESV"))
    }

    @Test
    fun decode_tooManyParts_returnsNull() {
        assertNull(decodePassageId("GEN.1.*.ESV.extra"))
    }

    @Test
    fun decode_emptyString_returnsNull() {
        assertNull(decodePassageId(""))
    }

    @Test
    fun decode_singlePartRange_treatsRangeAsNull() {
        // "5" split by "-" yields ["5"] (size 1, not 2), so range is null
        val decoded = decodePassageId("GEN.1.5.ESV")
        assertNotNull(decoded)
        assertNull(decoded!!.range)
    }

    // endregion

    // region round-trip

    @Test
    fun roundTrip_noRange_preservesAllFields() {
        val original = PassageId(book = "PSA", chapter = "23", translation = "KJV")
        assertEquals(original, decodePassageId(encodePassageId(original)))
    }

    @Test
    fun roundTrip_withRange_preservesAllFields() {
        val original = PassageId(book = "MAT", chapter = "5", translation = "ESV", range = listOf("1", "12"))
        assertEquals(original, decodePassageId(encodePassageId(original)))
    }

    @Test
    fun roundTrip_multiDigitChapter_preservesChapter() {
        val original = PassageId(book = "PSA", chapter = "119", translation = "ESV")
        assertEquals(original, decodePassageId(encodePassageId(original)))
    }

    // endregion
}
