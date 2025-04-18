package com.vacuumlabs.vuba.ledger.common

import javax.validation.Constraint
import javax.validation.Payload
import javax.validation.constraints.Pattern
import kotlin.reflect.KClass

const val identifierPattern = "[a-zA-Z0-9_-]+"
val identifierRegex = Regex(identifierPattern)
fun isValidIdentifier(value: CharSequence) = identifierRegex.matches(value)

@Pattern(regexp = identifierPattern)
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [])
annotation class ValidIdentifier(
    val message: String = "invalid Identifier value",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
