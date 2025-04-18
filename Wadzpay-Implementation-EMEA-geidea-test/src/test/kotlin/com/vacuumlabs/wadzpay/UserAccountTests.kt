package com.vacuumlabs.wadzpay

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import com.vacuumlabs.wadzpay.common.ErrorCodes.Companion.EMAIL_ALREADY_EXISTS
import com.vacuumlabs.wadzpay.common.ErrorCodes.Companion.FRAUDULENT_USER
import com.vacuumlabs.wadzpay.common.ErrorCodes.Companion.PHONE_NUMBER_ALREADY_EXISTS
import com.vacuumlabs.wadzpay.common.ErrorCodes.Companion.PHONE_NUMBER_DOES_NOT_EXISTS
import com.vacuumlabs.wadzpay.common.ErrorCodes.Companion.UNVERIFIED_PHONE_NUMBER
import com.vacuumlabs.wadzpay.common.ErrorResponse
import com.vacuumlabs.wadzpay.common.UnprocessableEntityException
import com.vacuumlabs.wadzpay.ledger.LedgerService
import com.vacuumlabs.wadzpay.services.CognitoService
import com.vacuumlabs.wadzpay.services.RedisService
import com.vacuumlabs.wadzpay.services.SeonService
import com.vacuumlabs.wadzpay.services.SeonStatus
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
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import javax.validation.ConstraintViolation
import javax.validation.Validation
import javax.validation.ValidatorFactory

@AutoConfigureMockMvc
class UserAccountTests @Autowired constructor(val mockMvc: MockMvc, var mapper: ObjectMapper) : IntegrationTests() {
    @MockkBean
    lateinit var twilioService: TwilioService

    @MockkBean
    lateinit var seonService: SeonService

    @MockkBean
    lateinit var cognitoService: CognitoService

    @SpykBean
    lateinit var userAccountService: UserAccountService

    @SpykBean
    lateinit var ledgerService: LedgerService

    @SpykBean
    lateinit var redisService: RedisService

    @BeforeEach
    fun clear() {
        /*Required, because otherwise tests are not isolated*/
        clearAllMocks()
        databaseCleanupService.deleteData()
        databaseCleanupService.createInitData()
    }

    // @Test
    fun `Can send OTP code to a phone number`() {
        every { cognitoService.isPhoneAvailable(PHONE_NUMBER) } returns true
        every { twilioService.sendPhoneOTPCode(PHONE_NUMBER) } returns VERIFICATION_SID

        mockMvc.post(mapper, REGISTER_PHONE_URL, CreatePhoneRequest(PHONE_NUMBER))
    }

 /*   //@Test
    fun `Can't send OTP code to an invalid phone number that is not caught by regexp validation`() {
        val invalidPhoneNumber = "+9112312312"
        every { twilioService.sendPhoneOTPCode(invalidPhoneNumber) } throws ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_PHONE_NUMBER")
        every { cognitoService.isPhoneAvailable(invalidPhoneNumber) } returns true

        val response = mockMvc.post(mapper, REGISTER_PHONE_URL, CreatePhoneRequest(invalidPhoneNumber)) { status { isBadRequest() } }
        response.errorMessage shouldBe INVALID_PHONE_NUMBER
        verify(exactly = 0) { redisService.savePhone(any()) }
    }*/

    // @Test
    fun `Can't register duplicate phone number`() {
        every { cognitoService.isPhoneAvailable(PHONE_NUMBER) } returns false

        val response = mockMvc.post(mapper, REGISTER_PHONE_URL, CreatePhoneRequest(PHONE_NUMBER)) { status { isConflict() } }.response<ErrorResponse>(mapper)
        response.message shouldBe PHONE_NUMBER_ALREADY_EXISTS
        response.status shouldBe HttpStatus.CONFLICT.value()

        verify(exactly = 0) { redisService.savePhone(any()) }
        verify(exactly = 0) { twilioService.sendPhoneOTPCode(any()) }
    }

    // @Test
    fun `Can verify correct OTP code`() {
        every { cognitoService.isPhoneAvailable(PHONE_NUMBER) } returns true
        every { redisService.getPhoneVerified(PHONE_NUMBER) } returns RedisService.REQUESTED
        justRun { twilioService.verifyOTPCode(PHONE_NUMBER, CORRECT_CODE) }

        mockMvc.post(mapper, VERIFY_PHONE_URL, VerifyPhoneRequest(PHONE_NUMBER, CORRECT_CODE))
        // verify { twilioService.verifyOTPCode(any(), any()) }
    }

    /*//@Test
    fun `Can't verify incorrect OTP code`() {
        every { cognitoService.isPhoneAvailable(PHONE_NUMBER) } returns true
        every { redisService.getPhoneVerified(PHONE_NUMBER) } returns RedisService.REQUESTED
        every { twilioService.verifyOTPCode(PHONE_NUMBER, CORRECT_CODE) } throws ForbiddenException(INCORRECT_CODE)

        val response = mockMvc.post(mapper, VERIFY_PHONE_URL, VerifyPhoneRequest(PHONE_NUMBER, CORRECT_CODE)) { status { isForbidden() } }.response<ErrorResponse>(mapper)
        response.message shouldBe INCORRECT_CODE
        response.status shouldBe HttpStatus.FORBIDDEN.value()

        verify { twilioService.verifyOTPCode(any(), any()) }
    }*/

    // @Test
    fun `Can't verify an OTP code without sending it first`() {
        every { cognitoService.isPhoneAvailable(PHONE_NUMBER) } returns true
        every { redisService.getPhoneVerified(PHONE_NUMBER) } returns null

        val response = mockMvc.post(mapper, VERIFY_PHONE_URL, VerifyPhoneRequest(PHONE_NUMBER, CORRECT_CODE)) { status { isNotFound() } }.response<ErrorResponse>(mapper)
        response.message shouldBe PHONE_NUMBER_DOES_NOT_EXISTS
        response.status shouldBe HttpStatus.NOT_FOUND.value()
        verify(exactly = 0) { twilioService.sendPhoneOTPCode(any()) }
    }

    // @Test
    fun `Can't verify phone number that is already taken`() {
        every { cognitoService.isPhoneAvailable(PHONE_NUMBER) } returns true
        every { redisService.getPhoneVerified(PHONE_NUMBER) } returns RedisService.REQUESTED
        every { cognitoService.isPhoneAvailable(PHONE_NUMBER) } returns false

        val response = mockMvc.post(mapper, VERIFY_PHONE_URL, VerifyPhoneRequest(PHONE_NUMBER, CORRECT_CODE)) { status { isConflict() } }.response<ErrorResponse>(mapper)
        response.message shouldBe PHONE_NUMBER_ALREADY_EXISTS
        response.status shouldBe HttpStatus.CONFLICT.value()
        verify(exactly = 0) { twilioService.sendPhoneOTPCode(any()) }
    }

    // @TestFactory
    fun `Test different cases of password input`() =
        listOf(
            "password123!#$" to true,
            "Password1!" to true,
            "Password1\"" to true,
            "Password1#" to true,
            "Password1\$" to true,
            "Password1%" to true,
            "Password1&" to true,
            "Password1'" to true,
            "Password1(" to true,
            "Password1)" to true,
            "Password1*" to true,
            "Password1+" to true,
            "Password1." to true,
            "Password1," to true,
            "Password1-" to true,
            "Password1/" to true,
            "Password1;" to true,
            "Password1:" to true,
            "Password1=" to true,
            "Password1<" to true,
            "Password1>" to true,
            "Password1?" to true,
            "Password1@" to true,
            "Password1[" to true,
            "Password1]" to true,
            "Password1\\" to true,
            "Password1^" to true,
            "Password1_" to true,
            "Password1`" to true,
            "Password1{" to true,
            "Password1}" to true,
            "Password1~" to true,
            "Password1|" to true,
            "abcdefghijklmnopqrstuvwxyz0123456789!@#\$%^&*()-_=+`~\\|[]{};:'\",./<>/" to true,
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#\$%^&*()-_=+`~\\|[]{};:'\",./<>/" to true,
            "abcdefghijklmnopqrstuvwxyz!@#\$%^&*()-_=+" to false,
            "abcdefghijklmnopqrstuvwxyz0123456789" to false,
            "abcdefghijklmnopqrstuvwxyz" to false,
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ" to false,
            "0123456789" to false,
            "!@#\$%^&*()" to false,
            "a1!@#" to false,
            "ABCabc123" to false,
            "abc123!@#abc123!@#abc123!@#abc123!@#abc123!@#abc123!@#abc123!@#abc123!@#abc123!@#abc123!@#abc123!@#abc123!@#abc123!@#abc123!@#abc123!@#abc123!@#" to false,
            "" to false
        ).map { (testPassword, expectedResult) ->
            dynamicTest(
                "given \"$testPassword\", " +
                    "when validating the password, " +
                    "then it should be reported as ${if (expectedResult) "valid" else "invalid"}"
            ) {
                val userTestRequest = PreRegisterRequest(
                    "validEmail@valid.com",
                    "+99999999",
                    testPassword,
                )
                val factory: ValidatorFactory = Validation.buildDefaultValidatorFactory()
                val validator = factory.validator
                val violations: Set<ConstraintViolation<PreRegisterRequest>> = validator.validate(userTestRequest)
                assertThat(violations.isEmpty()).isEqualTo(expectedResult)
            }
        }

    // @TestFactory
    fun `Test different cases of phoneNumber input`() =
        listOf(
            "+1231231231" to true,
            "+84123123123" to true,
            "12312.31231" to false,
            "123123.1231" to false,
            "123.1231231" to false,
            "11123aas12" to false,
            "123123" to false,
            "+1231231231231231" to false,
            "" to false,
        ).map { (testPhoneNumber, expectedResult) ->
            dynamicTest(
                "given \"$testPhoneNumber\", " +
                    "when validating the phone number, " +
                    "then it should be reported as ${if (expectedResult) "valid" else "invalid"}"
            ) {
                val userTestRequest = PreRegisterRequest(
                    "validEmail@valid.com",
                    testPhoneNumber,
                    "password123!",
                )
                val factory: ValidatorFactory = Validation.buildDefaultValidatorFactory()
                val validator = factory.validator
                val violations: Set<ConstraintViolation<PreRegisterRequest>> = validator.validate(userTestRequest)
                assertThat(violations.isEmpty()).isEqualTo(expectedResult)
            }
        }

    // @TestFactory
    fun `Can submit user details`() =
        listOf(SeonStatus.values().toList().minus(SeonStatus.DECLINE)).map { (seonStatus) ->
            dynamicTest("Can submit user details with good fraud detection score $seonStatus") {
                justRun { seonService.checkStatus(PHONE_NUMBER, EMAIL, IP_ADDRESS) }
                every { redisService.getPhoneVerified(PHONE_NUMBER) } returns RedisService.VERIFIED
                every { twilioService.sendEmailOTPCode(EMAIL) } returns VERIFICATION_SID
                every { cognitoService.isPhoneAvailable(PHONE_NUMBER) } returns true
                every { cognitoService.isEmailAvailable(EMAIL) } returns true

                mockMvc.post(mapper, DETAILS_URL, PreRegisterRequest(EMAIL, PHONE_NUMBER, PASSWORD))
                verify { redisService.saveEmailForPhone(EMAIL, PHONE_NUMBER) }
            }
        }

    // @TestFactory
    fun `Can't submit user details if fraud detection fails`() {
        every { seonService.checkStatus(PHONE_NUMBER, EMAIL, IP_ADDRESS) } throws UnprocessableEntityException(FRAUDULENT_USER)
        every { redisService.getPhoneVerified(PHONE_NUMBER) } returns RedisService.VERIFIED
        every { twilioService.sendEmailOTPCode(EMAIL) } returns VERIFICATION_SID

        val response = mockMvc.post(
            mapper, DETAILS_URL,
            PreRegisterRequest(
                EMAIL,
                PHONE_NUMBER, PASSWORD
            )
        ) { status { isBadRequest() } }
        response.errorMessage shouldBe FRAUDULENT_USER
        verify { redisService.saveEmailForPhone(EMAIL, PHONE_NUMBER) }
        verify { twilioService.sendEmailOTPCode(any()) }

        redisService.getEmailVerifiedForPhone(EMAIL, PHONE_NUMBER) shouldBe RedisService.REQUESTED
    }

    // @Test
    fun `Can't submit user details if phone number isn't verified`() {
        every { redisService.getPhoneVerified(PHONE_NUMBER) } returns RedisService.REQUESTED

        val response = mockMvc.post(
            mapper, DETAILS_URL,
            PreRegisterRequest(
                EMAIL,
                PHONE_NUMBER, PASSWORD
            )
        ) { status { isBadRequest() } }.response<ErrorResponse>(mapper)
        response.message shouldBe UNVERIFIED_PHONE_NUMBER
        response.status shouldBe HttpStatus.BAD_REQUEST.value()

        verify(exactly = 0) { redisService.saveEmailForPhone(EMAIL, PHONE_NUMBER) }
    }

    // @Test
    fun `Can't submit details for two users with the same email`() {
        every { redisService.getPhoneVerified(PHONE_NUMBER) } returns RedisService.VERIFIED
        every { cognitoService.isEmailAvailable(EMAIL) } returns false

        val response = mockMvc.post(
            mapper, DETAILS_URL,
            PreRegisterRequest(
                EMAIL,
                PHONE_NUMBER, PASSWORD
            )
        ) { status { isConflict() } }.response<ErrorResponse>(mapper)
        response.message shouldBe EMAIL_ALREADY_EXISTS
        response.status shouldBe HttpStatus.CONFLICT.value()

        verify(exactly = 0) { redisService.saveEmailForPhone(EMAIL, PHONE_NUMBER) }
    }

    // @Test
    fun `Can register new user`() {
        every { redisService.getPhoneVerified(PHONE_NUMBER) } returns RedisService.VERIFIED
        every { redisService.getEmailVerifiedForPhone(EMAIL, PHONE_NUMBER) } returns RedisService.REQUESTED
        every { cognitoService.isEmailAvailable(EMAIL) } returns true
        justRun { twilioService.verifyOTPCode(EMAIL, CORRECT_CODE) }

        every { cognitoService.register(EMAIL, PHONE_NUMBER, PASSWORD, false) } returns EMAIL

        mockMvc.post(
            mapper, VERIFY_AND_CREATE_URL,
            VerifyAndCreateRequest(
                EMAIL,
                PHONE_NUMBER, CORRECT_CODE, PASSWORD,
                false,
                null
            )
        )

        verify { redisService.deleteRegistrationEntity(EMAIL, PHONE_NUMBER) }
        verify { userAccountService.createUserAccount(EMAIL, EMAIL, PHONE_NUMBER) }
        verify { cognitoService.register(EMAIL, PHONE_NUMBER, PASSWORD, false) }
        verify { ledgerService.createAccounts(any()) }
    }

 /*   //@Test
    fun `Can't register user with incorect email verification code`() {
        every { redisService.getPhoneVerified(PHONE_NUMBER) } returns RedisService.VERIFIED
        every { redisService.getEmailVerifiedForPhone(EMAIL, PHONE_NUMBER) } returns RedisService.REQUESTED
        every { cognitoService.isEmailAvailable(EMAIL) } returns true
        every { twilioService.verifyOTPCode(EMAIL, CORRECT_CODE) } throws ForbiddenException(INCORRECT_CODE)

        val response = mockMvc.post(
            mapper, VERIFY_AND_CREATE_URL,
            VerifyAndCreateRequest(
                EMAIL,
                PHONE_NUMBER, CORRECT_CODE, PASSWORD
            )
        ) { status { isForbidden() } }.response<ErrorResponse>(mapper)
        response.message shouldBe INCORRECT_CODE
        response.status shouldBe HttpStatus.FORBIDDEN.value()

        verify(exactly = 0) { redisService.deleteRegistrationEntity(EMAIL, PHONE_NUMBER) }
        verify(exactly = 0) {
            userAccountService.createUserAccount(
                COGNITO_USERNAME, EMAIL,
                PHONE_NUMBER
            )
        }
        verify(exactly = 0) { cognitoService.register(EMAIL, PHONE_NUMBER, PASSWORD) }
        verify(exactly = 0) { ledgerService.createAccounts(any()) }
    }*/
}
