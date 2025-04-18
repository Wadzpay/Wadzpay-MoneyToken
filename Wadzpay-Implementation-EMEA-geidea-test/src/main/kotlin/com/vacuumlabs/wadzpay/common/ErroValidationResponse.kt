package com.vacuumlabs.wadzpay.common

import com.vacuumlabs.wadzpay.acquireradministration.model.ValidationErrorResponseCustom

data class ErroValidationResponse(
    val status: Number,
    val message: String,
    val fields: ValidationErrorResponseCustom
)

data class ErrorResponseWithListData(
    val status: Number,
    val message: String,
    val list: MutableList<String>
)
