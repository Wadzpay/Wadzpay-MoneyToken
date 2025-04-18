package com.vacuumlabs.vuba.ledger.common

import com.fasterxml.jackson.annotation.JsonValue
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.reflect.KClass

data class DualReference(
    @get:JsonValue
    @field:ValidDualReference
    val value: String
) {
    constructor(parent: Reference, child: Reference) : this("$parent/$child")
    override fun toString() = value
}

fun isValidDualReference(value: CharSequence) = value.split('/').let { parts ->
    parts.size == 2 && parts.all { isValidReference(it) }
}

fun String.splitReference(): Pair<Reference, Reference> {
    val parts = split('/')
    require(parts.size == 2) { "DualReference must have two parts, but found $parts" }
    return Reference(parts[0]) to Reference(parts[1])
}

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ DualReferenceValidator::class ])
annotation class ValidDualReference(
    val message: String = "invalid DualReference value",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class DualReferenceValidator : ConstraintValidator<ValidDualReference, CharSequence> {
    override fun isValid(value: CharSequence, context: ConstraintValidatorContext) = isValidDualReference(value)
}
