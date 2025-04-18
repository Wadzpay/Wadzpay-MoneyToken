package com.vacuumlabs.wadzpay.configuration

import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.math.BigDecimal

@Configuration
@ConfigurationProperties(prefix = "appconfig")
class AppConfig {

    var onRampApiKey: String = ""
    var gateways: String = ""
    var feeRate: BigDecimal = BigDecimal.ZERO
    var wallets = Wallets()
    var walletIds = WalletIds()
    var walletPassphrases = WalletPassphrases()
    var production: Boolean = false
    var environment: String = "dev"
    var s3: S3 = S3()
    var gap600RunMode: Boolean = false
    var stubEnable: Boolean = false

    // TODO: change this to addresses, but this is a real pain
    class Wallets {
        var btcInward: String = ""
        var btcOutward: String = ""
        var ethInward: String = ""
        var ethOutward: String = ""
        var usdtInward: String = ""
        var usdtOutward: String = ""
        var wtkInward: String = ""
        var wtkOutward: String = ""
        var usdcInward: String = ""
        var usdcOutward: String = ""
        var algoOutward: String = ""
        var algoInward: String = ""
        var usdcaOutward: String = ""
        var usdcaInward: String = ""
        var sartInward: String = ""
        var sartOutward: String = ""
    }

    class S3 {
        var appBucket: String = ""
    }

    // TODO: use wallet for this
    class WalletIds {
        var btc: String = ""
        var eth: String = ""
        var usdt: String = ""
        var wtk: String = ""
        var usdc: String = ""
        var algo: String = ""
        var usdca: String = ""
        var sart: String = ""
    }

    class WalletPassphrases {
        var btc: String = ""
        var eth: String = ""
        var usdt: String = ""
        var wtk: String = ""
        var usdc: String = ""
        var algo: String = ""
        var usdca: String = ""
        var sart: String = ""
    }

    fun getWalletPassphrase(asset: CurrencyUnit): String {
        return when (asset) {
            CurrencyUnit.WTK -> walletPassphrases.wtk
            CurrencyUnit.USDT -> walletPassphrases.usdt
            CurrencyUnit.ETH -> walletPassphrases.eth
            CurrencyUnit.BTC -> walletPassphrases.btc
            CurrencyUnit.USDC -> walletPassphrases.usdc
            CurrencyUnit.ALGO -> walletPassphrases.algo
            CurrencyUnit.USDCA -> walletPassphrases.usdca
            CurrencyUnit.SART -> walletPassphrases.sart
        }
    }

    fun getRefundFormUrl(): String {
        return if (environment == "prod") {
            "https://merchant.ddfpilot.wadzpay.com/"
        } else if (environment == "ddf-uat") {
            "https://merchant.ddf1.wadzpay.com/"
        } else {
            "https://merchant.$environment.wadzpay.com/"
        }
    }
}
