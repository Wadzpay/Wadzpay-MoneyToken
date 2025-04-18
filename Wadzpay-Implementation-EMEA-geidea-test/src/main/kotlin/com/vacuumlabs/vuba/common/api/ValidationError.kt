package com.vacuumlabs.vuba.common.api

data class ValidationError(
    val subject: String,
    val field: String,
    val value: String,
    val message: String,
)
