package com.vacuumlabs.vuba.ledger.common

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class IdentifierTests {

    companion object {
        @JvmStatic
        fun fromStringDataProvider(): Stream<Arguments> = Stream.of(
            arguments("", false),
            arguments("a:b", false),
            arguments("a", true),
            arguments("abc", true),
            arguments("1", true),
            arguments("123", true),
            arguments("_", true),
            arguments("a_1", true),
            arguments("a-1", true),
        )
    }

    @ParameterizedTest
    @MethodSource("fromStringDataProvider")
    fun testFromString(string: String, success: Boolean) {
        Assertions.assertEquals(isValidIdentifier(string), success)
    }
}
