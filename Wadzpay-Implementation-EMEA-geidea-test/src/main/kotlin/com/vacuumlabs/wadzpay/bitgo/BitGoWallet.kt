package com.vacuumlabs.wadzpay.bitgo

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.ServiceUnavailableException
import com.vacuumlabs.wadzpay.configuration.AppConfig
import com.vacuumlabs.wadzpay.configuration.BitGoConfiguration
import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import com.vacuumlabs.wadzpay.ledger.model.CryptoAddress
import com.vacuumlabs.wadzpay.ledger.model.CryptoAddressRepository
import com.vacuumlabs.wadzpay.utils.ApisLog
import com.vacuumlabs.wadzpay.utils.ApisLoggerRepository
import com.vacuumlabs.wadzpay.webhook.BitGoCoin
import com.vacuumlabs.wadzpay.webhook.toCurrencyUnit
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.math.BigDecimal
import java.net.SocketException
import java.util.UUID

@Component
class BitGoWallet(
    @Qualifier("BitGo") val restTemplate: RestTemplate,
    val bitGoConfiguration: BitGoConfiguration,
    val appConfig: AppConfig,
    val cryptoAddressRepository: CryptoAddressRepository,
    val apisLoggerRepository: ApisLoggerRepository
) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    /**
     * Retrieves information about transfer (transaction) happening on a wallet. This needs to be called
     * after we received a webhook event about a transfer happening
     *
     * @param coin crypto coin code - e.g. btc, tbtc, eth (we need an enum for these after client clarifies the bitgo situation)
     * @param walletId id of the wallet that the transaction happened on - retrieved from webhook event
     * @param transferId id of the transfer itself - retrieved from webhook event
     */
    @Retryable(
        value = [SocketException::class],
        maxAttemptsExpression = "\${retry-policy.retryCount}",
        backoff = Backoff(delayExpression = "\${retry-policy.retryInterval}")
    )
    fun getTransfer(coin: BitGoCoin, walletId: String, transferId: String): BitGoTransfer? {
        try {
            return restTemplate.getForObject(
                UriComponentsBuilder.fromHttpUrl(bitGoConfiguration.baseUrl).path("/api/v2")
                    .pathSegment(coin.toString()).pathSegment("wallet").pathSegment(walletId).pathSegment("transfer")
                    .pathSegment(transferId).build().toUri(),
                BitGoTransfer::class.java
            )
        } catch (e: RestClientResponseException) {
            apisLoggerRepository.save(
                ApisLog(
                    "BitGo",
                    "GetTransfer:- $coin",
                    "TrsId:- " + transferId + "WalletId:- " + walletId,
                    e.message.toString()
                )
            )

            logger.error(e.statusText, e)
        }

        return null
    }

    fun sendToExternalWallet(amount: BigDecimal, asset: CurrencyUnit, address: String): BitGoExternalTransaction {
        val request = SendExternalTransactionRequest(
            address, asset.amountToBaseUnits(amount), appConfig.getWalletPassphrase(asset)
        )
        val coin = asset.toBitGoCoin(appConfig.production)
        val walletId = asset.toWalletId(appConfig)

        logger.info("Sending $coin from wallet having id $walletId to external wallet: $request")
        apisLoggerRepository.save(
            ApisLog(
                "BitGoExt", "Req:- $request", "$address $walletId", appConfig.getWalletPassphrase(asset)
            )
        )
        val exTrx = restTemplate.postForObject(
            UriComponentsBuilder.fromHttpUrl(bitGoConfiguration.express.baseUrl).path("/api/v2")
                .pathSegment(coin.toString()).pathSegment("wallet").pathSegment(walletId).pathSegment("sendcoins")
                .build().toUri(),
            request,
            BitGoExternalTransaction::class.java
        )!!

        apisLoggerRepository.save(ApisLog("BitGoExt2", "Req:- $request", address, exTrx.toString()))

        return exTrx
    }

    private fun buildBTCFeeEstimateRequest(): HttpEntity<Map<String, Any>> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val map = mapOf(
            "nP2shInputs" to 0, "nP2pkhInputs" to 0, "nP2shP2wshInputs" to 1, "nOutputs" to 2
        )

        return HttpEntity(map, headers)
    }

    fun getFeeEstimateBTC(coin: BitGoCoin): BigDecimal {
        return restTemplate.postForEntity(
            UriComponentsBuilder.fromHttpUrl(bitGoConfiguration.express.baseUrl).path("/api/v2/calculateminerfeeinfo")
                .build().toUri(),
            buildBTCFeeEstimateRequest(),
            FeeEstimateBTC::class.java
        ).body!!.fee.normalize(coin)
    }

    fun getFeeEstimateETH(coin: BitGoCoin, address: String, amount: BigDecimal): BigDecimal {
        println("getFeeEstimateETH")
        println(coin)
        println(bitGoConfiguration.baseUrl)
        println(coin.toString())
        println(coin.toCurrencyUnit())
        println(
            UriComponentsBuilder.fromHttpUrl(bitGoConfiguration.baseUrl).path("/api/v2").pathSegment(coin.toString())
                .pathSegment("tx/fee").queryParam("recipient", address)
                .queryParam("amount", coin.toCurrencyUnit().amountToBaseUnits(amount)).build().toUri()
        )
        return restTemplate.getForObject(
            UriComponentsBuilder.fromHttpUrl(bitGoConfiguration.baseUrl).path("/api/v2").pathSegment(coin.toString())
                .pathSegment("tx/fee").queryParam("recipient", address)
                .queryParam("amount", coin.toCurrencyUnit().amountToBaseUnits(amount)).build().toUri(),
            FeeEstimateETH::class.java
        )!!.feeEstimate.normalize(CurrencyUnit.ETH.toBitGoCoin(appConfig.production))
    }

    fun getFeeEstimate(coin: BitGoCoin, address: String, amount: BigDecimal): BigDecimal {
        println("inside getFeeEstimate 111111")
        println(coin)
        if (coin == BitGoCoin.tusdt || coin == BitGoCoin.twtk || coin == BitGoCoin.tusdc || coin == BitGoCoin.sart || coin == BitGoCoin.talgo || coin == BitGoCoin.tusdca) {
            return BigDecimal.ZERO
        }
        try {
            println("inside getFeeEstimate 2222")
            println(coin)
            return if (coin == BitGoCoin.btc || coin == BitGoCoin.tbtc) {
                getFeeEstimateBTC(coin)
            } else {
                println("inside else @148")
                println(coin)
                getFeeEstimateETH(coin, address, amount)
            }
        } catch (ex: ResourceAccessException) {
            apisLoggerRepository.save(ApisLog("BitGo", "Failed To Get Estimate:- " + ex.message, address, coin.name))
            throw ServiceUnavailableException(ErrorCodes.WALLET_NOT_AVAILABLE)
        } catch (ex: Exception) {
            logger.info(ex.message)
            apisLoggerRepository.save(ApisLog("BitGo", "UNKNOWN_WALLET_ERROR:- " + ex.message, address, coin.name))
            throw ServiceUnavailableException(ErrorCodes.UNKNOWN_WALLET_ERROR)
        }
    }

    @Retryable(
        value = [SocketException::class],
        maxAttemptsExpression = "\${retry-policy.retryCount}",
        backoff = Backoff(delayExpression = "\${retry-policy.retryInterval}")
    )
    fun createAddress(asset: CurrencyUnit): CryptoAddress? {
        println("appConfig@178 ${appConfig.environment} ")
        println("asset@178  $asset")
        println("appConfig@178  ${appConfig.production}")
        val coin: BitGoCoin = when {
            asset.onEthBlockchain() -> {
                CurrencyUnit.ETH.toBitGoCoin(appConfig.production)
            }

            asset.onAlgoOnlyBlockchain() -> {
                CurrencyUnit.ALGO.toBitGoCoin(appConfig.production)
            }

            asset.onAlgoUSDCBlockchain() -> {
                CurrencyUnit.ALGO.toBitGoCoin(appConfig.production)
            }

            asset.onWadzpayBlockChain() -> {
                CurrencyUnit.SART.toBitGoCoin(appConfig.production)
            }

            else -> {
                asset.toBitGoCoin(appConfig.production)
            }
        }
        println("asset@201  $asset")
        val walletId = asset.toWalletId(appConfig)

        if (walletId == "not_existing" || coin.name.equals("twtk", true)) {
            return null
        }

        val addressString = if (asset.onWadzpayBlockChain()) {

            // TODO: get address from blockchain APIs
            UUID.randomUUID().toString()
        } else {
            println("restTemplate")
            println("coin $coin")
            restTemplate.postForEntity(
                UriComponentsBuilder.fromHttpUrl(bitGoConfiguration.baseUrl).path("/api/v2/").pathSegment(coin.toString())
                    .pathSegment("wallet").pathSegment(walletId).pathSegment("address").build().toUri(),
                CreateAddressRequest(),
                Address::class.java
            ).body!!.address
        }

        // logger.info("Address is : $addressString, for the coin type $coin, and for CurrencyUnit: $asset")
        return saveCryptoAdd(asset.toString(), addressString)
    }

    private fun saveCryptoAdd(asset: String, addressString: String): CryptoAddress? {
        var cryptoAddress: CryptoAddress? = null

        try {
            cryptoAddress = cryptoAddressRepository.save(CryptoAddress(asset = asset, address = addressString))
        } catch (e: Exception) {
            logger.info(e.localizedMessage)
            saveCryptoAdd(asset, addressString)
        }
        return cryptoAddress
    }

    @Retryable(
        value = [SocketException::class],
        maxAttemptsExpression = "\${retry-policy.retryCount}",
        backoff = Backoff(delayExpression = "\${retry-policy.retryInterval}")
    )
    fun createAddressNotSave(asset: String): String {
        val asset = CurrencyUnit.valueOf(asset)
        val coin: BitGoCoin = if (asset.onEthBlockchain()) {
            CurrencyUnit.ETH.toBitGoCoin(appConfig.production)
        } else if (asset.onAlgoUSDCBlockchain()) {
            CurrencyUnit.ALGO.toBitGoCoin(appConfig.production)
        } else if (asset.onAlgoOnlyBlockchain()) {
            CurrencyUnit.ALGO.toBitGoCoin(appConfig.production)
        } else {
            asset.toBitGoCoin(appConfig.production)
        }
        val walletId = asset.toWalletId(appConfig)

        if (walletId == "not_existing") {
            return "not_existing"
        }

        return restTemplate.postForEntity(
            UriComponentsBuilder.fromHttpUrl(bitGoConfiguration.baseUrl).path("/api/v2/").pathSegment(coin.toString())
                .pathSegment("wallet").pathSegment(walletId).pathSegment("address").build().toUri(),
            CreateAddressRequest(),
            Address::class.java
        ).body!!.address
    }
}

data class CreateAddressRequest(
    val forwarderVersion: Long = 1
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Address(
    val address: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class FeeEstimateETH(
    val feeEstimate: BigDecimal
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class FeeEstimateBTC(
    val fee: BigDecimal
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class BitGoTransfer(
    val id: String,
    /** id of the transfer */
    val txid: String,
    /** "receive" or "send" */
    val type: BitGoTransferType,
    /** coin or token symbol **/
    val coin: BitGoCoin,
    /** in the smallest countable coin part (e.g. Satoshi - 0.00000001 BTC) */
    @JsonFormat(shape = JsonFormat.Shape.STRING) val baseValueString: BigDecimal,
    /** fee for the transaction */
    @JsonFormat(shape = JsonFormat.Shape.STRING) val feeString: BigDecimal,
    val entries: List<BitGoTransferEntry> = listOf()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class BitGoTransferEntry(
    val address: String,
    val wallet: String? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING) val valueString: BigDecimal
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class BitGoExternalTransaction(
    val transfer: BitGoTransfer,
    val tx: String,
    val txid: String,
    val status: String // TODO: change to enum, but I'm not sure how signed(suppressed) will be handled"
)

enum class BitGoTransferType { receive, send }

fun BigDecimal.normalize(coin: BitGoCoin): BigDecimal {
    return when (coin) {
        BitGoCoin.twtk -> divide(CurrencyUnit.WTK.scalingFactor())
        BitGoCoin.wtk -> divide(CurrencyUnit.WTK.scalingFactor())
        BitGoCoin.btc -> divide(CurrencyUnit.BTC.scalingFactor())
        BitGoCoin.tbtc -> divide(CurrencyUnit.BTC.scalingFactor())
        BitGoCoin.eth -> divide(CurrencyUnit.ETH.scalingFactor())
        BitGoCoin.gteth -> divide(CurrencyUnit.ETH.scalingFactor())
        BitGoCoin.usdt -> divide(CurrencyUnit.USDT.scalingFactor())
        BitGoCoin.tusdt -> divide(CurrencyUnit.USDT.scalingFactor())
        BitGoCoin.usdc -> divide(CurrencyUnit.USDC.scalingFactor())
        BitGoCoin.tusdc -> divide(CurrencyUnit.USDC.scalingFactor())
        BitGoCoin.algo -> divide(CurrencyUnit.ALGO.scalingFactor())
        BitGoCoin.talgo -> divide(CurrencyUnit.ALGO.scalingFactor())
        BitGoCoin.usdca -> divide(CurrencyUnit.USDCA.scalingFactor())
        BitGoCoin.tusdca -> divide(CurrencyUnit.USDCA.scalingFactor())
        BitGoCoin.sart -> divide(CurrencyUnit.SART.scalingFactor())
    }
}

fun BitGoTransfer.normalizedAmount(): BigDecimal {
    return baseValueString.normalize(coin)
}

fun BitGoTransfer.normalizedFee(): BigDecimal {
    return feeString.normalize(coin)
}

fun CurrencyUnit.toBitGoCoin(production: Boolean): BitGoCoin {
    return when (this) {
        CurrencyUnit.WTK -> if (production) BitGoCoin.wtk else BitGoCoin.twtk
        CurrencyUnit.BTC -> if (production) BitGoCoin.btc else BitGoCoin.tbtc
        CurrencyUnit.ETH -> if (production) BitGoCoin.eth else BitGoCoin.gteth
        CurrencyUnit.USDT -> if (production) BitGoCoin.usdt else BitGoCoin.tusdt
        CurrencyUnit.USDC -> if (production) BitGoCoin.usdc else BitGoCoin.tusdc
        CurrencyUnit.ALGO -> if (production) BitGoCoin.algo else BitGoCoin.talgo
        CurrencyUnit.USDCA -> if (production) BitGoCoin.usdca else BitGoCoin.tusdca
        CurrencyUnit.SART -> if (production) BitGoCoin.sart else BitGoCoin.sart
    }
}

fun CurrencyUnit.toWalletId(appConfig: AppConfig): String {
    return when (this) {
        CurrencyUnit.WTK -> appConfig.walletIds.wtk
        CurrencyUnit.BTC -> appConfig.walletIds.btc
        CurrencyUnit.ETH -> appConfig.walletIds.eth
        CurrencyUnit.USDT -> appConfig.walletIds.usdt
        CurrencyUnit.USDC -> appConfig.walletIds.usdc
        CurrencyUnit.ALGO -> appConfig.walletIds.algo
        CurrencyUnit.USDCA -> appConfig.walletIds.usdca
        CurrencyUnit.SART -> appConfig.walletIds.sart
    }
}

fun CurrencyUnit.toAddress(appConfig: AppConfig): String {
    return when (this) {
        CurrencyUnit.WTK -> appConfig.wallets.wtkInward
        CurrencyUnit.BTC -> appConfig.wallets.btcInward
        CurrencyUnit.ETH -> appConfig.wallets.ethInward
        CurrencyUnit.USDT -> appConfig.wallets.usdtInward
        CurrencyUnit.USDC -> appConfig.wallets.usdcInward
        CurrencyUnit.ALGO -> appConfig.wallets.algoInward
        CurrencyUnit.USDCA -> appConfig.wallets.usdcaInward
        CurrencyUnit.SART -> appConfig.wallets.sartInward
    }
}

data class SendExternalTransactionRequest(
    val address: String,
    val amount: String,
    val walletPassphrase: String
)
