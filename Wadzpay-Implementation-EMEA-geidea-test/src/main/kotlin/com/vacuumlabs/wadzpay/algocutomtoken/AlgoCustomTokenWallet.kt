package com.vacuumlabs.wadzpay.algocutomtoken

import com.algorand.algosdk.account.Account
import com.vacuumlabs.wadzpay.algocutomtoken.models.AccountInfoFromMnuRequest
import com.vacuumlabs.wadzpay.algocutomtoken.models.AccountInfoFromMnuResponse
import com.vacuumlabs.wadzpay.algocutomtoken.models.AlgoCustomTokenAccountResponse
import com.vacuumlabs.wadzpay.algocutomtoken.models.AlgoCustomTokenCreateRequest
import com.vacuumlabs.wadzpay.algocutomtoken.models.AlgoCustomTokenCreateResponse
import com.vacuumlabs.wadzpay.algocutomtoken.models.AlgoCustomTokenOptInRequest
import com.vacuumlabs.wadzpay.algocutomtoken.models.AlgoCustomTokenToAccountRequest
import com.vacuumlabs.wadzpay.algocutomtoken.models.AlgoCustomTokenToAccountResponse
import com.vacuumlabs.wadzpay.algocutomtoken.models.AlgoCustomTokenTransferRequest
import com.vacuumlabs.wadzpay.algocutomtoken.models.AlgoCustomTokenTransferRequestById
import com.vacuumlabs.wadzpay.algocutomtoken.models.AlgoCustomTokenTransferResponse
import com.vacuumlabs.wadzpay.algocutomtoken.models.AlgoRefundUnspendTokenRequest
import com.vacuumlabs.wadzpay.algocutomtoken.models.AlgoRefundUnspendTokenResponse
import com.vacuumlabs.wadzpay.algocutomtoken.models.AlgoTokenAllBalanceItemResponse
import com.vacuumlabs.wadzpay.algocutomtoken.models.AlgoTokenAllBalanceResponse
import com.vacuumlabs.wadzpay.algocutomtoken.models.AlgoTokenBalanceReq
import com.vacuumlabs.wadzpay.algocutomtoken.models.AlgoTokenBalanceResponse
import com.vacuumlabs.wadzpay.algocutomtoken.models.AssetsInfo
import com.vacuumlabs.wadzpay.algocutomtoken.models.BcTransInfo
import com.vacuumlabs.wadzpay.algocutomtoken.models.SingleAssetInfo
import com.vacuumlabs.wadzpay.common.EntityNotFoundException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.configuration.AlgoCustomTokenConfiguration
import com.vacuumlabs.wadzpay.configuration.AppConfig
import com.vacuumlabs.wadzpay.ledger.model.AlgorandAddress
import com.vacuumlabs.wadzpay.ledger.model.AlgorandAddressRepository
import com.vacuumlabs.wadzpay.ledger.model.CryptoAddress
import com.vacuumlabs.wadzpay.ledger.model.CryptoAddressRepository
import com.vacuumlabs.wadzpay.utils.ApisLog
import com.vacuumlabs.wadzpay.utils.ApisLoggerRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.math.BigInteger
import java.net.SocketException

@Component
class AlgoCustomTokenWallet(
    @Qualifier("AlgoCustomToken")
    val restTemplate: RestTemplate,
    val algoCustomTokenConfiguration: AlgoCustomTokenConfiguration,
    val appConfig: AppConfig,
    val cryptoAddressRepository: CryptoAddressRepository,
    val apisLoggerRepository: ApisLoggerRepository,
    val algorandAddressRepository: AlgorandAddressRepository

) {
    val logger: Logger = LoggerFactory.getLogger(javaClass)
    val algoApiVersion = algoCustomTokenConfiguration.algoApiVersion
    val errorMsgInsufficientAmt = "insufficient amount"

    @Retryable(
        value = [SocketException::class],
        maxAttemptsExpression = "\${retry-policy.retryCount}",
        backoff = Backoff(delayExpression = "\${retry-policy.retryInterval}")
    )
    fun algoCutomTokenTransfer(sender: String, receiver: String, amt: String): String? {
        val envUrlPath = getCustomPath()
        // val sendmaster ="edit random width clap crowd swamp pride original mushroom decade math quick apology march endorse prison lab supply wave undo obscure auto armed absorb try"
        logger.info("creating algo transfer ReceiverAddress : $receiver , Sender : $sender , Amount : $amt")
        val algoCustomTokenTransferRequest =
            AlgoCustomTokenTransferRequest(ReceiverAddress = receiver, SendrMnemonic = sender, Amount = amt)
        logger.info("creating algo transfer request...")
        try {
            // transfer
            val algoTokenResp = restTemplate.postForObject(
                UriComponentsBuilder.fromHttpUrl(algoCustomTokenConfiguration.baseUrl)
                    .pathSegment(envUrlPath)
                    .path("/transferasset")
                    .build().toUri(),
                algoCustomTokenTransferRequest,
                AlgoCustomTokenTransferResponse::class.java
            )
            logger.info("algo custom token Transfer response : $algoTokenResp ")
            return algoTokenResp!!.transactionId
        } catch (e: Exception) {
            logger.info("Error while calling algo custom token Transfer : ${e.message}")
            apisLoggerRepository.save(
                ApisLog(
                    "algoCutomTokenTransfer",
                    "Transfer:- ",
                    "",
                    e.message.toString()
                )
            )

            logger.error("Error from Exception - algo custom token Transfer ", e)
            throw EntityNotFoundException(ErrorCodes.BLOCKCHAIN_SERVICE_NOT_FOUND)
        }
    }

    @Retryable(
        value = [SocketException::class],
        maxAttemptsExpression = "\${retry-policy.retryCount}",
        backoff = Backoff(delayExpression = "\${retry-policy.retryInterval}")
    )
    fun algoTokenBalance(algoAddress: String): String? {
        val envUrlPath = getCustomPath()
        logger.info("algo wallet token balance request for : $algoAddress")
        // val algoTokenBalanceReq = AlgoTokenBalanceReq("similar vintage waste pilot bus eagle expire embody maximum fury yellow man orbit erase squeeze waste focus monkey snow ceiling visa student dignity ability title")
        val algoTokenBalanceReq = AlgoTokenBalanceReq(algoAddress)
        // val algoResp :AlgoTokenBalanceResponse
        val url1 = UriComponentsBuilder.fromHttpUrl(algoCustomTokenConfiguration.baseUrl)
            .pathSegment(envUrlPath)
            .path("/balance")
            .build().toUri()
        // logger.info("algo wallet token balance :${url1.toURL()}")
        try {
            val algoResp1 = restTemplate.postForObject(
                UriComponentsBuilder.fromHttpUrl(algoCustomTokenConfiguration.baseUrl)
                    .pathSegment(envUrlPath)
                    .path("/balance")
                    .build().toUri(),
                algoTokenBalanceReq,
                AlgoTokenBalanceResponse::class.java
            )
            logger.info("algo token balance Response :$algoResp1")
            return algoResp1?.AssetBalance
        } catch (e: Exception) {
            logger.error("Error from - algo token balance ", e)
            throw EntityNotFoundException(ErrorCodes.BLOCKCHAIN_SERVICE_NOT_FOUND)
        }
    }

    @Retryable(
        value = [SocketException::class],
        maxAttemptsExpression = "\${retry-policy.retryCount}",
        backoff = Backoff(delayExpression = "\${retry-policy.retryInterval}")
    )
    fun createAddressNotSave(asset: String): String {
        val envUrlPath = getCustomPath()
        val algoResp = restTemplate.getForObject(
            UriComponentsBuilder.fromHttpUrl(algoCustomTokenConfiguration.baseUrl)
                .pathSegment(algoApiVersion)
                .pathSegment(envUrlPath)
                .path("/createaccount")
                .queryParam("asset", asset.toString())
                .build().toUri(),
            AlgoCustomTokenAccountResponse::class.java
        )

        // logger.info("createAddressNotSave: address details for algo custom token :" + algoResp.toString())
        logger.info("createAddressNotSave :address details for algo custom token :" + algoResp!!.Address)
        // logger.info("createAddressNotSave: address details for algo custom token :" + algoResp.passphrase)
        val adress = algoResp.Address // algoResp.toString()
        return adress
    }

    /*
    @Retryable(
        value = [SocketException::class],
        maxAttemptsExpression = "\${retry-policy.retryCount}",
        backoff = Backoff(delayExpression = "\${retry-policy.retryInterval}")
    )
    */
    @Retryable(
        value = [SocketException::class],
        maxAttemptsExpression = "\${retry-policy.retryCount}",
        backoff = Backoff(delayExpression = "\${retry-policy.retryInterval}")
    )
    fun createAddress(asset: String): CryptoAddress? {
        val envUrlPath = getCustomPath()
        logger.info("Address creation for  : $asset")
        logger.info(
            " url :" + UriComponentsBuilder.fromHttpUrl(algoCustomTokenConfiguration.baseUrl)
                .pathSegment(algoApiVersion)
                .pathSegment(envUrlPath)
                .path("/createaccount")
                .queryParam("asset", asset.toString())
                .build().toUri()
        )
        try {
            val algoResp = restTemplate.getForObject(
                UriComponentsBuilder.fromHttpUrl(algoCustomTokenConfiguration.baseUrl)
                    .pathSegment(algoApiVersion)
                    .pathSegment(envUrlPath)
                    .path("/createaccount")
                    .queryParam("asset", asset.toString())
                    .build().toUri(),
                AlgoCustomTokenAccountResponse::class.java
            )
            // logger.info("address details for algo custom token :" + algoResp.toString())
            logger.info("address details for algo custom token :" + algoResp!!.Address)
            // logger.info("address details for algo custom token :" + algoResp.passphrase)
            val adress = algoResp.Address // algoResp.passphrase
            logger.info(" db save : address details for algo custom token")
            val cryptoAdres = saveCryptoAdd(asset, adress)
            return cryptoAdres
        } catch (e: Exception) {
            logger.info("Error while calling createAddress: ${e.message}")
            apisLoggerRepository.save(
                ApisLog(
                    "createAddress",
                    "createAddress",
                    "emailone",
                    e.message.toString()
                )
            )
            // logger.info(e.toString())
            logger.error("Error from Exception - createAddress ", e)
        }
        return null
    }

    fun saveCryptoAdd(asset: String, addressString: String): CryptoAddress? {
        var cryptoAddress: CryptoAddress? = null
        // cryptoAddress=  CryptoAddress(asset = asset, address = addressString)
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
    fun loadCustomTokensToAccount(toAcc: String, amt: String): String {
        val envUrlPath = getCustomPath()
        logger.info("loadCustomTokensToAccount : $toAcc with amount : $amt")
        val algoCustomTokenToAccountRequest =
            AlgoCustomTokenToAccountRequest(AccountAddress = toAcc, Amount = amt)
        logger.info("creating loadCustomTokensToAccount request... $algoCustomTokenToAccountRequest")
        // logger.info("creating loadCustomTokensToAccount request... " + algoCustomTokenConfiguration.baseUrl)
        try {
            val algoTokenToAccResp = restTemplate.postForObject(
                UriComponentsBuilder.fromHttpUrl(algoCustomTokenConfiguration.baseUrl)
                    .pathSegment(envUrlPath)
                    .path("/transfertoaccount")
                    .build().toUri(),
                algoCustomTokenToAccountRequest,
                AlgoCustomTokenToAccountResponse::class.java
            )
            logger.info("loadCustomTokensToAccount response : $algoTokenToAccResp ")
            return algoTokenToAccResp!!.transactionId
        } catch (e: Exception) {
            logger.info("Error while calling loadCustomTokensToAccount: ${e.message}")
            apisLoggerRepository.save(
                ApisLog(
                    "loadCustomTokensToAccount",
                    "loadCustomTokensToAccount:- ",
                    "",
                    e.message.toString()
                )
            )
            logger.error("Error from Exception - loadCustomTokensToAccount", e)
            if (e.message?.contains(errorMsgInsufficientAmt, true) == true) {
                throw EntityNotFoundException(ErrorCodes.BLOCKCHAIN_INSUFFICIENT_AMOUNT)
            } else {
                throw EntityNotFoundException(ErrorCodes.BLOCKCHAIN_SERVICE_NOT_FOUND)
            }
            // return "Fail"
        }
    }

    @Retryable(
        value = [SocketException::class],
        maxAttemptsExpression = "\${retry-policy.retryCount}",
        backoff = Backoff(delayExpression = "\${retry-policy.retryInterval}")
    )
    fun refundUnspentCustomTokens(FromAcc: String, amt: String): String {
        val envUrlPath = getCustomPath()
        logger.info("refundUnspentCustomTokens : $FromAcc with amount : $amt")
        val algoRefundUnspendTokenRequest =
            AlgoRefundUnspendTokenRequest(AccountMnemonic = FromAcc, Amount = amt)
        logger.info("creating refundUnspentCustomTokens request...")
        try {
            val algoTokenRefundResp = restTemplate.postForObject(
                UriComponentsBuilder.fromHttpUrl(algoCustomTokenConfiguration.baseUrl)
                    .pathSegment(envUrlPath)
                    .path("/transferfromaccount")
                    .build().toUri(),
                algoRefundUnspendTokenRequest,
                AlgoRefundUnspendTokenResponse::class.java
            )
            logger.info("refundUnspentCustomTokens response : $algoTokenRefundResp ")
            return algoTokenRefundResp!!.transactionId
        } catch (e: Exception) {
            logger.info("Error while calling refundUnspentCustomTokens: ${e.message}")
            apisLoggerRepository.save(
                ApisLog(
                    "refundUnspentCustomTokens",
                    "refundUnspentCustomTokens:- ",
                    "",
                    e.message.toString()
                )
            )
            logger.error("Error from Exception - refundUnspentCustomTokens", e)
            throw EntityNotFoundException(ErrorCodes.BLOCKCHAIN_SERVICE_NOT_FOUND)
            // return "Fail"
        }
    }

    @Retryable(
        value = [SocketException::class],
        maxAttemptsExpression = "\${retry-policy.retryCount}",
        backoff = Backoff(delayExpression = "\${retry-policy.retryInterval}")
    )
    fun accountInfoFromMnu(accMnu: String): AccountInfoFromMnuResponse? {
        val envUrlPath = getCustomPath()
        logger.info("accountInfoFromMnu : $accMnu ")
        val accountInfoFromMnuRequest =
            AccountInfoFromMnuRequest(Mnemonic = accMnu)
        logger.info("creating loadCustomTokensToAccount request...")
        try {
            val accountInfoFromMnuResp = restTemplate.postForObject(
                UriComponentsBuilder.fromHttpUrl(algoCustomTokenConfiguration.baseUrl)
                    .pathSegment(envUrlPath)
                    .path("/accountfrommnemonic")
                    .build().toUri(),
                accountInfoFromMnuRequest,
                AccountInfoFromMnuResponse::class.java
            )
            logger.info("loadCustomTokensToAccount response : $accountInfoFromMnuResp ")
            return accountInfoFromMnuResp!!
        } catch (e: Exception) {
            logger.info("Error while calling accountInfoFromMnu: ${e.message}")
            apisLoggerRepository.save(
                ApisLog(
                    "accountInfoFromMnu",
                    "accountInfoFromMnu:- ",
                    "",
                    e.message.toString()
                )
            )
            logger.error("Error from Exception ", e)
            throw EntityNotFoundException(ErrorCodes.BLOCKCHAIN_SERVICE_NOT_FOUND)
        }
    }

    fun getTransactionInfo(transId: String): BcTransInfo? {
        val envUrlPath = getCustomPath()
        logger.info("getTransactionInfo : $transId ")
        try {
            val bcTransInfoResp = restTemplate.getForObject(
                UriComponentsBuilder.fromHttpUrl(algoCustomTokenConfiguration.baseUrl)
                    .pathSegment(envUrlPath)
                    .path("/transactions")
                    .pathSegment(transId)
                    .build().toUri(),
                BcTransInfo::class.java
            )
            logger.info("getTransactionInfo response : $bcTransInfoResp ")
            return bcTransInfoResp!!
        } catch (e: Exception) {
            logger.info("Error while calling getTransactionInfo: ${e.message}")
            apisLoggerRepository.save(
                ApisLog(
                    "getTransactionInfo",
                    "getTransactionInfo:- ",
                    "",
                    e.message.toString()
                )
            )
            logger.error("Error while calling getTransactionInfo", e)
            throw EntityNotFoundException(ErrorCodes.BLOCKCHAIN_SERVICE_NOT_FOUND)
        }
    }
    fun getAssetsOnAlgoNt(): AssetsInfo? {
        // val envUrlPath = getCustomPath()
        logger.info("getAssetsOnAlgoNt indexer url: ${algoCustomTokenConfiguration.bcIndexerUrl} ")
        try {
            val assetsInfo = restTemplate.getForObject(
                UriComponentsBuilder.fromHttpUrl(algoCustomTokenConfiguration.bcIndexerUrl)
                    .path("/v2/assets")
                    .build().toUri(),
                AssetsInfo::class.java
            )
            logger.info("getAssetsOnAlgoNt response : $assetsInfo ")
            return assetsInfo!!
        } catch (e: Exception) {
            logger.info("Error while calling getAssetsOnAlgoNt: ${e.message}")
            apisLoggerRepository.save(
                ApisLog(
                    "getAssetsOnAlgoNt",
                    "getAssetsOnAlgoNt:- ",
                    "",
                    e.message.toString()
                )
            )
            logger.error("Error while calling getAssetsOnAlgoNt", e)
            throw EntityNotFoundException(ErrorCodes.BLOCKCHAIN_SERVICE_NOT_FOUND)
        }
    }

    fun getCustomPath(): String {
        if (appConfig.environment.contains("dev", true)) {
            return "gdev"
        }
        if (appConfig.environment.contains("test", true)) {
            return "gtest"
        }
        if (appConfig.environment.contains("uat", true)) {
            return "guat"
        }
        if (appConfig.environment.contains("prod", true)) {
            return "gprod"
        }
        return ""
    }

    @Retryable(
        value = [SocketException::class],
        maxAttemptsExpression = "\${retry-policy.retryCount}",
        backoff = Backoff(delayExpression = "\${retry-policy.retryInterval}")
    )
    fun algoBalancesForAll(algoAddress: String): List<AlgoTokenAllBalanceItemResponse>? {
        val envUrlPath = getCustomPath()
        logger.info("algo wallet algoBalancesForAll : $algoAddress")
        val algoTokenBalanceReq = AlgoTokenBalanceReq(algoAddress)
        try {
            val algoResp1 = restTemplate.postForObject(
                UriComponentsBuilder.fromHttpUrl(algoCustomTokenConfiguration.baseUrl)
                    .pathSegment(algoApiVersion)
                    .pathSegment(envUrlPath)
                    .path("/balance")
                    .build().toUri(),
                algoTokenBalanceReq,
                AlgoTokenAllBalanceResponse::class.java
            )
            logger.info("algo wallet algoBalancesForAll response :$algoResp1")
            return algoResp1?.Balances
        } catch (e: RestClientResponseException) {
            logger.error(e.statusText, e)
            throw EntityNotFoundException(ErrorCodes.BLOCKCHAIN_SERVICE_NOT_FOUND)
        }
    }

    fun optInforCustomToken(toAcc: String, asset: String): String {
        val envUrlPath = getCustomPath()
        logger.info("optInforCustomToken : $toAcc with asset : $asset")
        val algoCustomTokenOptInRequest =
            AlgoCustomTokenOptInRequest(Address = toAcc, AssetName = asset)
        logger.info("creating optInforCustomToken request...")
        try {
            val algoTokenToAccResp = restTemplate.postForObject(
                UriComponentsBuilder.fromHttpUrl(algoCustomTokenConfiguration.baseUrl)
                    .pathSegment(algoApiVersion)
                    .pathSegment(envUrlPath)
                    .path("/optin")
                    .build().toUri(),
                algoCustomTokenOptInRequest,
                String::class.java
            )
            // logger.info("optInforCustomToken response : $algoTokenToAccResp ")
            // return algoTokenToAccResp!!.transactionId
            logger.info("optin success")
            return "success"
        } catch (e: RestClientResponseException) {
            apisLoggerRepository.save(
                ApisLog(
                    "optInforCustomToken",
                    "optInforCustomToken:- ",
                    "",
                    e.message.toString()
                )
            )
            logger.info("optin Fail")
            logger.error(e.statusText, e)
            // throw EntityNotFoundException(ErrorCodes.BLOCKCHAIN_SERVICE_NOT_FOUND)
            return "Fail"
        }
    }
    @Retryable(
        value = [SocketException::class],
        maxAttemptsExpression = "\${retry-policy.retryCount}",
        backoff = Backoff(delayExpression = "\${retry-policy.retryInterval}")
    )
    fun createNoOptInAddress(): String? {
        val envUrlPath = getCustomPath()
        logger.info(
            " url :" + UriComponentsBuilder.fromHttpUrl(algoCustomTokenConfiguration.baseUrl)
                .pathSegment(algoApiVersion)
                .pathSegment(envUrlPath)
                .path("/newaccountnooptin")
                .build().toUri()
        )
        try {
            val algoResp = restTemplate.getForObject(
                UriComponentsBuilder.fromHttpUrl(algoCustomTokenConfiguration.baseUrl)
                    .pathSegment(algoApiVersion)
                    .pathSegment(envUrlPath)
                    .path("/newaccountnooptin")
                    .build().toUri(),
                AlgoCustomTokenAccountResponse::class.java
            )
            logger.info("NoOptIn address details for algo custom token :" + algoResp.toString())
            logger.info("NoOptIn address details for algo custom token :" + algoResp!!.Address)
            logger.info("NoOptIn address details for algo custom token :" + algoResp.passphrase)
            logger.info(" db save : NoOptIn address details for algo custom token :$algoResp")
            algorandAddressRepository.save(AlgorandAddress(algoAddress = algoResp.Address, algoMnu = algoResp.passphrase))
            return algoResp.Address
        } catch (e: RestClientResponseException) {
            apisLoggerRepository.save(
                ApisLog(
                    "AlgoCutomAddress",
                    "createNoOptInAddress",
                    "emailone",
                    e.message.toString()
                )
            )
            logger.info(e.toString())
            logger.error(e.statusText, e)
        }
        return null
    }
    fun createAlgoCustomToken(
        TokenName: String,
        creatorAddress: String,
        NoOfTokens: String,
        NoOfDecimals: Int,
        assetUnit: String
    ): String? {
        val envUrlPath = getCustomPath()
        logger.info("createAlgoCustomToken : Token Name $TokenName , Creator : $creatorAddress, no of tokens : $NoOfTokens , no of Demicals : $NoOfDecimals")
        val algoCustomTokenCreateRequest =
            AlgoCustomTokenCreateRequest(AssetName = TokenName, Creator = creatorAddress, Decimals = NoOfDecimals, TotalIssuance = BigInteger(NoOfTokens), UnitName = assetUnit)
        logger.info("creating createAlgoCustomToken request...")
        try {
            val algoCustomTokenCreateResponse = restTemplate.postForObject(
                UriComponentsBuilder.fromHttpUrl(algoCustomTokenConfiguration.baseUrl)
                    .pathSegment(algoApiVersion)
                    .pathSegment(envUrlPath)
                    .path("/createasset")
                    .build().toUri(),
                algoCustomTokenCreateRequest,
                AlgoCustomTokenCreateResponse::class.java
            )
            logger.info("createAlgoCustomToken response : $algoCustomTokenCreateResponse ")
            return algoCustomTokenCreateResponse!!.AssetId.toString()
        } catch (e: RestClientResponseException) {
            apisLoggerRepository.save(
                ApisLog(
                    "createAlgoCustomToken",
                    "createAlgoCustomToken:- ",
                    "",
                    e.message.toString()
                )
            )
            logger.error(e.statusText, e)
            logger.info(e.message)
            throw EntityNotFoundException(ErrorCodes.BLOCKCHAIN_SERVICE_NOT_FOUND)
            // return "Fail"
        }
    }

    @Retryable(
        value = [SocketException::class],
        maxAttemptsExpression = "\${retry-policy.retryCount}",
        backoff = Backoff(delayExpression = "\${retry-policy.retryInterval}")
    )
    fun algoCutomTokenTransferByAssetId(sender: String, receiver: String, unitValue: String, assetId: String): String? {
        val envUrlPath = getCustomPath()
        // val sendmaster ="edit random width clap crowd swamp pride original mushroom decade math quick apology march endorse prison lab supply wave undo obscure auto armed absorb try"
        logger.info("creagting algoCutomTokenTransferByAssetId ReceiverAddress : $receiver , SendrMnemonic : $sender , AssetId: $assetId , Amount : $unitValue")
        val algoCustomTokenTransferRequestById =
            AlgoCustomTokenTransferRequestById(ReceiverAddress = receiver, AssetId = assetId, SendrAddress = sender, Amount = unitValue)
        logger.info("creagting algoCutomTokenTransferByAssetId request...")
        try {
            // transfer
            val algoTokenResp = restTemplate.postForObject(
                UriComponentsBuilder.fromHttpUrl(algoCustomTokenConfiguration.baseUrl)
                    .pathSegment(algoApiVersion)
                    .pathSegment(envUrlPath)
                    .path("/transferassetbyid")
                    .build().toUri(),
                algoCustomTokenTransferRequestById,
                AlgoCustomTokenTransferResponse::class.java
            )
            logger.info("algo custom token Transfer by assetId : $algoTokenResp ")
            return algoTokenResp!!.transactionId
        } catch (e: RestClientResponseException) {
            apisLoggerRepository.save(
                ApisLog(
                    "algoCutomTokenTransferByAssetId",
                    "Transfer:- ",
                    "",
                    e.message.toString()
                )
            )
            logger.error(e.statusText, e)
            throw EntityNotFoundException(ErrorCodes.BLOCKCHAIN_SERVICE_NOT_FOUND)
        }
    }

    @Retryable(
        value = [SocketException::class],
        maxAttemptsExpression = "\${retry-policy.retryCount}",
        backoff = Backoff(delayExpression = "\${retry-policy.retryInterval}")
    )
    fun algoCustomTokenTransferByAssetId(sender: String, receiver: String, unitValue: String, assetId: String): String {
        val envUrlPath = getCustomPath()
        // val sendmaster ="edit random width clap crowd swamp pride original mushroom decade math quick apology march endorse prison lab supply wave undo obscure auto armed absorb try"
        logger.info("creagting algoCutomTokenTransferByAssetId ReceiverAddress : $receiver , SendrMnemonic : $sender , AssetId: $assetId , Amount : $unitValue")
        val algoCustomTokenTransferRequestById =
            AlgoCustomTokenTransferRequestById(ReceiverAddress = receiver, AssetId = assetId, SendrAddress = sender, Amount = unitValue)
        logger.info("creagting algoCutomTokenTransferByAssetId request...")
        try {
            // transfer
            val algoTokenResp = restTemplate.postForObject(
                UriComponentsBuilder.fromHttpUrl(algoCustomTokenConfiguration.baseUrl)
                    .pathSegment(algoApiVersion)
                    .pathSegment(envUrlPath)
                    .path("/transferassetbyid")
                    .build().toUri(),
                algoCustomTokenTransferRequestById,
                AlgoCustomTokenTransferResponse::class.java
            )
            logger.info("algo custom token Transfer by assetId : $algoTokenResp ")
            return algoTokenResp!!.transactionId
        } catch (e: RestClientResponseException) {
            apisLoggerRepository.save(
                ApisLog(
                    "algoCutomTokenTransferByAssetId",
                    "Transfer:- ",
                    "",
                    e.message.toString()
                )
            )
            logger.error(e.statusText, e)
            return ErrorCodes.BLOCKCHAIN_SERVICE_NOT_FOUND
        }
    }

    fun getAssetInfoByIndexId(indexId: String): SingleAssetInfo? {
        // val envUrlPath = getCustomPath()
        logger.info("getAssetsOnAlgoNt indexer url: ${algoCustomTokenConfiguration.bcIndexerUrl} ")
        try {
            val indexAssetInfo = restTemplate.getForObject(
                UriComponentsBuilder.fromHttpUrl(algoCustomTokenConfiguration.bcIndexerUrl)
                    .path("/v2/assets/$indexId")
                    .build().toUri(),
                SingleAssetInfo::class.java
            )
            logger.info("getAssetInfoByIndexId response : $indexAssetInfo ")
            return indexAssetInfo!!
        } catch (e: RestClientResponseException) {
            apisLoggerRepository.save(
                ApisLog(
                    "getAssetInfoByIndexId",
                    "getAssetInfoByIndexId:- ",
                    "",
                    e.message.toString()
                )
            )
            logger.error(e.statusText, e)
            throw EntityNotFoundException(ErrorCodes.BLOCKCHAIN_SERVICE_NOT_FOUND)
        }
    }
    fun createCryptoAddressNoOptIn(asset: String): CryptoAddress? {
        val envUrlPath = getCustomPath()
        logger.info(
            " url :" + UriComponentsBuilder.fromHttpUrl(algoCustomTokenConfiguration.baseUrl)
                .pathSegment(algoApiVersion)
                .pathSegment(envUrlPath)
                .path("/newaccountnooptin")
                .build().toUri()
        )
        try {
            var address: String? = null
            logger.info(" useJAVAAlgoSDK : " + algoCustomTokenConfiguration.useJAVAAlgoSDK)
            if (algoCustomTokenConfiguration.useJAVAAlgoSDK) {
                /* BC address using algo JAVA SDK*/
                address = createCryptoAddressUsingAlgoSDK(asset)
                logger.info(" Algo address from Algo JAVA SDK : " + address)
            } else {
                val algoResp = restTemplate.getForObject(
                    UriComponentsBuilder.fromHttpUrl(algoCustomTokenConfiguration.baseUrl)
                        .pathSegment(algoApiVersion)
                        .pathSegment(envUrlPath)
                        .path("/newaccountnooptin")
                        .build().toUri(),
                    AlgoCustomTokenAccountResponse::class.java
                )
                address = algoResp?.Address // algoResp.passphrase
                logger.info(" db save : address details for algo custom token :$algoResp")
            }
            /*logger.info("NoOptIn address details for algo custom token :" + algoResp.toString())
            logger.info("NoOptIn address details for algo custom token :" + algoResp!!.Address)
            logger.info("NoOptIn address details for algo custom token :" + algoResp.passphrase)
            logger.info(" db save : NoOptIn address details for algo custom token :$algoResp")
            algorandAddressRepository.save(AlgorandAddress(algoAddress = algoResp.Address, algoMnu = algoResp.passphrase))
            */
            val cryptoAdres = saveCryptoAdd(asset, address!!)
            return cryptoAdres
            // return algoResp.Address
        } catch (e: RestClientResponseException) {
            apisLoggerRepository.save(
                ApisLog(
                    "AlgoCutomAddress",
                    "createCryptoAddressNoOptIn",
                    "emailone",
                    e.message.toString()
                )
            )
            logger.info(e.toString())
            logger.error(e.statusText, e)
        }
        return null
    }

    fun createCryptoAddressUsingAlgoSDK(asset: String): String {
        try {
            val acc = Account()
            val algoMnu = acc.toMnemonic()
            val algoAddress = acc.address
            logger.info(" db save : address details for algo custom token :$algoAddress")
            val algorandAddress = AlgorandAddress(algoAddress = algoAddress.toString(), algoMnu = algoMnu)
            val id = algorandAddressRepository.save(algorandAddress)
            logger.info(" db save : algorandAddressRepository  :$id")
            return algoAddress.toString()
        } catch (e: Exception) {
            apisLoggerRepository.save(
                ApisLog(
                    "createCryptoAddressUsingAlgoSDK",
                    "createCryptoAddressUsingAlgoSDK",
                    "",
                    e.message.toString() + " asset ==> " + asset
                )
            )
            logger.info(e.toString())
            throw EntityNotFoundException(ErrorCodes.NO_OPTIN_ACCOUNT_CREATION_ERROR)
        }
    }
}
