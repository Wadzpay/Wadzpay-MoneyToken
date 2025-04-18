package com.vacuumlabs.wadzpay.services

import com.vacuumlabs.DUMMY_PHONE_NUMBER
import com.vacuumlabs.wadzpay.auth.Role
import com.vacuumlabs.wadzpay.common.EntityNotFoundException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.configuration.AppConfig
import com.vacuumlabs.wadzpay.configuration.JwtConfiguration
import com.vacuumlabs.wadzpay.issuance.models.IssuanceBanks
import com.vacuumlabs.wadzpay.user.UserAccount
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminAddUserToGroupRequest
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserRequest
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminDeleteUserRequest
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminDisableUserRequest
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminEnableUserRequest
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthRequest
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminSetUserPasswordRequest
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminUpdateUserAttributesRequest
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersInGroupRequest
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersRequest
import software.amazon.awssdk.services.cognitoidentityprovider.model.NotAuthorizedException
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserType

@Service
class CognitoService(
    val jwtConfiguration: JwtConfiguration,
    val cognitoClient: CognitoIdentityProviderClient,
    val appConfig: AppConfig
) {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun register(email: String, phone: String?, password: String, isEmailVerified: Boolean): String {
        var phone = phone
        if (phone.isNullOrEmpty()) {
            phone = DUMMY_PHONE_NUMBER
        }
        val userAttrs = AttributeType.builder()
            .name("email")
            .value(email)
            .name("phone_number")
            .value(phone)
            .build()

        val userRequest = AdminCreateUserRequest.builder()
            .userPoolId(jwtConfiguration.userPoolId)
            .userAttributes(userAttrs)
            .username(email)
            .messageAction("SUPPRESS")
            .build()

        val response = cognitoClient.adminCreateUser(userRequest)

        logger.info("User ${response.user().username()} is created. Status: ${response.user().userStatus()}")

        val finalizeUserRequest = AdminSetUserPasswordRequest.builder()
            .userPoolId(jwtConfiguration.userPoolId)
            .username(email)
            .password(password)
            .permanent(true)
            .build()

        cognitoClient.adminSetUserPassword(finalizeUserRequest)

        val emailVerificationAttributes = AttributeType.builder().name("email_verified").value(isEmailVerified.toString()).build()
        val verifyUserEmailRequest = AdminUpdateUserAttributesRequest
            .builder()
            .userPoolId(jwtConfiguration.userPoolId)
            .username(email)
            .userAttributes(emailVerificationAttributes)
            .build()
        cognitoClient.adminUpdateUserAttributes(verifyUserEmailRequest)

        return response.user().username()
    }

    fun disableUser(user: UserAccount) {
        val disableUserRequest = AdminDisableUserRequest.builder()
            .userPoolId(jwtConfiguration.userPoolId)
            .username(user.cognitoUsername)
            .build()

        cognitoClient.adminDisableUser(disableUserRequest)
    }

    fun verifyEmail(email: String) {
        val emailVerificationAttributes = AttributeType.builder().name("email_verified").value("true").build()
        val verifyUserEmailRequest = AdminUpdateUserAttributesRequest
            .builder()
            .userPoolId(jwtConfiguration.userPoolId)
            .username(email)
            .userAttributes(emailVerificationAttributes)
            .build()
        cognitoClient.adminUpdateUserAttributes(verifyUserEmailRequest)
    }

    fun enableUser(user: UserAccount) {
        val enableUserRequest = AdminEnableUserRequest.builder()
            .userPoolId(jwtConfiguration.userPoolId)
            .username(user.cognitoUsername)
            .build()

        cognitoClient.adminEnableUser(enableUserRequest)
    }

    fun getUserState(email: String): Boolean {
        if (filterUsers(emailFilter(email)).isEmpty()) {
            throw EntityNotFoundException(ErrorCodes.USER_NOT_FOUND)
        } else {
            return filterUsers(emailFilter(email))[0].enabled()
        }
    }

    fun deleteUser(user: UserAccount) {
        val deleteUserRequest = AdminDeleteUserRequest.builder()
            .userPoolId(jwtConfiguration.userPoolId)
            .username(user.cognitoUsername)
            .build()

        cognitoClient.adminDeleteUser(deleteUserRequest)
    }
    fun addToGroup(user: UserAccount, role: Role) {
        val adminAddUserToGroupRequest = AdminAddUserToGroupRequest.builder()
            .userPoolId(jwtConfiguration.userPoolId)
            .groupName(role.name)
            .username(user.cognitoUsername)
            .build()

        cognitoClient.adminAddUserToGroup(adminAddUserToGroupRequest)
    }

    fun getCognitoUsernameByEmail(email: String): String? {
        val users = filterUsers(emailFilter(email))
        return if (users.isEmpty()) null else users[0].username()
    }

    fun getCognitoUsersByGroup(role: Role): List<CognitoUser> {
        val listUsersInGroupRequest = ListUsersInGroupRequest.builder()
            .userPoolId(jwtConfiguration.userPoolId)
            .groupName(role.name)
            .build()
        val users = cognitoClient.listUsersInGroup(listUsersInGroupRequest).users()

        return users.map {
            CognitoUser(
                it.username(),
                it.getAttribute("phone_number"),
                it.getAttribute("email")
            )
        }
    }

    private fun UserType.getAttribute(attributeName: String): String {
        return attributes().first { it.name() == attributeName }.value()
    }

    fun isPhoneAvailable(phone: String): Boolean {
        return !anyUserFilterRequest(phoneFilter(phone))
    }

    fun isEmailAvailable(email: String): Boolean {
        return !anyUserFilterRequest(emailFilter(email))
    }

    private fun filterUsers(filter: String): List<UserType> {
        val userListRequest = ListUsersRequest.builder()
            .userPoolId(jwtConfiguration.userPoolId)
            .filter(filter)
            .build()
        val response = cognitoClient.listUsers(userListRequest)
        return response.users()
    }

    private fun anyUserFilterRequest(filter: String): Boolean {
        return filterUsers(filter).isNotEmpty()
    }

    private fun emailFilter(email: String): String {
        return "email = \"$email\""
    }

    private fun phoneFilter(phone: String): String {
        return "phone_number = \"$phone\""
    }

    fun login(email: String, password: String): JWTData {
        val adminInitiateAuthRequest = AdminInitiateAuthRequest.builder().authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
            .userPoolId(jwtConfiguration.userPoolId).clientId(getClintId()).authParameters(
                mapOf(Pair("USERNAME", email), Pair("PASSWORD", password))
            ).build()

        return try {
            val auth = cognitoClient.adminInitiateAuth(adminInitiateAuthRequest)
            val request = auth.authenticationResult()
            JWTData(true, request.idToken(), request.tokenType(), request.expiresIn())
        } catch (e: NotAuthorizedException) {
            JWTData(success = false, message = e.localizedMessage.substringBefore(".").toString())
        } catch (e: Exception) {
            JWTData(success = false, message = e.localizedMessage.toString())
        }
    }

    private fun getClintId(): String {
        println(appConfig.environment + "")
        return if (appConfig.environment.equals("prod", true)) {
            "3sk23u48ub5ctr35u47m2o4dos"
        } else if (appConfig.environment.equals("test", true)) {
            "qcakkm5vn7tuqo5umda4otuf3"
        } else if (appConfig.environment.equals("uat", true)) {
            "2f7amn9bg5877rkvou8c0b24m0"
        } else if (appConfig.environment.equals("poc", true)) {
            "3icfcrqubcueiv2t1i8h56sb11"
        } else if (appConfig.environment.equals("ddf-uat", true)) {
            "69nabm3pk9rkro74haiaso342f"
        } else if (appConfig.environment.equals("ddf", true)) {
            "3qg0396almtpalkv4k6gm0nta3"
        } else if (appConfig.environment.equals("geidea-dev", true)) {
            "743tba3tmdqtpn3ns8tnmhl1ok"
        } else if (appConfig.environment.equals("geidea-test", true)) {
            "6qk31hio355uipf9qkm8ct5960"
        } else if (appConfig.environment.equals("privatechain-prod", true)) {
            "6anr59nb8dfj1nrjetlcuc76jn"
        } else {
            "2pge6nhui8edjckr5fts5o0a1u"
        }
    }

    fun addToGroupIssuance(issuanceBank: IssuanceBanks, role: Role) {
        println("jwtConfiguration.userPoolId ==>" + jwtConfiguration.userPoolId)
        println("role.name ==>" + role.name)
        println("cognitoUsername ==>" + issuanceBank.cognitoUsername)
        val adminAddUserToGroupRequest = AdminAddUserToGroupRequest.builder()
            .userPoolId(jwtConfiguration.userPoolId)
            .groupName(role.name)
            .username(issuanceBank.cognitoUsername)
            .build()

        cognitoClient.adminAddUserToGroup(adminAddUserToGroupRequest)
    }
}

data class CognitoUser(val username: String, val phoneNumber: String, val email: String)
data class JWTData(
    val success: Boolean,
    val jwtToken: String = "",
    val tokenType: String = "",
    val expireIn: Int = 0,
    val message: String = ""
)
