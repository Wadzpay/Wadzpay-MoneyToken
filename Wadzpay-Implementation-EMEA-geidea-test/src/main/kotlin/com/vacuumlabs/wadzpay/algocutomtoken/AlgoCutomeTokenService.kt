package com.vacuumlabs.wadzpay.algocutomtoken

import com.vacuumlabs.wadzpay.algocutomtoken.models.AssetsInfoOnAlgoNt
import com.vacuumlabs.wadzpay.algocutomtoken.models.BcTransInfo
import com.vacuumlabs.wadzpay.algocutomtoken.models.WadzpayMinterRepository
import com.vacuumlabs.wadzpay.common.BadRequestException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.ServiceUnavailableException
import com.vacuumlabs.wadzpay.configuration.AppConfig
import com.vacuumlabs.wadzpay.issuance.models.IssuanceBanks
import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import com.vacuumlabs.wadzpay.ledger.LedgerService
import com.vacuumlabs.wadzpay.ledger.model.AccountOwner
import com.vacuumlabs.wadzpay.ledger.model.AlgorandAddress
import com.vacuumlabs.wadzpay.ledger.model.AlgorandAddressRepository
import com.vacuumlabs.wadzpay.ledger.model.CryptoAddress
import com.vacuumlabs.wadzpay.ledger.model.CryptoAddressRepository
import com.vacuumlabs.wadzpay.ledger.model.SubaccountRepository
import com.vacuumlabs.wadzpay.ledger.model.Transaction
import com.vacuumlabs.wadzpay.ledger.model.TransactionStatus
import com.vacuumlabs.wadzpay.ledger.model.TransactionType
import com.vacuumlabs.wadzpay.ledger.service.TransactionService
import com.vacuumlabs.wadzpay.merchant.model.FiatCurrencyUnit
import com.vacuumlabs.wadzpay.user.UserAccount
import com.vacuumlabs.wadzpay.utils.ApisLog
import com.vacuumlabs.wadzpay.utils.ApisLoggerRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.ResourceAccessException
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant

// import java.util.UUID

@Service
class AlgoCutomeTokenService(
    val algoCustomTokenWallet: AlgoCustomTokenWallet,
    val ledgerService: LedgerService,
    val apisLoggerRepository: ApisLoggerRepository,
    val appConfig: AppConfig,
    val transactionService: TransactionService,
    val cryptoAddressRepository: CryptoAddressRepository,
    val subaccountRepository: SubaccountRepository,
    val algorandAddressRepository: AlgorandAddressRepository,
    val algoCustomTokenCacheService: AlgoCustomTokenCacheService,
    val wadzpayMinterRepository: WadzpayMinterRepository
) {
    val logger: Logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun sendToExternalWalletWadzpayPrivateBlockChain(
        sender: AccountOwner,
        receiverAccount: AccountOwner,
        amountx: BigDecimal,
        asset: CurrencyUnit,
        address: String,
        description: String? = null,
        transactionType: TransactionType,
        refundFiatType: FiatCurrencyUnit? = null,
        refundAmountFiat: BigDecimal? = BigDecimal.ZERO,
        issuanceBanks: IssuanceBanks? = null,
        refundedTransactionId: String? = null
    ): Transaction {
        var amount = amountx.setScale(2, BigDecimal.ROUND_HALF_UP)
        val bcTransId: String?
        if (amount <= BigDecimal.ZERO) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
        }
        if (amount.stripTrailingZeros().scale() > asset.maximumNumberOfDigits.toInt()) {
            val am =
                amount.setScale(asset.maximumNumberOfDigits.toInt(), RoundingMode.UP).stripTrailingZeros()
            amount = am
        }
        val senderAddress = sender.account.getSubaccountByAsset(CurrencyUnit.SART).address?.address
        val receiverAddress = address
        if (senderAddress == null) {
            throw BadRequestException(ErrorCodes.ACCOUNT_NOT_FOUND)
        }
        apisLoggerRepository.save(
            ApisLog(
                "algoBCTransfer",
                amount.toString(),
                "Base amount: $amountx $asset",
                address
            )
        )
        try {
            bcTransId = algoCustomTokenWallet.algoCutomTokenTransfer(senderAddress, receiverAddress, amount.toString())
        } catch (ex: ResourceAccessException) {
            println("@94 $ex")
            logger.info(ex.message)
            ledgerService.returnMoney(sender, asset, amount)
            apisLoggerRepository.save(
                ApisLog(
                    "sendToExternalWalletWadzpayPrivateBlockChain",
                    ex.message.toString(),
                    sender.account.getOwnerName()!!,
                    address
                )
            )
            throw ServiceUnavailableException(ErrorCodes.WALLET_NOT_AVAILABLE)
        }
        val transaction = ledgerService.createSARTTransaction(
            sender,
            receiverAccount,
            amount,
            BigDecimal.ZERO,
            asset,
            bcTransId!!,
            description,
            status = TransactionStatus.SUCCESSFUL,
            transactionType,
            issuanceBanks,
            refundedTransactionId
        )
        transactionService.showPushNotifications(transaction)
        return transaction
    }

    fun createAlgoAddress(): CryptoAddress? {
        val algoaddress = algoCustomTokenWallet.createAddress(CurrencyUnit.SART.toString())
        return algoaddress
    }
    fun getAlgoTokenBalance(algoaddress: String): String? {
        val bal = algoCustomTokenWallet.algoTokenBalance(algoaddress)
        return bal
    }
    fun loadCustomTokensToAccount(toAccMnu: String, amt: String): String {
        try {
            val addressOfToAcc = algoCustomTokenWallet.accountInfoFromMnu(toAccMnu)!!.Address
            logger.info("Address of account : $addressOfToAcc")
            val transID = algoCustomTokenWallet.loadCustomTokensToAccount(addressOfToAcc, amt)
            logger.info("transaction id for loadCustomTokensToAccount : $transID")
            return transID
        } catch (e: Exception) {
            throw e
        }
    }
    fun refundUnspentCustomTokens(userAddressMnu: String, amt: String): String? {
        try {
            val transId = algoCustomTokenWallet.refundUnspentCustomTokens(userAddressMnu, amt)
            return transId
        } catch (e: Exception) {
            throw e
        }
    }
    fun accountAssetBalance(userAddressMnu: String): String? {
        return algoCustomTokenWallet.accountInfoFromMnu(userAddressMnu)!!.AssetBalance
    }
    fun getSubAccountByAddress(address: String, assetType: CurrencyUnit): AccountOwner {
        val cryptoAddress = cryptoAddressRepository.getByAddressAndAsset(address, assetType.toString())
        println("cryptoAddress ==>" + cryptoAddress)
        if (cryptoAddress != null) {
            val subAccount = subaccountRepository.getByAddress(cryptoAddress)
            println("subAccount ==>" + subAccount)
            if (subAccount != null) {
                println("subAccount ==>" + subAccount.account.owner)
            }
            if (subAccount != null && subAccount.account.owner != null) {
                return subAccount.account.owner!!
            }
            throw ServiceUnavailableException(ErrorCodes.INVALID_WALLET_ADDRESS)
        }
        throw ServiceUnavailableException(ErrorCodes.INVALID_WALLET_ADDRESS)
    }
    fun algoTransactionInfo(transId: String): BcTransInfo? {
        return algoCustomTokenWallet.getTransactionInfo(transId)
    }
    fun saveAlgorandAddress(adrs: String, mnu: String): Long? {
        val algorandAddress = AlgorandAddress(algoAddress = adrs, algoMnu = mnu)
        val id = algorandAddressRepository.save(algorandAddress)
        return id!!.id
    }

    fun findAlgorandAddress(adrs: String): AlgorandAddress? {
        return algorandAddressRepository.getByAlgoAddress(adrs)
    }

    @Transactional
    fun sendToPrivateBlockChainMerchant(
        sender: AccountOwner,
        receiverAccount: AccountOwner,
        amountx: BigDecimal,
        asset: CurrencyUnit,
        address: String,
        description: String? = null,
        transactionType: TransactionType,
        refundFiatType: FiatCurrencyUnit? = null,
        refundAmountFiat: BigDecimal? = BigDecimal.ZERO,
        issuanceBanks: IssuanceBanks? = null
    ): Transaction {
        var amount = amountx.setScale(2, BigDecimal.ROUND_HALF_UP)
        val bcTransId: String?
        logger.info("@203 calling sendToPrivateBlockChainMerchant ")
        println("@203 sendToPrivateBlockChainMerchant")
        if (amount <= BigDecimal.ZERO) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
        }
        if (amount.stripTrailingZeros().scale() > asset.maximumNumberOfDigits.toInt()) {
            val am =
                amount.setScale(asset.maximumNumberOfDigits.toInt(), RoundingMode.UP).stripTrailingZeros()
            amount = am
            //  throw BadRequestException(ErrorCodes.INVALID_AMOUNT_TOO_MANY_DECIMAL_PLACES)
        }
        // algoCustomTokenWallet.algoCutomTokenTransfer("RXTLUC7DCS3RESAPCBWXUTFZS5PLF2R6FVGXCC7CWKFXGI3MZS7VH7QPIY", "SEXB4HGK2KJYU3YJLTOSFCQEOK22P5Q23SAF4LPOHIV5ZICDGFKBR6DHSA",amount.toString())

        val senderAddress = sender.account.getSubaccountByAsset(CurrencyUnit.SART).address?.address
        // sender.accounts[0].getSubaccountByAsset(CurrencyUnit.SART).address?.address
        val receiverAddress = address
        if (senderAddress == null) {
            logger.info("@220 Account not found")
            println("@220 Account not found")
            throw BadRequestException(ErrorCodes.ACCOUNT_NOT_FOUND)
        }
        apisLoggerRepository.save(
            ApisLog(
                "algoBCTransfer",
                amount.toString(),
                "Base amount: $amountx $asset",
                address
            )
        )
        try {
            logger.info("@233 transferring SART...")
            println("@233 transferring SART")
// val recAddress = algoCustomTokenWallet.accountInfoFromMnu(receiverAddress)!!.Address
            // val balanceofsender = algoCustomTokenWallet.algoTokenBalance(senderAddress)
            // logger.info("balance of Sendr : $balanceofsender")
            bcTransId = algoCustomTokenWallet.algoCutomTokenTransfer(senderAddress, receiverAddress, amount.toString())
        } catch (ex: ResourceAccessException) {
            println("@239 $ex")
            logger.info(ex.message)
            ledgerService.returnMoney(sender, asset, amount)
            apisLoggerRepository.save(
                ApisLog(
                    "sendToExternalWalletWadzpayPrivateBlockChain",
                    ex.message.toString(),
                    sender.account.getOwnerName()!!,
                    address
                )
            )
            throw ServiceUnavailableException(ErrorCodes.WALLET_NOT_AVAILABLE)
        }
        val transaction = ledgerService.createSARTTransactionMerchant(
            sender,
            receiverAccount,
            amount,
            BigDecimal.ZERO,
            asset,
            bcTransId!!,
            description,
            status = TransactionStatus.SUCCESSFUL,
            transactionType,
            issuanceBanks
        )
        transactionService.showPushNotifications(transaction)
        println("@265 transaction")
        return transaction
    }

    fun getAssetsOnAlgoNt(): ArrayList<AssetsInfoOnAlgoNt> {
        var AssetsInfoOnAlgoNtList = ArrayList<AssetsInfoOnAlgoNt>()
        var assetsInfo = algoCustomTokenWallet.getAssetsOnAlgoNt()
        // logger.info("getAssetsOnAlgoNt from Service")
        for (asset in assetsInfo!!.assets) {
            AssetsInfoOnAlgoNtList.add(
                AssetsInfoOnAlgoNt(
                    asset.index,
                    asset.params.name,
                    asset.params.unitName,
                    asset.params.decimals
                )
            )
            // logger.info("inside loop")
        }
        return AssetsInfoOnAlgoNtList
    }

    fun getBalanceForAsset(address: String, assetType: String): String? {
        var assetBalances = algoCustomTokenWallet.algoBalancesForAll(address)
        if (assetBalances != null) {
            for (assetBal in assetBalances) {
                // logger.info("inside loop asset balance :${assetBal.Assetbalance} and asset name : ${assetBal.Assetname} " )
                if (assetBal.Assetname.compareTo(assetType, true) == 0) {
                    return assetBal.Assetbalance
                }
            }
        }
        return null
    }

    fun getUserAccountFromWalletAddress(walletUserAddress: String): UserAccount {
        val cryptoAddress = cryptoAddressRepository.getByAddressAndAsset(walletUserAddress, CurrencyUnit.SART.toString())
//        println("cryptoAddress@306 ==>$cryptoAddress")
        if (cryptoAddress != null) {
            val subAccount = subaccountRepository.getByAddress(cryptoAddress)
//            println("subAccount @309 ==> $subAccount")
//            println("subAccount @310 ==>" + subAccount?.account?.owner)
            var userAccountVar = subAccount?.account?.owner as UserAccount
            return userAccountVar
        }
        throw BadRequestException(ErrorCodes.INVALID_WALLET_ADDRESS)
    }

    fun getNoOptNewAlgoAddress(): String? {
        return algoCustomTokenWallet.createNoOptInAddress()
    }

    fun getNewAlgoToken(
        newTokenName: String,
        createtorAddress: String,
        totalTokens: String,
        fractions: Int,
        assetUnit: String
    ): String? {
        return algoCustomTokenWallet.createAlgoCustomToken(newTokenName, createtorAddress, totalTokens, fractions, assetUnit)
    }

    fun loadAssetsFromAlgoNetworkToCache(): String? {
        val assetsOnAlgoNt = algoCustomTokenWallet.getAssetsOnAlgoNt()
        algoCustomTokenCacheService.clearAssets()
        for (assetItem in assetsOnAlgoNt!!.assets) {
            // logger.info(" Index: ${assetItem.index} , asset Name: ${assetItem.params.name}, asset Unit Name: ${assetItem.params.unitName}, asset Decimals: ${assetItem.params.decimals} ")
            algoCustomTokenCacheService.setCustomTokenName(assetItem.params.name)
            algoCustomTokenCacheService.setCustomTokenIndex(assetItem.params.name, assetItem.index.toString())
            algoCustomTokenCacheService.setCustomTokenDecimals(
                assetItem.params.name,
                assetItem.params.decimals.toString()
            )
        }
        return "success"
    }

    fun refreshCacheWithPvtNtAssets(): String? {
        if (algoCustomTokenCacheService.getCustomTokenCachedStatus()) {
            return "success"
        }
        val status = loadAssetsFromAlgoNetworkToCache()
        algoCustomTokenCacheService.setCustomTokenCachedStatus(true)
        return status
    }

    fun getAssetsOnAlgoNtFromCache(): ArrayList<AssetsInfoOnAlgoNt> {
        var AssetsInfoOnAlgoNtList = ArrayList<AssetsInfoOnAlgoNt>()
        var AssetName = "SART"
        println("Test for AssetName " + AssetName)
        algoCustomTokenCacheService.setCustomTokenCachedStatus(false)
        refreshCacheWithPvtNtAssets()
        val AssetIndex = algoCustomTokenCacheService.getCustomTokenIndex(AssetName)!!
        val AssetDecimals = algoCustomTokenCacheService.getCustomTokenDecimals(AssetName)!!
        println("AssetDecimals  for SART ==>" + AssetDecimals)
        AssetsInfoOnAlgoNtList.add(AssetsInfoOnAlgoNt(AssetIndex.toInt(), AssetName, AssetName, AssetDecimals.toInt()))
        return AssetsInfoOnAlgoNtList
    }

    fun algoCustomTokenTransferFromMinter(userAddress: String, tokenName: String, noOfTokens: BigDecimal): String? {
        var senderAddress: String? = null
        val wadzpayMinter = wadzpayMinterRepository.getByAssetName(tokenName)
        if (wadzpayMinter != null) {
            senderAddress = wadzpayMinter.wadzpayMinterAddress
        } else {
            val AssetIndex = algoCustomTokenCacheService.getCustomTokenIndex(tokenName)!!
            senderAddress = getAssetMinterByAssetIndex(AssetIndex)
        }
        println("senderAddress ==> " + senderAddress)
        println(algoCustomTokenCacheService.getCustomTokenCachedStatus())
        if (!algoCustomTokenCacheService.getCustomTokenCachedStatus()) {
            loadAssetsFromAlgoNetworkToCache()
        }
        val AssetIndex = algoCustomTokenCacheService.getCustomTokenIndex(tokenName)!!
        val AssetDecimals = algoCustomTokenCacheService.getCustomTokenDecimals(tokenName)!!
        if (senderAddress != null) {
            return algoCustomTokenWallet.algoCustomTokenTransferByAssetId(
                senderAddress,
                userAddress,
                noOfTokens.toString(),
                AssetIndex
            )
        }
        return ErrorCodes.WALLET_ADDRESS_NOT_EXIST
    }

    fun algoCustomTokenTransferByAssetId(senderAddress: String, recipientAddress: String, tokenName: String, noOfTokens: BigDecimal): String {
        println(algoCustomTokenCacheService.getCustomTokenCachedStatus())
        if (!algoCustomTokenCacheService.getCustomTokenCachedStatus()) {
            loadAssetsFromAlgoNetworkToCache()
        }
        val AssetIndex = algoCustomTokenCacheService.getCustomTokenIndex(tokenName)!!
        val AssetDecimals = algoCustomTokenCacheService.getCustomTokenDecimals(tokenName)!!
        return algoCustomTokenWallet.algoCustomTokenTransferByAssetId(
            senderAddress,
            recipientAddress,
            noOfTokens.toString(),
            AssetIndex
        )
    }

    fun getAssetMinterByAssetIndex(indexId: String): String? {
        println("indexId ==> $indexId")
        val minterAddress = algoCustomTokenWallet.getAssetInfoByIndexId(indexId)!!.asset.params.creator
        logger.info("minter address for asset ID $indexId is $minterAddress")
        return minterAddress
    }

    fun getMinterWalletAdress(tokenName: String): String? {
        var minterAddress: String? = null
        val wadzpayMinter = wadzpayMinterRepository.getByAssetName(tokenName)
        minterAddress = if (wadzpayMinter != null) {
            wadzpayMinter.wadzpayMinterAddress
        } else {
            val indexId = algoCustomTokenCacheService.getCustomTokenIndex(tokenName)!!
            getAssetMinterByAssetIndex(indexId)
        }
        return minterAddress
    }

    fun createAddressWithOptin(asset: String): CryptoAddress? {

        val algoaddress = algoCustomTokenWallet.createCryptoAddressNoOptIn(asset)

        logger.info("algoaddress ==> " + algoaddress!!.address)
        logger.info("Time algo address creation optin start ==> " + Instant.now())
        try {
            // Thread.sleep(1_000)
            algoCustomTokenWallet.optInforCustomToken(algoaddress.address, asset)
        } catch (ex: Exception) {
            logger.info("createAddressWithOptin excepetion " + ex.toString())
        }
        logger.info("Time algo address creation optin end==> " + Instant.now())
        return algoaddress
    }

    fun p2pTransferByWadzpayPrivateBlockChain(
        sender: UserAccount,
        receiverAccount: UserAccount,
        amount: BigDecimal,
        asset: CurrencyUnit,
        address: String,
        transactionType: TransactionType,
        issuanceBanks: IssuanceBanks,
        status: TransactionStatus
    ): Transaction {
        var bcTransId: String? = null
        val senderAddress = sender.account.getSubaccountByAsset(CurrencyUnit.SART).address?.address
        val receiverAddress = address
        if (senderAddress == null) {
            throw BadRequestException(ErrorCodes.ACCOUNT_NOT_FOUND)
        }
        apisLoggerRepository.save(
            ApisLog(
                "algoBCTransfer",
                amount.toString(),
                "Base amount: $amount $asset",
                address
            )
        )
        try {
            if (status == TransactionStatus.SUCCESSFUL) {
                bcTransId =
                    algoCustomTokenWallet.algoCutomTokenTransfer(senderAddress, receiverAddress, amount.toString())
            }
        } catch (ex: ResourceAccessException) {
            println("@94 $ex")
            logger.info(ex.message)
            ledgerService.returnMoney(sender, asset, amount)
            apisLoggerRepository.save(
                ApisLog(
                    "sendToExternalWalletWadzpayPrivateBlockChain",
                    ex.message.toString(),
                    sender.account.getOwnerName()!!,
                    address
                )
            )
            throw ServiceUnavailableException(ErrorCodes.WALLET_NOT_AVAILABLE)
        }
        val transaction = ledgerService.createSARTTransaction(
            sender,
            receiverAccount,
            amount,
            BigDecimal.ZERO,
            asset,
            bcTransId,
            null,
            status = status,
            transactionType,
            issuanceBanks
        )
        transactionService.showPushNotifications(transaction)
        return transaction
    }
}
