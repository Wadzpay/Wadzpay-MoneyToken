package com.vacuumlabs.wadzpay.acquireradministration.model

import java.time.LocalDateTime

data class ErrorDetails(val timestamp: LocalDateTime, val message: String, val details: Map<String, String?>)
