package com.vacuumlabs.vuba.ledger.common

import com.fasterxml.jackson.annotation.JsonValue
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.reflect.KClass

data class Reference(
    @get:JsonValue
    @field:ValidReference
    val value: String
) {
    override fun toString() = value
}
// TODO why trim?
fun isValidReference(value: CharSequence) = value.trim('.').split('.').all { isValidIdentifier(it) }

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ ReferenceValidator::class ])
annotation class ValidReference(
    val message: String = "invalid Reference value",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class ReferenceValidator : ConstraintValidator<ValidReference, CharSequence> {
    override fun isValid(value: CharSequence, context: ConstraintValidatorContext) = isValidReference(value)
}
