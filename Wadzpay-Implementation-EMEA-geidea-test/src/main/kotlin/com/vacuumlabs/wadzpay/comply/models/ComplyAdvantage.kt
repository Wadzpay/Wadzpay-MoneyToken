package com.vacuumlabs.wadzpay.comply.models

class ComplyAdvantage {

    var id: String = ""
    var source_format: String = ""
    var data: ComplyAdvantageData = TODO()
}

class ComplyAdvantageData {
    var tx_id: String = ""
    var customer_name: String = ""
    var customer_id: String = ""
    var customer_risk_category: String = ""
    var counterparty_id: String = ""
    var counterparty_name: String = ""
    var counterparty_country: String = ""
    var counterparty_bank_country: String = ""
    var tx_direction: String = ""
    var tx_base_currency: String = ""
    var tx_base_amount: String = ""
    var tx_currency: String = ""
    var tx_date_time: String = ""
}
