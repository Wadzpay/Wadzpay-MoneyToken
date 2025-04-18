package com.vacuumlabs.wadzpay.configuration

import com.vacuumlabs.API_VERSION
import com.vacuumlabs.wadzpay.accountowner.AccountOwnerService
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.ErrorResponse
import com.vacuumlabs.wadzpay.exchange.ExchangeService
import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import com.vacuumlabs.wadzpay.merchant.model.FiatCurrencyUnit
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.util.stream.Collectors

@RestController
@RequestMapping("$API_VERSION/config")
@Tag(name = "Config")
class ConfigController(
    val appConfig: AppConfig,
    val exchangeService: ExchangeService,
    val accountOwnerService: AccountOwnerService
) {

    @GetMapping("")
    @Operation(summary = "Get application config ")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Success"),
        ApiResponse(
            responseCode = "403",
            description = ErrorCodes.UNAUTHORIZED,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getConfig(principal: Authentication): ConfigResponse {
        println("@44 getConfig")
        val account = accountOwnerService.extractAccount(principal)
        println("@46 getConfig")
        val wtkAddress = account.account.getSubaccountByAsset(CurrencyUnit.WTK).address?.address ?: "xxx"
        println("@48 getConfig")
        val btcAddress = account.account.getSubaccountByAsset(CurrencyUnit.BTC).address?.address ?: "xxx"
        println("@50 getConfig")
        val ethAddress = account.account.getSubaccountByAsset(CurrencyUnit.ETH).address?.address ?: "xxx"
        println("@52 getConfig")
        val usdtAddress = account.account.getSubaccountByAsset(CurrencyUnit.USDT).address?.address ?: "xxx"
        println("@54 getConfig")
        val usdcAddress = account.account.getSubaccountByAsset(CurrencyUnit.USDT).address?.address ?: "xxx"
        println("@56 getConfig")
        val algoAddress = account.account.getSubaccountByAsset(CurrencyUnit.ALGO).address?.address ?: "xxx"
        println("@58 getConfig")
        val usdcaAddress = account.account.getSubaccountByAsset(CurrencyUnit.USDCA).address?.address ?: "xxx"
        println("@60 getConfig")
        val sarAddress = account.account.getSubaccountByAsset(CurrencyUnit.SART).address?.address ?: "xxx"
        println("@62 getConfig")

        return ConfigResponse(
            appConfig.onRampApiKey,
            appConfig.gateways,
            listOf(
                Crypto(CurrencyUnit.WTK.name, wtkAddress, wtkAddress),
                Crypto(CurrencyUnit.BTC.name, btcAddress, btcAddress),
                Crypto(CurrencyUnit.ETH.name, ethAddress, ethAddress),
                Crypto(CurrencyUnit.USDC.name, usdcAddress, usdcAddress),
                Crypto(CurrencyUnit.USDT.name, usdtAddress, usdtAddress),
                Crypto(CurrencyUnit.ALGO.name, algoAddress, algoAddress),
                Crypto(CurrencyUnit.USDCA.name, usdcaAddress, usdcaAddress),
                Crypto(CurrencyUnit.SART.name, sarAddress, sarAddress)
            ),
            FiatCurrencyUnit.values().asList().stream().map {
                Fiat(it.name, it.sign, it.fullName)
            }.collect(Collectors.toList())
        )
    }

    @GetMapping("algo")
    @Operation(summary = "Get application config ")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Success"),
        ApiResponse(
            responseCode = "403",
            description = ErrorCodes.UNAUTHORIZED,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getConfigAlgo(principal: Authentication): ConfigResponse {
        println("@44 getConfig")
        val account = accountOwnerService.extractAccount(principal)
        println("@46 getConfig")
        val wtkAddress = account.account.getSubAccountByAssetAlgo(CurrencyUnit.WTK)?.address?.address ?: "xxx"
        println("@48 getConfig")
        val btcAddress = account.account.getSubAccountByAssetAlgo(CurrencyUnit.BTC)?.address?.address ?: "xxx"
        println("@50 getConfig")
        val ethAddress = account.account.getSubAccountByAssetAlgo(CurrencyUnit.ETH)?.address?.address ?: "xxx"
        println("@52 getConfig")
        val usdtAddress = account.account.getSubAccountByAssetAlgo(CurrencyUnit.USDT)?.address?.address ?: "xxx"
        println("@54 getConfig")
        val usdcAddress = account.account.getSubAccountByAssetAlgo(CurrencyUnit.USDT)?.address?.address ?: "xxx"
        println("@56 getConfig")
        val algoAddress = account.account.getSubAccountByAssetAlgo(CurrencyUnit.ALGO)?.address?.address ?: "xxx"
        println("@58 getConfig")
        val usdcaAddress = account.account.getSubAccountByAssetAlgo(CurrencyUnit.USDCA)?.address?.address ?: "xxx"
        println("@60 getConfig")
        val sarAddress = account.account.getSubAccountByAssetAlgo(CurrencyUnit.SART)?.address?.address ?: "xxx"
        println("@62 getConfig")

        return ConfigResponse(
            appConfig.onRampApiKey,
            appConfig.gateways,
            listOf(
                Crypto(CurrencyUnit.WTK.name, wtkAddress, wtkAddress),
                Crypto(CurrencyUnit.BTC.name, btcAddress, btcAddress),
                Crypto(CurrencyUnit.ETH.name, ethAddress, ethAddress),
                Crypto(CurrencyUnit.USDC.name, usdcAddress, usdcAddress),
                Crypto(CurrencyUnit.USDT.name, usdtAddress, usdtAddress),
                Crypto(CurrencyUnit.ALGO.name, algoAddress, algoAddress),
                Crypto(CurrencyUnit.USDCA.name, usdcaAddress, usdcaAddress),
                Crypto(CurrencyUnit.SART.name, sarAddress, sarAddress)
            ),
            FiatCurrencyUnit.values().asList().stream().map {
                Fiat(it.name, it.sign, it.fullName)
            }.collect(Collectors.toList())
        )
    }

    @GetMapping("/digitalCurrency")
    @Operation(summary = "Get list of supported Digital Currency")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Success")
    )
    fun getDigitalCurrency(): DigitalCurrencyList {
        return DigitalCurrencyList(CurrencyUnit.values().map { it.name })
    }

    @GetMapping("/fee")
    @Operation(summary = "Get P2P transaction fee")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Success")
    )
    fun getFee(): BigDecimal {
        return appConfig.feeRate
    }

    @GetMapping("/exchangeRate")
    @Operation(summary = "Retrieves exchange rate for digital currency/fiat pair from CryptoCompare.com")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Success")
    )
    fun getExchangeRate(
        @RequestParam(required = true, name = "from") from: FiatCurrencyUnit,
        @RequestParam(required = true, name = "to") to: CurrencyUnit
    ): BigDecimal {

        return exchangeService.getExchangeRate(to, from)
    }

    @GetMapping("/exchangeRates")
    @Operation(summary = "Retrieves exchange rates for single Digital Currency and all fiat currencies from CryptoCompare.com")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Success")
    )
    fun getExchangeRate(
        @RequestParam(required = true, name = "from") from: FiatCurrencyUnit
    ): Map<CurrencyUnit, BigDecimal> {

        return exchangeService.getExchangeRates(from)
    }

    @GetMapping("/exchangeRatesFake")
    @Operation(summary = "Retrieves exchange rates for single Digital Currency and all fiat currencies from CryptoCompare.com But Don't use it")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Success")
    )
    fun getExchangeRateFake(
        @RequestParam(required = true, name = "from") from: FiatCurrencyUnit
    ): Map<CurrencyUnit, BigDecimal> {

        return exchangeService.getExchangeRatesFake(from)
    }
}

data class DigitalCurrencyList(
    val digitalCurrencyList: List<String>
)

data class ConfigResponse(
    val onRampApiKey: String,
    val gateways: String,
    val digitalCurrencyList: List<Crypto>,
    val fiats: List<Fiat>
)

data class Crypto(
    val code: String,
    val inwardAddress: String,
    val outwardAddress: String,
)

data class Fiat(
    val code: String,
    val sign: String,
    val fullName: String
)
