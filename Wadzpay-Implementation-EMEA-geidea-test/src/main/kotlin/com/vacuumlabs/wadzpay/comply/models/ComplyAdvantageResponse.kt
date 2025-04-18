package com.vacuumlabs.wadzpay.comply.models

import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.Date

data class ComplyAdvantageResponse(
    var result: ResultData,
    var errors: List<Error>
)

data class Error(
    var errorCode: String?
)

data class ResultData(
    var have_all_passed: Boolean,
    var transaction_datetime: Date,
    var id: String?,
    var internal_id: Int,
    var source_format: Int,
    var stored_data: StoredData,
    @JsonIgnore
    var max_priority_action: StringBuilder?,
    var rule_results: List<RuleResults>,
    var cancelled: Boolean,
    var updated_at: Date

)

data class StoredData(
    var tx_id: String,
    var customer_name: String?,
    var customer_id: String?,
    var customer_risk_category: String?,
    var counterparty_id: String?,
    var counterparty_name: String?,
    var counterparty_country: String?,
    var counterparty_bank_country: String?,
    var tx_direction: String?,
    var tx_base_currency: String?,
    var tx_base_amount: String?,
    var tx_currency: String?,
    var tx_date_time: Date,
    var ll_counterparty_country: String?,
    var ll_counterparty_bank_country: String?,
    var ll_tx_currency: String?
)

data class RuleResults(
    var message: String,
    var has_passed: Boolean,
    var rule: Rule,
    @JsonIgnore
    var action: String?
)

data class Rule(
    var id: Int,
    var name: String?,
    var code: String?
)
