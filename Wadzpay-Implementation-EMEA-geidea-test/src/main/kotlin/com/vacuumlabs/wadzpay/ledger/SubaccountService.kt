package com.vacuumlabs.wadzpay.ledger

import com.vacuumlabs.vuba.ledger.common.DualReference
import com.vacuumlabs.wadzpay.algocutomtoken.AlgoCustomTokenCacheService
import com.vacuumlabs.wadzpay.algocutomtoken.AlgoCustomTokenWallet
import com.vacuumlabs.wadzpay.algocutomtoken.AlgoCutomeTokenService
import com.vacuumlabs.wadzpay.bitgo.BitGoWallet
import com.vacuumlabs.wadzpay.ledger.model.Account
import com.vacuumlabs.wadzpay.ledger.model.AccountType
import com.vacuumlabs.wadzpay.ledger.model.CryptoAddress
import com.vacuumlabs.wadzpay.ledger.model.CryptoAddressRepository
import com.vacuumlabs.wadzpay.ledger.model.Subaccount
import com.vacuumlabs.wadzpay.ledger.model.SubaccountRepository
import com.vacuumlabs.wadzpay.user.UserAccount
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class SubaccountService(
    val subaccountRepository: SubaccountRepository,
    val bitGoWallet: BitGoWallet,
    val algoCustomTokenWallet: AlgoCustomTokenWallet,
    val cryptoAddressRepository: CryptoAddressRepository,
    @org.springframework.context.annotation.Lazy
    @Autowired
    val algoCutomeTokenService: AlgoCutomeTokenService,
    val algoCustomTokenCacheService: AlgoCustomTokenCacheService
) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)
    fun createSubaccount(account: Account, reference: DualReference, asset: String, userAccount: UserAccount): Subaccount {
        println("creating subaccount for- $asset")
        val subAccount = subaccountRepository.save(
            Subaccount(
                account = account,
                reference = reference,
                asset = asset,
                userAccountId = userAccount
            )
        )
        if (account.type != AccountType.RESERVATION) {
            val assetIndex = algoCustomTokenCacheService.getCustomTokenIndex(subAccount.asset)
            val address: CryptoAddress? = if (assetIndex != null) {
                logger.info("Address creation for pvt blockchain :" + subAccount.asset)
                algoCutomeTokenService.createAddressWithOptin(subAccount.asset)
            } else {
                logger.info("Address creation for open blockchain :" + subAccount.asset)
                createAddress(subAccount)
            }
            subAccount.address = address
            subaccountRepository.save(subAccount)
        }
        return subAccount
    }

    fun createSubaccount(account: Account, reference: DualReference, asset: CurrencyUnit): Subaccount {
        logger.info("creating subaccount for $asset")
        val subAccount = subaccountRepository.save(
            Subaccount(
                account = account,
                reference = reference,
                asset = asset.toString()
            )
        )
        if (account.type != AccountType.RESERVATION) {
            val address = createAddress(subAccount)
            subAccount.address = address
            subaccountRepository.save(subAccount)
        }
        return subAccount
    }

    fun createAddress(subaccount: Subaccount): CryptoAddress? {
        println("createAddress-1")
        var address: CryptoAddress? = null
        if (CurrencyUnit.valueOf(subaccount.asset).onEthBlockchain()) {
            address = subaccount.account.subaccounts.find { CurrencyUnit.valueOf(it.asset).onEthBlockchain() && it.address != null }?.address
        }
        println("createAddress-2")

        if (CurrencyUnit.valueOf(subaccount.asset).onAlgoOnlyBlockchain()) {
            address =
                subaccount.account.subaccounts.find { CurrencyUnit.valueOf(it.asset).onAlgoOnlyBlockchain() && it.address != null }?.address
        }
        println("createAddress-3")

        if (CurrencyUnit.valueOf(subaccount.asset).onAlgoUSDCBlockchain()) {
            address =
                subaccount.account.subaccounts.find { CurrencyUnit.valueOf(it.asset).onAlgoUSDCBlockchain() && it.address != null }?.address
        }
        println("createAddress-4")

        if (CurrencyUnit.valueOf(subaccount.asset).onWadzpayBlockChain()) {
            address =
                subaccount.account.subaccounts.find { CurrencyUnit.valueOf(it.asset).onWadzpayBlockChain() && it.address != null }?.address
        }
        println("createAddress-5")

        return if (address == null) {
            println("createAddress-6")
            if (CurrencyUnit.valueOf(subaccount.asset) == CurrencyUnit.SART) {
                println("createAddress-7")
                // logger.info("creating algo address for subaccount...")

                /* Earlier It was used */
                /* algoCustomTokenWallet.createAddress(subaccount.asset)  */

                /* Replace with new Address creation */
                algoCutomeTokenService.createAddressWithOptin(subaccount.asset)
                // logger.info("algo address for subaccount created successfully")
            } else {
                println("createAddress-8")
                bitGoWallet.createAddress(CurrencyUnit.valueOf(subaccount.asset))
            }
        } else {
            println("createAddress-9")
            cryptoAddressRepository.save(CryptoAddress(asset = subaccount.asset, address = address.address))
        }
    }

    fun createAccountForPrivateBC(account: Account, reference: DualReference, asset: String, userAccount: UserAccount): Subaccount {
        println("creating subaccount for: $asset")
        val subAccount = subaccountRepository.save(
            Subaccount(
                account = account,
                reference = reference,
                asset = asset,
                userAccountId = userAccount
            )
        )
        if (account.type != AccountType.RESERVATION) {
            val address = algoCutomeTokenService.createAddressWithOptin(subAccount.asset)
            subAccount.address = address
            subaccountRepository.save(subAccount)
        }
        return subAccount
    }
}
