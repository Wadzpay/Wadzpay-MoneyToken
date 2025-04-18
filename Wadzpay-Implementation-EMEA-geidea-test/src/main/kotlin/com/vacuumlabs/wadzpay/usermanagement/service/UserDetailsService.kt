package com.vacuumlabs.wadzpay.usermanagement.service

import com.vacuumlabs.wadzpay.a.EncoderUtil
import com.vacuumlabs.wadzpay.common.DuplicateEntityException
import com.vacuumlabs.wadzpay.common.EntityNotFoundException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.configuration.AppConfig
import com.vacuumlabs.wadzpay.issuance.CommonService
import com.vacuumlabs.wadzpay.rolemanagement.model.Role
import com.vacuumlabs.wadzpay.rolemanagement.model.RoleDataService
import com.vacuumlabs.wadzpay.rolemanagement.model.RoleListingModule
import com.vacuumlabs.wadzpay.rolemanagement.service.LevelService
import com.vacuumlabs.wadzpay.rolemanagement.service.RoleService
import com.vacuumlabs.wadzpay.usermanagement.dataclass.DeactivateUserDetailsRequest
import com.vacuumlabs.wadzpay.usermanagement.dataclass.EditUserDetailsRequest
import com.vacuumlabs.wadzpay.usermanagement.dataclass.GetUserDetailsListRequest
import com.vacuumlabs.wadzpay.usermanagement.dataclass.StatusEnum
import com.vacuumlabs.wadzpay.usermanagement.dataclass.UserDetailsDataResponse
import com.vacuumlabs.wadzpay.usermanagement.dataclass.UserDetailsRequest
import com.vacuumlabs.wadzpay.usermanagement.dataclass.UserDetailsResponse
import com.vacuumlabs.wadzpay.usermanagement.model.Department
import com.vacuumlabs.wadzpay.usermanagement.model.LevelUserDetailsMapping
import com.vacuumlabs.wadzpay.usermanagement.model.LevelUserDetailsMappingRepository
import com.vacuumlabs.wadzpay.usermanagement.model.Nacl
import com.vacuumlabs.wadzpay.usermanagement.model.NaclRepository
import com.vacuumlabs.wadzpay.usermanagement.model.UserComments
import com.vacuumlabs.wadzpay.usermanagement.model.UserCommentsRepository
import com.vacuumlabs.wadzpay.usermanagement.model.UserDetails
import com.vacuumlabs.wadzpay.usermanagement.model.UserDetailsRepository
import com.vacuumlabs.wadzpay.usermanagement.model.UserDetailsTransaction
import com.vacuumlabs.wadzpay.usermanagement.model.UserDetailsTransactionRepository
import com.vacuumlabs.wadzpay.usermanagement.model.UserDetailsViewModel
import com.vacuumlabs.wadzpay.usermanagement.model.comments
import com.vacuumlabs.wadzpay.usermanagement.model.toViewModelLevelUsers
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.NoSuchElementException
import kotlin.Comparator
import kotlin.streams.toList

@Service
class UserDetailsService(
    val userDetailsRepository: UserDetailsRepository,
    val userDetailsTransactionRepository: UserDetailsTransactionRepository,
    val roleService: RoleService,
    val departmentService: DepartmentService,
    val levelUserDetailsMappingRepository: LevelUserDetailsMappingRepository,
    val levelService: LevelService,
    val userCommentsRepository: UserCommentsRepository,
    val commonService: CommonService,
    val naclRepository: NaclRepository,
    val appConfig: AppConfig
) {

    /** Create a new User Details in the database, or throw a [DuplicateEntityException] if it already exists.
     *  Check for existence before update the User Details, if user id not exist then send error code [NoSuchElementException]
     *  Fetch all User Details with active status and send back as mutable list.
     */

    val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun createUser(userDetailsRequest: UserDetailsRequest): Any {
        if (!checkEmail(userDetailsRequest.emailId)) {
            throw EntityNotFoundException(ErrorCodes.INVALID_EMAIL)
        }
        val userAlreadyExistByEmail = userDetailsRepository.getByEmailId(userDetailsRequest.emailId)
        val level = levelService.getByLevelIdAndStatus(userDetailsRequest.currentLevel, true)
        if (level.isEmpty()) {
            throw EntityNotFoundException("No Level found with Id ${userDetailsRequest.currentLevel}")
        }
        val userAlreadyExistByUserId = userDetailsRepository.getByUserPreferenceId(userDetailsRequest.userPreferenceId)
        if (!userAlreadyExistByUserId.isNullOrEmpty()) {
            throw EntityNotFoundException("User already exist with User Id ${userDetailsRequest.userPreferenceId}")
        }
        if (!userDetailsRequest.mobileNo.isNullOrEmpty()) {
            val userAlreadyExistByMobile = userDetailsRepository.getByMobileNo(userDetailsRequest.mobileNo!!)
            if (!userAlreadyExistByMobile.isNullOrEmpty()) {
                throw EntityNotFoundException("User already exist with Mobile ${userDetailsRequest.mobileNo}")
            }
        }
        if (userAlreadyExistByEmail != null) {
            throw EntityNotFoundException("User already exist with email ${userDetailsRequest.emailId}")
        }
        var departmentData: List<Department>? = null
        if (userDetailsRequest.departmentId != null) {
            departmentData = departmentService.getByDepartmentIdAndStatus(userDetailsRequest.departmentId, true)
            if (departmentData.isEmpty()) {
                throw EntityNotFoundException("No  Department found with Id ${userDetailsRequest.departmentId}")
            }
        }
        val roleData = roleService.getByRoleIdAndLevelIdAndStatus(userDetailsRequest.roleId, userDetailsRequest.currentLevel.toInt(), true)
        if (roleData.isNullOrEmpty()) {
            throw EntityNotFoundException("No Role found with Id ${userDetailsRequest.roleId}")
        }
        var lastUserDetails: UserDetails? = null
        if (userDetailsRepository.count() > 0) {
            lastUserDetails = userDetailsRepository.findAll().toList().last()
        }
        val currentTime: Instant = Instant.now()
        val userDetails = UserDetails(
            name = userDetailsRequest.userName,
            userPreferenceId = userDetailsRequest.userPreferenceId,
            countryCode = userDetailsRequest.countryCode,
            mobileNo = userDetailsRequest.mobileNo,
            emailId = userDetailsRequest.emailId.toLowerCase(),
            designation = userDetailsRequest.designation,
            departmentId = departmentData?.get(0),
            roleId = roleData[0],
            status = StatusEnum.PENDING_ACTIVATION,
            createdBy = lastUserDetails,
            updatedBy = lastUserDetails,
            createdAt = currentTime,
            updatedAt = currentTime,
            password = null,
            roleFromUserId = userDetailsRequest.roleFromUserId
        )
        val savedUserId = userDetailsRepository.save(userDetails)
        createUserDetailsTransaction(savedUserId)
        if (appConfig.environment.contains("dev", true)) {
            sendPasswordResetLink(savedUserId, false)
        }
        val comments = userDetailsRequest.comment
        if (!userDetailsRequest.comment.isNullOrEmpty() && lastUserDetails != null) {
            createUserComments(savedUserId, lastUserDetails, comments)
        }
        val levelUserMapping = LevelUserDetailsMapping(
            userId = savedUserId,
            levelId = level[0],
            createdBy = savedUserId.createdBy?.userId,
            createdAt = currentTime
        )
        levelUserDetailsMappingRepository.save(levelUserMapping)
        return UserDetailsResponse(
            userId = savedUserId.userId,
            status = savedUserId.status.statusType
        )
    }

    fun sendPasswordResetLink(savedUserId: UserDetails, isResetPassword: Boolean) {
        val hashedUuid = commonService.hashUuid()
        savedUserId.passwordUuid = hashedUuid
        userDetailsRepository.save(savedUserId)
        if (!isResetPassword) {
            createNacl(savedUserId)
        }
        commonService.sendEmail(savedUserId.emailId, hashedUuid)
    }

    fun createNacl(savedUserId: UserDetails) {
        val saltKey = EncoderUtil.getNewSaltKey()
        val saltKeyObject = Nacl(
            userId = savedUserId,
            naclKey = saltKey,
            createdBy = savedUserId.userId,
            createdAt = Instant.now(),
            updatedBy = savedUserId.userId,
            updatedAt = Instant.now(),
            status = true
        )
        naclRepository.save(saltKeyObject)
    }

    fun createUserDetailsTransaction(savedUserId: UserDetails) {
        val currentTime: Instant = Instant.now()

        val userDetailsTransaction = UserDetailsTransaction(
            userId = savedUserId.userId,
            name = savedUserId.name,
            userPreferenceId = savedUserId.userPreferenceId,
            countryCode = savedUserId.countryCode,
            mobileNo = savedUserId.mobileNo,
            emailId = savedUserId.emailId,
            designation = savedUserId.designation,
            departmentId = savedUserId.departmentId?.departmentId,
            roleId = savedUserId.roleId?.roleId,
            status = savedUserId.status,
            createdUpdatedBy = savedUserId.createdBy?.userId,
            createdUpdatedAt = currentTime,
            password = savedUserId.password,
            roleFromUserId = savedUserId.roleFromUserId
        )
        userDetailsTransactionRepository.save(userDetailsTransaction)
    }

    private fun createUserComments(savedUserId: UserDetails, createdBy: UserDetails, comments: String?) {
        val currentTime: Instant = Instant.now()
        val userComments = UserComments(
            userId = savedUserId,
            comment = comments,
            createdBy = createdBy,
            createdAt = currentTime,
        )
        userCommentsRepository.save(userComments)
    }

    fun updateUser(request: EditUserDetailsRequest): Any {
        val userData = userDetailsRepository.getByUserId(request.userId)
        if (userData != null) {
            if (!request.userPreferenceId.isNullOrEmpty() && request.userPreferenceId != userData.userPreferenceId) {
                val userAlreadyExistByUserId = userDetailsRepository.getByUserPreferenceId(request.userPreferenceId)
                if (!userAlreadyExistByUserId.isNullOrEmpty()) {
                    throw EntityNotFoundException("User already exist with User Id ${request.userPreferenceId}")
                }
            }
            if (!request.mobileNo.isNullOrEmpty() && request.mobileNo != userData.mobileNo) {
                val userAlreadyExistByMobile = userDetailsRepository.getByMobileNo(request.mobileNo!!)
                if (!userAlreadyExistByMobile.isNullOrEmpty()) {
                    throw EntityNotFoundException("User already exist with Mobile ${request.mobileNo}")
                }
            }
            if (!request.emailId.isNullOrEmpty() && request.emailId != userData.emailId) {
                if (!checkEmail(request.emailId)) {
                    throw EntityNotFoundException(ErrorCodes.INVALID_EMAIL)
                }
                val userAlreadyExistByEmail = userDetailsRepository.getByEmailId(request.emailId)
                if (userAlreadyExistByEmail != null) {
                    throw EntityNotFoundException("User already exist with email ${request.emailId}")
                }
            }
            var departmentData: List<Department>? = null
            if (request.departmentId != null) {
                departmentData = departmentService.getByDepartmentIdAndStatus(request.departmentId, true)
                if (departmentData.isEmpty()) {
                    throw EntityNotFoundException("No  Department found with Id ${request.departmentId}")
                }
            }
            var roleData: List<Role>? = null
            if (request.roleId != null) {
                val levelMapping = levelUserDetailsMappingRepository.findByUserId(userData)
                if (!levelMapping.isNullOrEmpty()) {
                    roleData =
                        roleService.getByRoleIdAndLevelIdAndStatus(
                            request.roleId,
                            levelMapping[0].levelId.levelId.toInt(),
                            true
                        )
                    if (roleData.isNullOrEmpty()) {
                        throw EntityNotFoundException("No Role found with Id ${request.roleId}")
                    }
                }
            }
            var lastUserDetails: UserDetails? = null
            if (userDetailsRepository.count() > 0) {
                lastUserDetails = userDetailsRepository.findAll().toList().last()
            }
            var firstUser: UserDetails? = null
            if (userDetailsRepository.count() > 0) {
                firstUser = userDetailsRepository.findAll().toList().first()
            }
            val currentTime: Instant = Instant.now()

            userData.name = if (!request.userName.isNullOrEmpty()) request.userName else userData.name
            userData.userPreferenceId = if (!request.userPreferenceId.isNullOrEmpty()) request.userPreferenceId else userData.userPreferenceId
            userData.countryCode = if (!request.countryCode.isNullOrEmpty()) request.countryCode else userData.countryCode
            userData.mobileNo = if (!request.mobileNo.isNullOrEmpty()) request.mobileNo else userData.mobileNo
            userData.emailId = if (!request.emailId.isNullOrEmpty()) request.emailId.toLowerCase() else userData.emailId
            userData.designation = request.designation
            userData.departmentId = if (request.departmentId != null) departmentData?.get(0) else userData.departmentId
            userData.roleId = if (request.roleId != null) roleData?.get(0) else userData.roleId
            userData.updatedBy = if (firstUser == lastUserDetails) null else firstUser
            userData.updatedAt = currentTime
            userData.roleFromUserId = request.roleFromUserId
            val savedUserId = userDetailsRepository.save(userData)
            createUserDetailsTransaction(savedUserId)
            if (!request.comment.isNullOrEmpty() && lastUserDetails != null) {
                createUserComments(savedUserId, lastUserDetails, request.comment)
            }
            return UserDetailsResponse(
                userId = savedUserId.userId,
                status = savedUserId.status.statusType
            )
        }
        throw EntityNotFoundException("No  User found with user Id ${request.userId}")
    }

    fun fetchUsersFiltersPagination(request: GetUserDetailsListRequest): Any {
        val level = levelService.getByLevelIdAndStatus(request.currentLevel!!, true)
        if (level.isNotEmpty()) {
            val userListingFilter: MutableList<LevelUserDetailsMapping>? =
                levelUserDetailsMappingRepository.findByLevelId(
                    level[0]
                )
            val userDetails = mutableListOf<UserDetailsViewModel>()
            if (!userListingFilter.isNullOrEmpty()) {
                userListingFilter.forEach { data ->
                    userDetails.add(data.toViewModelLevelUsers())
                }
            }
            var userDetailsTotal = mutableListOf<UserDetailsViewModel>()
            var userDetailsViewModel: List<UserDetailsViewModel> = userDetails.toList()
            if (userDetailsViewModel.isNotEmpty()) {
                userDetailsTotal = userDetailsViewModel.filterNot { e ->
                    e.status?.contains(StatusEnum.DEACTIVATED.statusType, ignoreCase = true) ?: false
                }.toMutableList()
                userDetailsViewModel = userDetailsViewModel.filterNot { e ->
                    e.status?.contains(StatusEnum.DEACTIVATED.statusType, ignoreCase = true) ?: false
                }
            }
            /** Filter by Dates*/
            if (request.fromDate != null && request.toDate != null) {
                val createdTo = request.toDate!!.toInstant()
                val nextDay = createdTo?.plus(1, ChronoUnit.DAYS)
                request.toDate = Date.from(nextDay)
                userDetailsViewModel = userDetailsViewModel.filter { e ->
                    Date.from(e.createdAt) >= request.fromDate && Date.from(e.createdAt) <= request.toDate
                }
                userDetailsTotal = userDetailsViewModel.toMutableList()
            }
            /** Filter by User Name*/
            if (!request.userName.isNullOrEmpty()) {
                userDetailsViewModel = userDetailsViewModel.filter { e ->
                    e.userName.contains(request.userName!!, ignoreCase = true)
                }
            }
            /** Filter by User Preference Id*/
            if (!request.userPreferenceId.isNullOrEmpty()) {
                userDetailsViewModel = userDetailsViewModel.filter { e ->
                    e.userPreferenceId?.contains(request.userPreferenceId.toString(), ignoreCase = true) ?: false
                }
            }
            /** Filter by User email Id*/
            if (!request.userEmail.isNullOrEmpty()) {
                userDetailsViewModel = userDetailsViewModel.filter { e ->
                    e.userEmail?.contains(request.userEmail.toString(), ignoreCase = true) ?: false
                }
            }
            /** Filter by Assigned Role*/
            if (!request.assignedRole.isNullOrEmpty()) {
                userDetailsViewModel = userDetailsViewModel.filter { e ->
                    e.assignedRole?.contains(request.assignedRole.toString(), ignoreCase = true) ?: false
                }
            }
            /** Filter by Requested By*/
            if (!request.requestedBy.isNullOrEmpty()) {
                userDetailsViewModel = userDetailsViewModel.filter { e ->
                    e.requestedBy?.contains(request.requestedBy.toString(), ignoreCase = true) ?: false
                }
            }
            /** Filter by Action By*/
            if (!request.actionBy.isNullOrEmpty()) {
                userDetailsViewModel = userDetailsViewModel.filter { e ->
                    e.actionBy?.contains(request.actionBy!!, ignoreCase = true) ?: false
                }
            }
            /** Filter by Updated At*/
            if (request.updatedAt != null) {
                userDetailsViewModel = userDetailsViewModel.filter { e ->
                    e.updatedAt.toString().contains(request.updatedAt.toString(), ignoreCase = true)
                }
            }
            /** Filter by Last active*/
            if (request.lastActiveAt != null) {
                userDetailsViewModel = userDetailsViewModel.filter { e ->
                    e.lastActiveAt.toString().contains(request.lastActiveAt.toString(), ignoreCase = true)
                }
            }
            /** Filter by Status */
            if (!request.status.isNullOrEmpty()) {
                userDetailsViewModel = userDetailsViewModel.filter { e ->
                    e.status?.contains(request.status.toString(), ignoreCase = true) ?: false
                }
            }
            if (request.sortField?.isNotEmpty() == true) {
                if (request.sortOrder?.toLowerCase() == "asc") {
                    userDetailsViewModel = userDetailsViewModel.sortedWith(dynamicComparator(request.sortField, true))
                }
                if (request.sortOrder?.toLowerCase() == "desc") {
                    userDetailsViewModel = userDetailsViewModel.sortedWith(dynamicComparator(request.sortField, false))
                }
            } else {
                userDetailsViewModel = userDetailsViewModel.sortedWith(dynamicComparator("userId", true))
            }
            userDetailsViewModel.forEach { data ->
                data.comment = getCommentsByUserId(data.userId)
            }
            val pagination = RoleDataService.Pagination(
                current_page = request.page,
                total_records = userDetailsViewModel.size,
                total_pages = roleService.calculateTotalNoPages(
                    userDetailsViewModel.size.toDouble(),
                    if (request.limit > 0) request.limit.toDouble() else userDetailsViewModel.size.toDouble()
                )
            )
            if (request.page != null && request.page > 0) {
                val pageNo = request.page - 1
                userDetailsViewModel =
                    userDetailsViewModel.stream().skip(pageNo * request.limit)
                        .limit(request.limit).toList()
            }
            return UserDetailsDataResponse(
                userDetailsTotal.size,
                userDetailsViewModel,
                pagination
            )
        }
        val userDetailsViewModel = mutableListOf<UserDetailsViewModel>()
        return UserDetailsDataResponse(
            0,
            userDetailsViewModel,
            RoleDataService.Pagination(
                current_page = request.page,
                total_records = 0,
                total_pages = 0.0
            )
        )
    }

    // Function to dynamically get a comparator based on a property
    fun dynamicComparator(property: String, ascending: Boolean = true): Comparator<UserDetailsViewModel> {
        val comparator = when (property) {
            "userName" -> compareBy<UserDetailsViewModel> { it.userName.toLowerCase() }
            "userPreferenceId" -> compareBy { it.userPreferenceId?.toLowerCase() }
            "userEmail" -> compareBy { it.userEmail?.toLowerCase() }
            "assignedRole" -> compareBy { it.assignedRole?.toLowerCase() }
            "requestedBy" -> compareBy { it.requestedBy?.toLowerCase() }
            "actionBy" -> compareBy { it.actionBy?.toLowerCase() }
            "createdAt" -> compareBy { it.createdAt }
            "updatedAt" -> compareBy { it.updatedAt }
            "lastActiveAt" -> compareBy { it.lastActiveAt }
            "status" -> compareBy { it.status?.toLowerCase() }
            "userId" -> compareBy { it.userId }
            else -> compareBy { it.userId }
        }
        return if (ascending) comparator else comparator.reversed()
    }

    fun getRolesByUsers(currentLevel: Int): MutableList<RoleListingModule> {
        val level = levelService.getByLevelIdAndStatus(currentLevel.toShort(), true)
        val roleData = mutableListOf<RoleListingModule>()
        if (level.isNotEmpty()) {
            val userListingFilter: MutableList<LevelUserDetailsMapping>? =
                levelUserDetailsMappingRepository.findByLevelId(
                    level[0]
                )
            var userDetails = mutableListOf<UserDetailsViewModel>()
            if (!userListingFilter.isNullOrEmpty()) {
                userListingFilter.forEach { data ->
                    userDetails.add(data.toViewModelLevelUsers())
                }
            }
            if (userDetails.isNotEmpty()) {
                userDetails = userDetails.filterNot { e ->
                    e.status?.contains(StatusEnum.DEACTIVATED.statusType, ignoreCase = true) ?: false
                }.toMutableList()
            }
            if (userDetails.isNotEmpty()) {
                val roleDataListReturn = mutableListOf<RoleListingModule>()
                userDetails.forEach { data ->
                    val roleDataList = roleService.getRolesByRoleIdLevelId(data.assignedRoleId!!, currentLevel)
                    if (roleDataList.isNotEmpty()) {
                        roleDataList[0].userPreferenceId = data.userPreferenceId
                        roleDataList[0].userId = data.userId
                        roleDataList[0].userName = data.userName
                        roleDataListReturn.add(roleDataList[0])
                    }
                }
                return roleDataListReturn
            }
        }
        return roleData
    }

    fun checkEmail(username: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$"
        return username.matches(emailRegex.toRegex())
    }

    fun deActivateUser(request: DeactivateUserDetailsRequest): Any {
        val currentTime: Instant = Instant.now()

        val userData = userDetailsRepository.getByUserId(request.userId)
        if (userData != null) {
            var lastUserDetails: UserDetails? = null
            if (userDetailsRepository.count() > 0) {
                lastUserDetails = userDetailsRepository.findAll().toList().last()
            }
            var firstUser: UserDetails? = null
            if (userDetailsRepository.count() > 0) {
                firstUser = userDetailsRepository.findAll().toList().first()
            }
            userData.status = StatusEnum.DEACTIVATED
            // userData.comment = if (!request.comment.isNullOrEmpty()) request.comment else userData.comment
            userData.updatedBy = if (firstUser == lastUserDetails) null else firstUser
            userData.updatedAt = currentTime
            val savedUserId = userDetailsRepository.save(userData)
            createUserDetailsTransaction(savedUserId)
            if (!request.comment.isNullOrEmpty() && lastUserDetails != null) {
                createUserComments(savedUserId, lastUserDetails, request.comment)
            }
            return UserDetailsResponse(
                userId = savedUserId.userId,
                status = savedUserId.status.statusType
            )
        }
        throw EntityNotFoundException("No  User found with user Id ${request.userId}")
    }

    fun getUserCountByRole(role: Role): Int {
        val userData = userDetailsRepository.getByRoleId(role)
        if (userData != null) {
            return userData.size
        }
        return 0
    }

    fun getCommentsByUserId(userId: Long): MutableList<comments> {
        val userDetails = userDetailsRepository.getByUserId(userId)
        val userComments = userDetails?.let { userCommentsRepository.getByUserId(it) }
        val commentsList = mutableListOf<comments>()
        if (!userComments.isNullOrEmpty()) {
            userComments.forEach { data ->
                val comments = comments(
                    userCommentId = data.userCommentId,
                    comment = data.comment,
                    commentDate = data.createdAt,
                    createdBy = data.createdBy?.name
                )
                commentsList.add(comments)
            }
        }
        return commentsList.sortedByDescending { e -> e.userCommentId }.toMutableList()
    }

    fun getUserDetailsByEmail(email: String): Boolean {
        val userAlreadyExistByEmail = userDetailsRepository.getByEmailIdIgnoreCase(email)
        return !userAlreadyExistByEmail.isNullOrEmpty()
    }

    fun getUserDetailsByEmailAndPassword(email: String, password: String): UserDetails? {
        return userDetailsRepository.getUserDetailsByEmailIdAndPasswordAndStatus(email, password, StatusEnum.ACTIVE)
    }

    fun getSaltKeyByUserId(userDetails: UserDetails): String {
        val saltKey = naclRepository.getByUserId(userDetails)
        if (saltKey != null) {
            return saltKey.naclKey!!
        }
        throw EntityNotFoundException(ErrorCodes.SALT_KEY_NOT_FOUND)
    }
}
