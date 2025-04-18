package com.vacuumlabs.wadzpay

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.vacuumlabs.wadzpay.ContactTests.Companion.EMAIL_2
import com.vacuumlabs.wadzpay.auth.Role
import com.vacuumlabs.wadzpay.common.EntityNotFoundException
import com.vacuumlabs.wadzpay.merchant.Invitation
import com.vacuumlabs.wadzpay.merchant.InviteUserRequest
import com.vacuumlabs.wadzpay.merchant.model.Merchant
import com.vacuumlabs.wadzpay.services.CognitoService
import com.vacuumlabs.wadzpay.services.SeonService
import com.vacuumlabs.wadzpay.services.TwilioService
import com.vacuumlabs.wadzpay.user.CreatePhoneRequest
import com.vacuumlabs.wadzpay.user.PreRegisterRequest
import com.vacuumlabs.wadzpay.user.UserAccountService
import com.vacuumlabs.wadzpay.user.VerifyAndCreateRequest
import com.vacuumlabs.wadzpay.user.VerifyPhoneRequest
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.verify
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc

@AutoConfigureMockMvc
// @TestMethodOrder(OrderAnnotation::class)
class MerchantDashboardRegistrationFlowTests @Autowired constructor(val mockMvc: MockMvc, val mapper: ObjectMapper, val userAccountService: UserAccountService) : IntegrationTests() {
    @MockkBean
    lateinit var twilioService: TwilioService

    @MockkBean
    lateinit var seonService: SeonService

    @MockkBean
    lateinit var cognitoService: CognitoService

    companion object {
        const val CREATE_MERCHANT_FROM_DASHBOARD_URL = "/merchantDashboard/admin/merchant"
        const val INVITE_USER_URL = "/merchantDashboard/admin/invite"
        const val CHECK_INVITATION_URL = "/merchantDashboard/invite"
        val INVITE_USER_REQUEST = InviteUserRequest(EMAIL_2, Role.MERCHANT_READER)
    }

    @BeforeAll
    fun clear() {
        clearAllMocks()
        databaseCleanupService.deleteData()
        databaseCleanupService.createInitData()
    }

    // @Test
    @Order(1)
    fun `Step 1 - can send OTP code to a phone number`() {
        every { twilioService.sendPhoneOTPCode(PHONE_NUMBER) } returns VERIFICATION_SID
        every { cognitoService.isPhoneAvailable(PHONE_NUMBER) } returns true

        mockMvc.post(mapper, REGISTER_PHONE_URL, CreatePhoneRequest(PHONE_NUMBER))
    }

    // @Test
    @Order(2)
    fun `Step 2 - can verify received phone number OTP code`() {
        justRun { twilioService.verifyOTPCode(PHONE_NUMBER, CORRECT_CODE) }
        every { cognitoService.isPhoneAvailable(PHONE_NUMBER) } returns true

        mockMvc.post(mapper, VERIFY_PHONE_URL, VerifyPhoneRequest(PHONE_NUMBER, CORRECT_CODE))
    }

    // @Test
    @Order(3)
    fun `Step 3 - can send registration details and send OTP code to an email`() {
        justRun { seonService.checkStatus(PHONE_NUMBER, EMAIL, IP_ADDRESS) }
        every { twilioService.sendEmailOTPCode(EMAIL) } returns VERIFICATION_SID
        every { cognitoService.isPhoneAvailable(PHONE_NUMBER) } returns true
        every { cognitoService.isEmailAvailable(EMAIL) } returns true

        mockMvc.post(mapper, DETAILS_URL, PreRegisterRequest(EMAIL, PHONE_NUMBER, PASSWORD))
    }

    // @Test
    @Order(4)
    fun `Step 4 - can verify received email OTP code and finish registration`() {
        justRun { twilioService.verifyOTPCode(EMAIL, CORRECT_CODE) }
        justRun { cognitoService.addToGroup(any(), any()) }
        every { cognitoService.register(EMAIL, PHONE_NUMBER, PASSWORD, false) } returns EMAIL
        every { cognitoService.isEmailAvailable(EMAIL) } returns true

        assertThrows<EntityNotFoundException> {
            userAccountService.getUserAccountByEmail(EMAIL)
        }

        mockMvc.post(mapper, VERIFY_AND_CREATE_URL, VerifyAndCreateRequest(EMAIL, PHONE_NUMBER, CORRECT_CODE, PASSWORD, true, null))

        val userAccount = userAccountService.getUserAccountByEmail(EMAIL)
        userAccount.email shouldBe EMAIL
        userAccount.phoneNumber shouldBe PHONE_NUMBER

        verify { cognitoService.addToGroup(userAccount, Role.MERCHANT_ADMIN) }
    }

    // @Test
    @Order(5)
    @WithMockUser(username = EMAIL, roles = ["MERCHANT_ADMIN"])
    fun `Step 5 - can register a merchant`() {
        mockMvc.post(mapper, CREATE_MERCHANT_FROM_DASHBOARD_URL, CREATE_MERCHANT_REQUEST) { status { isCreated() } }.response<Merchant>(mapper)
    }

    // @Test
    @Order(6)
    @WithMockUser(username = EMAIL, roles = ["MERCHANT_ADMIN"])
    fun `Step 6 - can invite a user`() {
        mockMvc.post(mapper, INVITE_USER_URL, INVITE_USER_REQUEST) { status { isCreated() } }
    }

    // @Test
    @Order(7)
    @WithMockUser(username = EMAIL, roles = ["MERCHANT_ADMIN"])
    fun `Step 7 - can verify invitation is preset`() {
        val invitation = Invitation(MERCHANT_NAME, Role.MERCHANT_READER)
        mockMvc.get(mapper, "$CHECK_INVITATION_URL?email=$EMAIL_2", invitation)
    }
}
