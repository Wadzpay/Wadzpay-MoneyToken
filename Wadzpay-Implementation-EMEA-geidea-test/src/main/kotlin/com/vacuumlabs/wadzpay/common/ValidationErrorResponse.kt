package com.vacuumlabs.wadzpay.common

data class ValidationErrorResponse(
    val message: String,
    val validationErrors: List<ValidationError>
)

data class ValidationError(
    val field: String,
    val value: String,
    val message: String
)
