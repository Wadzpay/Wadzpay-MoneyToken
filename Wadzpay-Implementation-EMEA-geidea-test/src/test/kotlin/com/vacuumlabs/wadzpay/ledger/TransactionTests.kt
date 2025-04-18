package com.vacuumlabs.wadzpay.ledger

import com.fasterxml.jackson.databind.ObjectMapper
import com.vacuumlabs.wadzpay.ContactTests.Companion.COGNITO_USERNAME_2
import com.vacuumlabs.wadzpay.ContactTests.Companion.EMAIL_2
import com.vacuumlabs.wadzpay.ContactTests.Companion.PHONE_NUMBER_2
import com.vacuumlabs.wadzpay.IntegrationTests
import com.vacuumlabs.wadzpay.MerchantTests.Companion.AMOUNT
import com.vacuumlabs.wadzpay.MerchantTests.Companion.ASSET
import com.vacuumlabs.wadzpay.get
import com.vacuumlabs.wadzpay.ledger.model.SubaccountRepository
import com.vacuumlabs.wadzpay.ledger.model.Transaction
import com.vacuumlabs.wadzpay.ledger.model.TransactionDirection
import com.vacuumlabs.wadzpay.ledger.model.TransactionRepository
import com.vacuumlabs.wadzpay.ledger.model.TransactionStatus
import com.vacuumlabs.wadzpay.ledger.model.TransactionType
import com.vacuumlabs.wadzpay.merchant.MerchantService
import com.vacuumlabs.wadzpay.user.UserAccountService
import com.vacuumlabs.wadzpay.viewmodels.TransactionViewModel
import org.junit.jupiter.api.BeforeAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import java.util.UUID

@AutoConfigureMockMvc
class TransactionTests @Autowired constructor(
    val mockMvc: MockMvc,
    val mapper: ObjectMapper,
    val userAccountService: UserAccountService,
    val transactionRepository: TransactionRepository,
    val merchantService: MerchantService,
    val subaccountRepository: SubaccountRepository
) : IntegrationTests() {

    companion object {
        const val MERCHANT_NAME_2 = "MERCHANT_NAME_2"
        const val COGNITO_USERNAME_3 = "1234567893"
        const val EMAIL_3 = "email3@domain.com"
        const val PHONE_NUMBER_3 = "+421944112231"
        const val COGNITO_USERNAME_4 = "1234567894"
        const val EMAIL_4 = "email4@domain.com"
        const val PHONE_NUMBER_4 = "+421944112234"
        const val COGNITO_USERNAME_5 = "1234567895"
        const val EMAIL_5 = "email5@domain.com"
        const val PHONE_NUMBER_5 = "+421944112235"
        val CURRENCY_UNITS = CurrencyUnit.values()
        lateinit var transaction: Transaction
        lateinit var transactionViewModel: TransactionViewModel
        lateinit var transactionViewModelList: List<TransactionViewModel>
        lateinit var merchantTransaction: Transaction
        lateinit var merchantTransactionViewModel: TransactionViewModel
        lateinit var merchantTransactionViewModelList: List<TransactionViewModel>
        lateinit var userMerchantTransaction: Transaction
        lateinit var userMerchantTransactionViewModel: TransactionViewModel
    }

    @BeforeAll
    fun clear() {
        databaseCleanupService.deleteData()
        databaseCleanupService.createInitData()
        val user1 = userAccountService.createUserAccount(COGNITO_USERNAME, EMAIL, PHONE_NUMBER)
        val user2 = userAccountService.createUserAccount(COGNITO_USERNAME_2, EMAIL_2, PHONE_NUMBER_2)
        val merchant = merchantService.createMerchant(CREATE_MERCHANT_REQUEST.copy(name = MERCHANT_NAME))

        transaction = transactionRepository.save(
            Transaction(
                sender = user1.account.getSubaccountByAsset(ASSET),
                receiver = user2.account.getSubaccountByAsset(ASSET),
                amount = AMOUNT,
                asset = ASSET.toString(),
                type = TransactionType.OTHER,
                status = TransactionStatus.SUCCESSFUL
            )
        )
        transactionViewModel = TransactionViewModel(
            transaction.uuid,
            null,
            null,
            transaction.createdAt,
            transaction.amount,
            transaction.asset,
            transaction.fiatAmount,
            transaction.fiatAsset,
            transaction.status,
            transaction.type,
            transaction.sender.account.getOwnerName(),
            transaction.receiver.account.getOwnerName(),
            TransactionDirection.OUTGOING,
            transaction.description,
            transaction.amount
        )

        merchantTransaction = transactionRepository.save(
            Transaction(
                // getSubaccountByAsset would throw LazyInitializationException
                sender = subaccountRepository.findByAccountAndAsset(merchant.account, ASSET)!!,
                receiver = user2.account.getSubaccountByAsset(ASSET),
                amount = AMOUNT,
                asset = ASSET.toString(),
                type = TransactionType.MERCHANT,
                status = TransactionStatus.SUCCESSFUL
            )
        )
        merchantTransactionViewModel = TransactionViewModel(
            merchantTransaction.uuid,
            null,
            null,
            merchantTransaction.createdAt,
            merchantTransaction.amount,
            merchantTransaction.asset,
            merchantTransaction.fiatAmount,
            merchantTransaction.fiatAsset,
            merchantTransaction.status,
            merchantTransaction.type,
            merchantTransaction.sender.account.getOwnerName(),
            merchantTransaction.receiver.account.getOwnerName(),
            TransactionDirection.OUTGOING,
            merchantTransaction.description,
            merchantTransaction.amount
        )

        userMerchantTransaction = transactionRepository.save(
            Transaction(
                sender = user1.account.getSubaccountByAsset(ASSET),
                receiver = subaccountRepository.findByAccountAndAsset(merchant.account, ASSET)!!,
                amount = AMOUNT,
                asset = ASSET.toString(),
                type = TransactionType.MERCHANT,
                status = TransactionStatus.SUCCESSFUL
            )
        )
        userMerchantTransactionViewModel = TransactionViewModel(
            userMerchantTransaction.uuid,
            null,
            null,
            userMerchantTransaction.createdAt,
            userMerchantTransaction.amount,
            userMerchantTransaction.asset,
            userMerchantTransaction.fiatAmount,
            userMerchantTransaction.fiatAsset,
            userMerchantTransaction.status,
            userMerchantTransaction.type,
            userMerchantTransaction.sender.account.getOwnerName(),
            userMerchantTransaction.receiver.account.getOwnerName(),
            TransactionDirection.OUTGOING,
            userMerchantTransaction.description,
            userMerchantTransaction.amount,
            trxId = userMerchantTransaction.tx_id
        )

        transactionViewModelList = listOf(userMerchantTransactionViewModel, transactionViewModel)
        merchantTransactionViewModelList = listOf(userMerchantTransactionViewModel.copy(direction = TransactionDirection.INCOMING), merchantTransactionViewModel)
    }

    // @Test
    fun `Unauthorized requests`() {
        mockMvc.get(mapper, USER_BALANCES_URL) { isForbidden() }
        mockMvc.get(mapper, USER_TRANSACTIONS_URL) { isForbidden() }
        mockMvc.get(mapper, "$USER_TRANSACTIONS_URL/1") { isForbidden() }
        mockMvc.get(mapper, MERCHANT_BALANCES_URL) { isForbidden() }
        mockMvc.get(mapper, MERCHANT_TRANSACTIONS_URL) { isForbidden() }
        mockMvc.get(mapper, "$MERCHANT_TRANSACTIONS_URL/1") { isForbidden() }
    }

    // @Test
    @WithMockUser
    fun `Can't get balance and transactions if user doesn't exist`() {
        mockMvc.get(mapper, USER_BALANCES_URL) { isNotFound() }
        mockMvc.get(mapper, USER_TRANSACTIONS_URL) { isNotFound() }
    }

    // @Test
    @WithMockUser
    fun `Can't get balance and transactions if merchant doesn't exist`() {
        mockMvc.get(mapper, MERCHANT_BALANCES_URL) { isForbidden() }
        mockMvc.get(mapper, MERCHANT_TRANSACTIONS_URL) { isForbidden() }
    }

    // @Test
    @WithMockUser(username = EMAIL)
    fun `Can get list of balances when user account has an account`() {
        mockMvc.get(mapper, USER_BALANCES_URL, CURRENCY_UNITS.associateWith { 0 })
    }

    // @Test
    @WithMockUser(username = MERCHANT_NAME, roles = ["MERCHANT"])
    fun `Can get list of balances when merchant has an account`() {
        mockMvc.get(mapper, MERCHANT_BALANCES_URL, CURRENCY_UNITS.associateWith { 0 })
    }

    // @Test
    @WithMockUser(username = EMAIL)
    fun `Can't get non-existing transaction by id as user`() {
        mockMvc.get(mapper, "$USER_TRANSACTIONS_URL/${UUID.randomUUID()}") { isNotFound() }
    }

    // @Test
    @WithMockUser(username = MERCHANT_NAME, roles = ["MERCHANT"])
    fun `Can't get non-existing transaction by id as merchant`() {
        mockMvc.get(mapper, "$MERCHANT_TRANSACTIONS_URL/${UUID.randomUUID()}") { isNotFound() }
    }

    // @Test
    @WithMockUser(username = EMAIL)
    fun `Can't get transaction belonging to other user`() {
        val user3 = userAccountService.createUserAccount(COGNITO_USERNAME_3, EMAIL_3, PHONE_NUMBER_3)
        val user4 = userAccountService.createUserAccount(COGNITO_USERNAME_4, EMAIL_4, PHONE_NUMBER_4)
        val transaction = transactionRepository.save(
            Transaction(
                sender = user4.account.getSubaccountByAsset(ASSET),
                receiver = user3.account.getSubaccountByAsset(ASSET),
                amount = AMOUNT,
                asset = ASSET.toString(),
                type = TransactionType.OTHER,
                status = TransactionStatus.SUCCESSFUL
            )
        )
        mockMvc.get(mapper, "$USER_TRANSACTIONS_URL/${transaction.uuid}") { isNotFound() }
    }

    // @Test
    @WithMockUser(username = MERCHANT_NAME, roles = ["MERCHANT"])
    fun `Can't get transaction belonging to other merchant`() {
        val user5 = userAccountService.createUserAccount(COGNITO_USERNAME_5, EMAIL_5, PHONE_NUMBER_5)
        val merchant = merchantService.createMerchant(CREATE_MERCHANT_REQUEST.copy(name = MERCHANT_NAME_2))
        val transaction = transactionRepository.save(
            Transaction(
                sender = subaccountRepository.findByAccountAndAsset(merchant.account, ASSET)!!,
                receiver = user5.account.getSubaccountByAsset(ASSET),
                amount = AMOUNT,
                asset = ASSET.toString(),
                type = TransactionType.MERCHANT,
                status = TransactionStatus.SUCCESSFUL
            )
        )
        mockMvc.get(mapper, "$MERCHANT_TRANSACTIONS_URL/${transaction.uuid}") { isNotFound() }
    }

   /* //@Test
    @WithMockUser(username = EMAIL)
    fun `Can get transaction by id as user`() {
        mockMvc.get(
            mapper, "$USER_TRANSACTIONS_URL/${transaction.uuid}",
            transactionViewModel
        )
    }*/

   /* //@Test
    @WithMockUser(username = MERCHANT_NAME, roles = ["MERCHANT"])
    fun `Can get transaction by id as merchant`() {
        mockMvc.get(
            mapper, "$MERCHANT_TRANSACTIONS_URL/${merchantTransaction.uuid}",
            merchantTransactionViewModel
        )
    }*/

   /* //@Test
    @WithMockUser(username = EMAIL)
    fun `Can filter transactions by cognitoUsername as user`() {
        mockMvc.get(mapper, "$USER_TRANSACTIONS_URL?cognitoUsername=invalidId") { isNotFound() }

        mockMvc.get(mapper, "$USER_TRANSACTIONS_URL?cognitoUsername=$COGNITO_USERNAME_2", listOf(transactionViewModel))
    }*/

   /* //@Test
    @WithMockUser(username = EMAIL)
    fun `Can filter transactions by direction as user`() {
        mockMvc.get(mapper, "$USER_TRANSACTIONS_URL?direction=INCOMING", listOf<TransactionViewModel>())

        mockMvc.get(mapper, "$USER_TRANSACTIONS_URL?direction=OUTGOING", transactionViewModelList)
    }*/

    /*//@Test
    @WithMockUser(username = MERCHANT_NAME, roles = ["MERCHANT"])
    fun `Can filter transactions by direction as merchant`() {
        mockMvc.get(mapper, "$MERCHANT_TRANSACTIONS_URL?direction=INCOMING", listOf(userMerchantTransactionViewModel.copy(direction = TransactionDirection.INCOMING)))

        mockMvc.get(mapper, "$MERCHANT_TRANSACTIONS_URL?direction=OUTGOING", listOf(merchantTransactionViewModel))
    }*/

   /* //@Test
    @WithMockUser(username = EMAIL)
    fun `Can filter transactions by asset as user`() {
        mockMvc.get(mapper, "$USER_TRANSACTIONS_URL?asset=ETH", listOf<TransactionViewModel>())

        mockMvc.get(mapper, "$USER_TRANSACTIONS_URL?asset=BTC", transactionViewModelList)
    }*/

  /*  //@Test
    @WithMockUser(username = MERCHANT_NAME, roles = ["MERCHANT"])
    fun `Can filter transactions by asset as merchant`() {
        mockMvc.get(mapper, "$MERCHANT_TRANSACTIONS_URL?asset=ETH", listOf<TransactionViewModel>())

        mockMvc.get(mapper, "$MERCHANT_TRANSACTIONS_URL?asset=BTC", merchantTransactionViewModelList)
    }*/

   /* //@Test
    @WithMockUser(username = EMAIL)
    fun `Can filter transactions by search term as user`() {
        mockMvc.get(mapper, "$USER_TRANSACTIONS_URL?search=invalidTerm", listOf<TransactionViewModel>())

        mockMvc.get(mapper, "$USER_TRANSACTIONS_URL?search=$MERCHANT_NAME", listOf(userMerchantTransactionViewModel))

        mockMvc.get(mapper, "$USER_TRANSACTIONS_URL?search=$EMAIL_2", listOf(transactionViewModel))
    }*/

    /*//@Test
    @WithMockUser(username = MERCHANT_NAME, roles = ["MERCHANT"])
    fun `Can filter transactions by search term as merchant`() {
        mockMvc.get(mapper, "$MERCHANT_TRANSACTIONS_URL?search=invalidTerm", listOf<TransactionViewModel>())

        mockMvc.get(mapper, "$MERCHANT_TRANSACTIONS_URL?search=$EMAIL", listOf(userMerchantTransactionViewModel.copy(direction = TransactionDirection.INCOMING)))

        mockMvc.get(mapper, "$MERCHANT_TRANSACTIONS_URL?search=$MERCHANT_NAME", merchantTransactionViewModelList)
    }*/

  /*  //@Test
    @WithMockUser(username = EMAIL)
    fun `Can paginate transactions as user`() {
        mockMvc.get(mapper, "$USER_TRANSACTIONS_URL?page=1", listOf<TransactionViewModel>())

        mockMvc.get(mapper, "$USER_TRANSACTIONS_URL?page=-1") { isBadRequest() }

        mockMvc.get(mapper, "$USER_TRANSACTIONS_URL?page=0", transactionViewModelList)
    }*/

   /* //@Test
    @WithMockUser(username = MERCHANT_NAME, roles = ["MERCHANT"])
    fun `Can paginate transactions as merchant`() {
        mockMvc.get(mapper, "$MERCHANT_TRANSACTIONS_URL?page=1", listOf<TransactionViewModel>())

        mockMvc.get(mapper, "$MERCHANT_TRANSACTIONS_URL?page=-1") { isBadRequest() }

        mockMvc.get(mapper, "$MERCHANT_TRANSACTIONS_URL?page=0", merchantTransactionViewModelList)
    }*/
}
