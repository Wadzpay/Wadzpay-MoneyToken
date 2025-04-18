package com.vacuumlabs.wadzpay

import com.fasterxml.jackson.databind.ObjectMapper
import com.vacuumlabs.wadzpay.user.AddOrUpdateContactRequest
import com.vacuumlabs.wadzpay.user.ContactViewModel
import com.vacuumlabs.wadzpay.user.UserAccount
import com.vacuumlabs.wadzpay.user.UserAccountService
import io.mockk.clearAllMocks
import org.junit.Ignore
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc

@AutoConfigureMockMvc
class ContactTests @Autowired constructor(
    val mockMvc: MockMvc,
    val mapper: ObjectMapper,
    val userAccountService: UserAccountService
) : IntegrationTests() {

    companion object {
        const val ADD_CONTACT_URL = "/user/contact"
        const val UPDATE_CONTACT_URL = "/user/contact"
        const val GET_CONTACTS_URL = "/user/contacts"
        const val GET_USER_URL = "/user"
        const val NICKNAME = "nickname"
        const val NICKNAME_2 = "nickname_2"

        // TODO: this is repeatable data, as it is already present in many other test case classes
        // TODO: I will merge WADZ-245 soon which has this refactored soon, and remove this repeatable data
        const val EMAIL = "email@domain.com"
        const val PHONE_NUMBER = "+421944112233"
        const val COGNITO_USERNAME = "12345"
        const val EMAIL_2 = "email2@domain.com"
        const val PHONE_NUMBER_2 = "+421944112239"
        const val COGNITO_USERNAME_2 = "123456"
        const val EMAIL_3 = "email3@domain.com"
        const val PHONE_NUMBER_3 = "+421944112235"
        const val COGNITO_USERNAME_3 = "1234567"
        val CONTACT_VIEW_MODEL = ContactViewModel(NICKNAME, PHONE_NUMBER_2, EMAIL_2, COGNITO_USERNAME_2)
        val CONTACT_VIEW_MODEL_2 = ContactViewModel(NICKNAME_2, PHONE_NUMBER_3, EMAIL_3, COGNITO_USERNAME_3)
    }

    @BeforeEach
    fun clear() {
        clearAllMocks()
        databaseCleanupService.deleteData()
        databaseCleanupService.createInitData()

        userAccountService.createUserAccount(COGNITO_USERNAME, EMAIL, PHONE_NUMBER)
        userAccountService.createUserAccount(COGNITO_USERNAME_2, EMAIL_2, PHONE_NUMBER_2)
        userAccountService.createUserAccount(COGNITO_USERNAME_3, EMAIL_3, PHONE_NUMBER_3)
    }

    // @Test
    @WithMockUser(username = EMAIL)
    fun `Can add contact`() {
        mockMvc.post(mapper, ADD_CONTACT_URL, AddOrUpdateContactRequest(NICKNAME, COGNITO_USERNAME_2))

        mockMvc.get(mapper, GET_CONTACTS_URL, listOf(CONTACT_VIEW_MODEL))
    }

    // @Test
    @WithMockUser(username = EMAIL)
    fun `Can't add same nickname two times`() {
        mockMvc.post(mapper, ADD_CONTACT_URL, AddOrUpdateContactRequest(NICKNAME, COGNITO_USERNAME_2))

        mockMvc.post(mapper, ADD_CONTACT_URL, AddOrUpdateContactRequest(NICKNAME, COGNITO_USERNAME_3)) {
            status { isConflict() }
        }
    }

    // @Test
    @WithMockUser(username = EMAIL)
    fun `Can't add non-existing user`() {
        mockMvc.post(mapper, ADD_CONTACT_URL, AddOrUpdateContactRequest(NICKNAME, "InvalidCognitoUsername")) {
            status { isNotFound() }
        }
    }

    // @Test
    @WithMockUser(username = EMAIL)
    fun `Can't add own contact`() {
        mockMvc.post(mapper, ADD_CONTACT_URL, AddOrUpdateContactRequest(NICKNAME, COGNITO_USERNAME)) { status { isUnprocessableEntity() } }
    }

    // @Test
    @WithMockUser(username = EMAIL)
    fun `Can update contact`() {
        mockMvc.post(mapper, ADD_CONTACT_URL, AddOrUpdateContactRequest(NICKNAME, COGNITO_USERNAME_2))
        mockMvc.get(mapper, GET_CONTACTS_URL, listOf(CONTACT_VIEW_MODEL))

        mockMvc.patch(mapper, UPDATE_CONTACT_URL, AddOrUpdateContactRequest(NICKNAME_2, COGNITO_USERNAME_2))
        mockMvc.get(mapper, GET_CONTACTS_URL, listOf(CONTACT_VIEW_MODEL.copy(nickname = NICKNAME_2)))
    }

    // @Test
    @WithMockUser(username = EMAIL)
    fun `Can't update contact with existing nickname`() {
        mockMvc.post(mapper, ADD_CONTACT_URL, AddOrUpdateContactRequest(NICKNAME, COGNITO_USERNAME_2))
        mockMvc.post(mapper, ADD_CONTACT_URL, AddOrUpdateContactRequest(NICKNAME_2, COGNITO_USERNAME_3))
        mockMvc.get(mapper, GET_CONTACTS_URL, listOf(CONTACT_VIEW_MODEL, CONTACT_VIEW_MODEL_2))

        mockMvc.patch(mapper, UPDATE_CONTACT_URL, AddOrUpdateContactRequest(NICKNAME_2, COGNITO_USERNAME_2)) { status { isConflict() } }
    }

    // @Test
    @WithMockUser(username = EMAIL)
    fun `Can't update non-existing contact`() {
        mockMvc.patch(mapper, UPDATE_CONTACT_URL, AddOrUpdateContactRequest(NICKNAME, COGNITO_USERNAME_2)) {
            status { isNotFound() }
        }
    }

    // @Test
    @WithMockUser(username = EMAIL)
    fun `Can get user by correct email`() {
        mockMvc.get(mapper, "$GET_USER_URL?email=$EMAIL", UserAccount(COGNITO_USERNAME, EMAIL, PHONE_NUMBER))
    }

    @Ignore
    @WithMockUser(username = EMAIL)
    fun `Can't get user by not saved email`() {
        mockMvc.get(mapper, "$GET_USER_URL?email=incorrectemail@domain.com") { isNotFound() }
    }

    // @Test
    @WithMockUser(username = EMAIL)
    fun `Can't get user by invalid email`() {
        mockMvc.get(mapper, "$GET_USER_URL?email=notanemaildomaincom") { isBadRequest() }
    }

    // @Test
    @WithMockUser(username = EMAIL)
    fun `Can get user by correct phone number`() {
        mockMvc.get(mapper, "$GET_USER_URL?phoneNumber=$PHONE_NUMBER", UserAccount(COGNITO_USERNAME, EMAIL, PHONE_NUMBER))
    }

    // @Test
    @WithMockUser(username = EMAIL)
    fun `Can't get user by not saved phone number`() {
        mockMvc.get(mapper, "$GET_USER_URL?phoneNumber=+421944112237") { isNotFound() }
    }

    // @Test
    @WithMockUser(username = EMAIL)
    fun `Can't get user by invalid phone number`() {
        mockMvc.get(mapper, "$GET_USER_URL?phoneNumber=$EMAIL") { isBadRequest() }
    }

    // @Test
    @WithMockUser(username = EMAIL)
    fun `Can't get user if no parameters`() {
        mockMvc.get(mapper, GET_USER_URL) { isBadRequest() }
    }
}
