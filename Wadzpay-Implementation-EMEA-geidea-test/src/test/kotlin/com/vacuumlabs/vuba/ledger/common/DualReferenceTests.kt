package com.vacuumlabs.vuba.ledger.common

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class DualReferenceTests {

    companion object {
        @JvmStatic
        fun fromStringDataProvider(): Stream<Arguments> = Stream.of(
            Arguments.arguments("", false),
            Arguments.arguments("a.b", false),
            Arguments.arguments("a", false),
            Arguments.arguments("a/b/c", false),
            Arguments.arguments("a/b", true),
            Arguments.arguments("a.b.c/1.2.3", true),
        )
    }

    @ParameterizedTest
    @MethodSource("fromStringDataProvider")
    fun testFromString(string: String, success: Boolean) {
        Assertions.assertEquals(isValidDualReference(string), success)
    }
}
