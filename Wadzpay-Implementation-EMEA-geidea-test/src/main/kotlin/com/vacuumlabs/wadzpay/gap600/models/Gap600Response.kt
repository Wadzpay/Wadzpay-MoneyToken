package com.vacuumlabs.wadzpay.gap600.models

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
// import com.vacuumlabs.wadzpay.gap600.Gap600Message
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
data class Gap600ConfirmationResponse(
    val status: Int,
    val message: Gap600Message
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Gap600Message(

    val Hash: String,
    val outputAddress: String,
    val status: String,
    val username: String,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    val scoreTime: String,
    val agentId: String,
    val size: Int,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    val txValueUSD: BigDecimal,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    val txValueBTC: BigDecimal

)
@JsonIgnoreProperties(ignoreUnknown = true)
data class Gap600400Response(
    val status: Int,
    val type: String,
    val message: String

)
@JsonIgnoreProperties(ignoreUnknown = true)
data class Gap600FailedResponse(
    val status: Int,
    val type: String,
    val Hash: String
)
/*
   @JsonFormat(shape = JsonFormat.Shape.STRING)
    val message : String,

 */
