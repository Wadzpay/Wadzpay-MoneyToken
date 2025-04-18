package com.vacuumlabs.wadzpay.ledger

import com.vacuumlabs.vuba.ledger.api.dsl.CommitBuilder
import com.vacuumlabs.vuba.ledger.api.dsl.commit
import com.vacuumlabs.vuba.ledger.common.DualReference
import com.vacuumlabs.vuba.ledger.common.Reference
import com.vacuumlabs.vuba.ledger.service.LedgerService
import com.vacuumlabs.wadzpay.ledger.model.Account
import com.vacuumlabs.wadzpay.ledger.model.AccountRepository
import com.vacuumlabs.wadzpay.ledger.model.AccountType
import com.vacuumlabs.wadzpay.ledger.model.Subaccount
import com.vacuumlabs.wadzpay.ledger.model.SubaccountRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.UUID

enum class CurrencyUnit(val maximumNumberOfDigits: Long) {

    WTK(18L),
    BTC(8L),
    ETH(18L),
    USDC(6L),
    USDT(6L),
    ALGO(6L),
    USDCA(6L),
    SART(6L);

    fun validAmount(amount: BigDecimal): Boolean {
        return maxOf(0, amount.stripTrailingZeros().scale()) <= maximumNumberOfDigits
    }

    fun scalingFactor(reverse: Boolean = false): BigDecimal {
        val factor = BigDecimal(10).pow(maximumNumberOfDigits.toInt())
        return if (reverse) {
            BigDecimal.ONE.divide(factor)
        } else {
            factor
        }
    }

    fun amountToBaseUnits(amount: BigDecimal): String {
        return amount.multiply(scalingFactor()).toBigInteger().toString()
    }

    fun onEthBlockchain(): Boolean {
        return this == ETH || this == USDT || this == WTK || this == USDC
    }

    /*  fun onAlgoBlockchain(): Boolean {
          return this == ALGO || this == USDCA
      }
  */

    fun onAlgoUSDCBlockchain(): Boolean {
        return this == USDCA
    }

    fun onAlgoOnlyBlockchain(): Boolean {
        return this == ALGO
    }

    fun onWadzpayBlockChain(): Boolean {
        return this == SART
    }
}

object ReferenceConventions {
    object Wadzpay {
        const val prefix = "wadzpay"
        fun omnibus() = Reference("$prefix.omnibus")
        fun feeCollection() = Reference("$prefix.fee_collection")

        fun omnibus(assetId: String) = DualReference(omnibus(), Reference(assetId))
        fun feeCollection(assetId: String) = DualReference(feeCollection(), Reference(assetId))
    }

    object Account {
        /** Generated [Reference] for the associated ledger account of a new [Account] **/
        fun account(userAccountId: Long, type: AccountType) =
            Reference(accountTypePrefix(type) + "mvp.user.$userAccountId")

        /** Generated [DualReference] for the associated primary ledger subaccount of a new [Account] **/
        fun subaccount(userAccountId: Long, asset: String, type: AccountType = AccountType.MAIN) =
            DualReference(account(userAccountId, type), Reference(asset))

        /** Generated [Reference] for the associated creation commit of a new [Account] **/
        fun createCommit(userAccountId: Long, type: AccountType) =
            Reference(accountTypePrefix(type) + "commit.create.$userAccountId")

        private fun accountTypePrefix(type: AccountType) = if (type == AccountType.MAIN) "" else "$type."
    }

    /** Generated [Reference] for a simple transaction commit **/
    fun commit(id: UUID): Reference = Reference("commit.$id.transaction")
    fun commitPOS(id: UUID): Reference = Reference("pos.$id.transaction")
}

@Service
class LedgerInitializerService(
    val ledgerService: LedgerService,
    val accountRepository: AccountRepository,
    val subaccountRepository: SubaccountRepository
) {
    fun CommitBuilder.createAssetSubaccounts(assets: List<String>, subaccount: (String) -> DualReference) {
        assets.forEach { asset ->
            subaccountDeclaration(reference = subaccount(asset), asset = asset, zero = true)
        }
    }

    fun CommitBuilder.createAssetSubaccounts(asset: String, subaccount: (String) -> DualReference) {
        subaccountDeclaration(reference = subaccount(asset), asset = asset, zero = true)
    }

    fun isWTKAdded() = ledgerService.getAsset("WTK").isPresent
    fun isALGOAdded() =
        ledgerService.getAsset("ALGO").isPresent && ledgerService.getAccount(ReferenceConventions.Wadzpay.omnibus()).isPresent

    fun isUSDCAdded() =
        ledgerService.getAsset("USDC").isPresent && ledgerService.getAccount(ReferenceConventions.Wadzpay.omnibus()).isPresent

    fun isUSDCaAdded() =
        ledgerService.getAsset("USDCA").isPresent && ledgerService.getAccount(ReferenceConventions.Wadzpay.omnibus()).isPresent

    fun isSARaAdded() =
        ledgerService.getAsset("SART").isPresent && ledgerService.getAccount(ReferenceConventions.Wadzpay.omnibus()).isPresent

    fun isInitialized() = ledgerService.getAccount(ReferenceConventions.Wadzpay.omnibus()).isPresent

    fun createAssets() {
        ledgerService.createAsset(CurrencyUnit.WTK.name, CurrencyUnit.WTK.scalingFactor(true))
        ledgerService.createAsset(CurrencyUnit.BTC.name, CurrencyUnit.BTC.scalingFactor(true))
        ledgerService.createAsset(CurrencyUnit.ETH.name, CurrencyUnit.ETH.scalingFactor(true))
        ledgerService.createAsset(CurrencyUnit.USDT.name, CurrencyUnit.USDT.scalingFactor(true))
        ledgerService.createAsset(CurrencyUnit.USDC.name, CurrencyUnit.USDC.scalingFactor(true))
        ledgerService.createAsset(CurrencyUnit.ALGO.name, CurrencyUnit.ALGO.scalingFactor(true))
        ledgerService.createAsset(CurrencyUnit.USDCA.name, CurrencyUnit.USDCA.scalingFactor(true))
        ledgerService.createAsset(CurrencyUnit.SART.name, CurrencyUnit.SART.scalingFactor(true))
    }

    fun createAccounts() {
        val allAssets = CurrencyUnit.values()
        listOf(
            ReferenceConventions.Wadzpay.omnibus(), ReferenceConventions.Wadzpay.feeCollection()
        ).forEach { ledgerService.createAccount(it) }

        val omnibusBusiness = accountRepository.save(Account(reference = ReferenceConventions.Wadzpay.omnibus()))
        val feeCollectionBusiness =
            accountRepository.save(Account(reference = ReferenceConventions.Wadzpay.feeCollection()))

        ledgerService.createCommit(
            commit {
                reference("commit.wadzpay.initial")
                createAssetSubaccounts(allAssets.map { it.name }) { ReferenceConventions.Wadzpay.omnibus(it) }
                createAssetSubaccounts(allAssets.map { it.name }) {
                    ReferenceConventions.Wadzpay.feeCollection(it)
                }
            }
        )

        allAssets.forEach {
            subaccountRepository.save(
                Subaccount(
                    account = omnibusBusiness, reference = ReferenceConventions.Wadzpay.omnibus(it.name), asset = it.toString()
                )
            )

            subaccountRepository.save(
                Subaccount(
                    account = feeCollectionBusiness,
                    reference = ReferenceConventions.Wadzpay.feeCollection(it.name),
                    asset = it.toString()
                )
            )
        }
    }

    fun createAccountsFor(currencyUnit: CurrencyUnit) {
        val logger: Logger = LoggerFactory.getLogger(javaClass)

        try {
            ledgerService.createAsset(currencyUnit.name, currencyUnit.scalingFactor(true))
        } catch (e: Exception) {
            logger.info(e.message)
        }
        val omnibusBusinessRef = accountRepository.getByReference(ReferenceConventions.Wadzpay.omnibus())
        val feeCollectionBusinessRef = accountRepository.getByReference(ReferenceConventions.Wadzpay.feeCollection())

        logger.info(omnibusBusinessRef.reference.toString())
        logger.info(feeCollectionBusinessRef.getOwnerName())

        try {
            ledgerService.createCommit(
                commit {
                    reference("commit.wadzpay.initial")
                    createAssetSubaccounts(currencyUnit.name) { ReferenceConventions.Wadzpay.omnibus(it) }
                    createAssetSubaccounts(currencyUnit.name) { ReferenceConventions.Wadzpay.feeCollection(it) }
                },
                true
            )
        } catch (e: Exception) {
            println(e.stackTrace)
        }

        try {
            subaccountRepository.save(
                Subaccount(
                    account = omnibusBusinessRef,
                    reference = ReferenceConventions.Wadzpay.omnibus(currencyUnit.name),
                    asset = currencyUnit.toString()
                )
            )
        } catch (e: Exception) {
            logger.info(e.message)
        }

        try {
            subaccountRepository.save(
                Subaccount(
                    account = feeCollectionBusinessRef,
                    reference = ReferenceConventions.Wadzpay.feeCollection(currencyUnit.name),
                    asset = currencyUnit.toString()
                )
            )
        } catch (e: Exception) {
            logger.info(e.message)
        }
    }

    fun createAccountsForPrivateAsset(assetName: String) {
        val logger: Logger = LoggerFactory.getLogger(javaClass)
        val unit = 0.0001.toBigDecimal()
        try {
            ledgerService.createAsset(assetName, unit)
        } catch (e: Exception) {
            logger.info(e.message)
        }
        val omnibusBusinessRef = accountRepository.getByReference(ReferenceConventions.Wadzpay.omnibus())
        val feeCollectionBusinessRef = accountRepository.getByReference(ReferenceConventions.Wadzpay.feeCollection())

        logger.info(omnibusBusinessRef.reference.toString())
        logger.info(feeCollectionBusinessRef.getOwnerName())

        try {
            ledgerService.createCommit(
                commit {
                    reference("commit.wadzpay.initial")
                    createAssetSubaccounts(assetName) { ReferenceConventions.Wadzpay.omnibus(it) }
                    createAssetSubaccounts(assetName) { ReferenceConventions.Wadzpay.feeCollection(it) }
                },
                true
            )
        } catch (e: Exception) {
            println(e.stackTrace)
        }

        try {
            subaccountRepository.save(
                Subaccount(
                    account = omnibusBusinessRef,
                    reference = ReferenceConventions.Wadzpay.omnibus(assetName),
                    asset = assetName
                )
            )
        } catch (e: Exception) {
            logger.info(e.message)
        }

        try {
            subaccountRepository.save(
                Subaccount(
                    account = feeCollectionBusinessRef,
                    reference = ReferenceConventions.Wadzpay.feeCollection(assetName),
                    asset = assetName
                )
            )
        } catch (e: Exception) {
            logger.info(e.message)
        }
    }
}
