package com.vacuumlabs.vuba.common

import com.vacuumlabs.vuba.common.api.ValidationError

sealed class VubaBusinessException(
    override val message: String
) : RuntimeException(message) {
    class NotFound(
        val subject: String,
        val field: String,
        val value: String,
        message: String = "$subject not found: $field = \"$value\"",
    ) : VubaBusinessException(message) {
        constructor(error: ValidationError) : this(error.subject, error.field, error.value, error.message)
    }

    class AlreadyExists(
        val subject: String,
        val field: String,
        val value: String,
        message: String = "$subject already exists: $field = \"$value\"",
    ) : VubaBusinessException(message) {
        constructor(error: ValidationError) : this(error.subject, error.field, error.value, error.message)
    }

    class BusinessValidation(
        val errors: List<ValidationError> = emptyList(),
        message: String
    ) : VubaBusinessException(message)

    class TechnicalValidation(
        val errors: List<ValidationError>,
        message: String
    ) : VubaBusinessException(message)
}
