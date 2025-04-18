package com.vacuumlabs.wadzpay.gap600

import com.vacuumlabs.wadzpay.ledger.CurrencyUnit

const val testpathsegment = "test"
const val gap600source = "gap600"

enum class Gap600Coin { btc, tbtc }

enum class Gap600CoinType(val coin: String) {
    BTC("BTC")
}

fun Gap600Coin.toCurrencyUnit(): CurrencyUnit {
    return when (Gap600Coin.valueOf(name)) {
        Gap600Coin.btc -> CurrencyUnit.BTC
        Gap600Coin.tbtc -> CurrencyUnit.BTC
    }
}
