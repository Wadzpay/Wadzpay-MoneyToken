package com.vacuumlabs.wadzpay.rolemanagement.model

import au.com.console.jpaspecificationdsl.get
import com.vacuumlabs.wadzpay.usermanagement.dataclass.StatusEnum
import com.vacuumlabs.wadzpay.usermanagement.model.UserDetails
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import javax.persistence.Entity
import javax.persistence.EntityManager
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Join
import javax.persistence.criteria.JoinType
import javax.persistence.criteria.Order
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

@Entity
@Table(name = "role")
data class Role(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val roleId: Int = 0,
    var roleName: String,
    @ManyToOne
    @JoinColumn(name = "level_id")
    var levelId: Level,
    var aggregatorId: String
) {

    var roleComments: String = ""
    var createdBy: String? = null
    var createdAt: Instant = Instant.now()
    var updatedBy: String? = null
    var updatedAt: Instant = Instant.now()
    @Transient
    var users: Long = 0
    var status: Boolean? = true
}
@Repository
interface RoleRepository : CrudRepository<Role, Int>, RoleCustomRepository {
    @Query(
        "SELECT  NEW com.vacuumlabs.wadzpay.rolemanagement.model.RoleListing(r,ma,1L)  \n" +
            "FROM Role r \n" +
            "LEFT JOIN FETCH Level l ON r.levelId.levelId = l.levelId\n" +
            "LEFT JOIN FETCH RoleModule ma ON r.roleId = ma.roleId.roleId\n where r.levelId=:level ORDER BY " +
            "CASE WHEN :sortBy = 'roleId' THEN r.roleId   END ASC, " +
            "CASE WHEN :sortBy = 'roleName' THEN r.roleName END ASC"
        /*"           \"CASE WHEN :sortBy = 'roleId' THEN r.roleId END :sortDirection, \" +\n" +
        "           \"CASE WHEN :sortBy = 'roleName' THEN r.roleName END :sortDirection, \" +\n" +
        "           \"CASE WHEN :sortBy = 'status' THEN r.status END :sortDirection \" "*/
    )

    fun findByRecordsWithModuleRelationDD(level: Int, sortBy: String = "roleId"): MutableList<RoleListing>
    @Query(
        "SELECT  NEW com.vacuumlabs.wadzpay.rolemanagement.model.RoleListing(r,ma,1L)  \n" +
            "FROM Role r \n" +
            "LEFT JOIN FETCH Level l ON r.levelId.levelId = l.levelId\n" +
            "LEFT JOIN FETCH RoleModule ma ON r.roleId = ma.roleId.roleId\n" +

            " where l.levelNumber=:level and r.aggregatorId=:aggregatorId"
        /*"CASE WHEN :sortBy = 'roleId' THEN r.roleId   END ,\n " +
        "CASE WHEN :sortBy = 'roleName' THEN r.roleName END "*/
        /*"           \"CASE WHEN :sortBy = 'roleId' THEN r.roleId END :sortDirection, \" +\n" +
        "           \"CASE WHEN :sortBy = 'roleName' THEN r.roleName END :sortDirection, \" +\n" +
        "           \"CASE WHEN :sortBy = 'status' THEN r.status END :sortDirection \" "*/
    )

    fun findByRecordsWithModuleRelation(level: Short, aggregatorId: String, sort: Sort): MutableList<RoleListing>

    /*@Query("SELECT r.*, ma.*\n,(select count(u) from user_details u where u.role_id=r.role_id) " +
    "FROM role r\n" +
    "LEFT JOIN levels l ON r.level_id = l.level_id\n" +
    "LEFT JOIN role_modules ma ON r.role_id = ma.role_id\n" +
    "WHERE l.level_number = :level AND r.aggregator_id = :aggregatorId\n", nativeQuery = true)*/
    @Query(
        "SELECT  NEW com.vacuumlabs.wadzpay.rolemanagement.model.RoleListing(r,ma,(SELECT COUNT(u) FROM UserDetails u WHERE u.roleId = r))  \n" +
            "FROM Role r \n" +
            "LEFT JOIN FETCH Level l ON r.levelId.levelId = l.levelId\n" +
            "LEFT JOIN FETCH RoleModule ma ON r.roleId = ma.roleId.roleId\n" +

            " where l.levelNumber=:level and r.aggregatorId=:aggregatorId"
    )

    fun findByRecordsWithModuleRelationV22(@Param("level") level: Short, @Param("aggregatorId") aggregatorId: String): Any

    fun getByRoleIdAndLevelIdAndStatus(roleId: Int, levelId: Level, status: Boolean): MutableList<Role>?

    fun getByLevelIdAndStatus(levelId: Level, status: Boolean, sort: Sort): MutableList<Role>?
    @Query("from Role where levelId=:levelId  and status=:status ")
    fun getByLevelIdAndAggregatorIdAndStatus(levelId: Int, status: Boolean, sort: Sort): MutableList<Role>?
}

interface RoleCustomRepository {
    fun findByRecordsWithModuleRelationV2(level: Short, aggregatorId: String, request: RoleDataService.GetRoleListRequest): Any
}

@Repository
class RoleCustomRepositoryImpl(
    private val entityManager: EntityManager
) : RoleCustomRepository {

    override fun findByRecordsWithModuleRelationV2(@Param(value = "levelNum")levelNum: Short, @Param(value = "aggregatorId")aggregatorId: String, request: RoleDataService.GetRoleListRequest): Any {

// Step 1: Initialize CriteriaBuilder, CriteriaQuery, and Root
        // Step 1: Initialize CriteriaBuilder, CriteriaQuery, and Root
        /* val offset=request.limit.toInt()*(request.page?.minus(1)!!)
        val sql = """
        SELECT r.*, ma.*,
               (SELECT COUNT(u) FROM user_details u WHERE u.role_id = r.role_id) AS userDetailsCount
        FROM role r
        LEFT JOIN levels l ON r.level_id = l.level_id
        LEFT JOIN role_modules ma ON r.role_id = ma.role_id
        ORDER BY userDetailsCount asc
        LIMIT 10 OFFSET 0
    """.trimIndent()
        //        WHERE l.level_number = 0 AND r.aggregator_id = 1
        var queryTotal1 = entityManager.createNativeQuery(sql);
        var countResult1 = queryTotal1.resultList.size;

        val query1 = entityManager.createNativeQuery(sql)
     //   query1.setParameter("level", 0)
      //  query1.setParameter("aggregatorId", 1)
       // query1.setParameter("firstResult", request.limit.toInt()*(request.page?.minus(1)!!))
       // query1.setParameter("maxResults", 10)
        val results =  query1.resultList as List<Tuple>
   val roleListingFilter1 =     results.map {
       RoleDataService.RoleListingDTO(
           roleId = it[0] as Int,
           roleName = it[1] as String,
           roleCreatedAt = it[2] as Instant,
           roleUpdatedAt = it[3] as Instant,
           aggregatorId = it[4] as String,
           roleStatus = it[5] as Boolean?,
           levelId = it[6] as Short,
           levelName = it[7] as String,
           levelNumber = it[8] as Short,
           levelCreatedAt = it[9] as Instant,
           levelUpdatedAt = it[10] as Instant,
           moduleIdArray = (it[11] as java.sql.Array).array as Array<Int>,
           roleModuleId = it[12] as Int,
           roleModuleCreatedAt = it[13] as Instant,
           roleModuleUpdatedAt = it[14] as Instant,
           userDetailsCount = it[15] as Long
       )}
       val pagination1 = RoleDataService.Pagination(
           current_page = request.page,
           total_records = countResult1,
           total_pages = calculateTotalNoPages(
               countResult1.toDouble(),
               if (request.limit > 0) request.limit.toDouble() else countResult1.toDouble()
           )
       )

       return RoleDataService.RoleDataResponseV3(countResult1,roleListingFilter1,pagination1)
*/

        val cb: CriteriaBuilder = entityManager.criteriaBuilder
        val cq: CriteriaQuery<Any> = cb.createQuery(Any::class.java)

        // Define the root entity as RoleModule
        val roleModuleRoot: Root<RoleModule> = cq.from(RoleModule::class.java)

        // Join from RoleModule to Role
        val roleJoin: Join<RoleModule, Role> = roleModuleRoot.join("roleId", JoinType.LEFT)

        // Join from Role to Level
        val levelJoin: Join<Role, Level> = roleJoin.join("levelId", JoinType.LEFT)

        // Create subquery for COUNT
        val countSubquery = cq.subquery(Long::class.java)
        val userDetailsRoot = countSubquery.from(UserDetails::class.java)
        val cbCount: CriteriaBuilder = entityManager.criteriaBuilder
        val predicatesCount = mutableListOf<Predicate>()

        countSubquery.select(cbCount.count(userDetailsRoot))
        predicatesCount.add(cbCount.equal(userDetailsRoot.get<Role>("roleId").get<Any>("roleId"), roleJoin.get<Role>("roleId")))
        predicatesCount.add(cbCount.notEqual(userDetailsRoot.get<Any>("status"), StatusEnum.DEACTIVATED))
        countSubquery.where(
            *predicatesCount.toTypedArray()
        )

        // Construct the main query with the RoleListing constructor
        cq.select(
            cb.construct(
                RoleListingV2::class.java,
                roleJoin,
                roleModuleRoot,
                countSubquery
            )
        )
        val predicates = mutableListOf<Predicate>()
        predicates.add(cb.isTrue(roleJoin.get<Boolean>("status")))
        predicates.add(cb.equal(levelJoin.get<Int>("levelNumber"), levelNum))
        predicates.add(cb.equal(roleJoin.get<Long>("aggregatorId"), aggregatorId))

        if (request.startDate != "" && request.startDate != null && request.endDate != "" && request.endDate != null) {
            predicates.add(
                cb.between(roleJoin.get("updatedAt"), Instant.parse(request.startDate), Instant.parse(request.endDate))
            )
        }
        if ((request.roleName != "" && request.roleName != null) || (request.roleId != 0 && request.roleId != null) || (request.updatedBy != "" && request.updatedBy != null) || (request.status == true && request.status != null)) {
            if (request.roleName != "" && request.roleName != null) {
                request.roleName?.let {
                    predicates.add(cb.like(cb.lower(roleJoin.get("roleName")), "%${it.toLowerCase()}%"))
                }
            }

            if (request.roleId != 0 && request.roleId != null) {
                request.roleId?.let {
                    predicates.add(cb.equal(roleJoin.get<Int>("roleId"), it))
                }
            }
            if (request.updatedBy != "" && request.updatedBy != null) {
                request.updatedBy?.let {
                    predicates.add(cb.like(cb.lower(roleJoin.get<String>("updatedBy")), "%${it.toLowerCase()}%"))
                }
            }

            cq.where(*predicates.toTypedArray())
        }
        var sortField = "updatedAt"
        if (request.sortField != null) {
            sortField = request.sortField
        }
        if (sortField == "users") {
            val order: Order = if (request.sortOrder.equals("asc", ignoreCase = true)) {
                cb.asc(countSubquery.selection)
            } else {
                cb.desc(countSubquery.selection)
            }
            cq.orderBy(order)
        } else {
            val order: Order = if (request.sortOrder.equals("asc", ignoreCase = true)) {
                cb.asc(roleJoin.get<Any>(sortField))
            } else {
                cb.desc(roleJoin.get<Any>(sortField))
            }

            cq.orderBy(order)
        }

        var queryTotal = entityManager.createQuery(cq)
        var totalRoles = queryTotal.resultList
        var countResult = totalRoles.size

        if (request.page == 0L) {
            return RoleDataService.RoleDataResponseV3(countResult, totalRoles, null)
        }

        // Execute the query
        val query: TypedQuery<Any> = entityManager.createQuery(cq)
        query.firstResult = (request.limit.toInt() * (request.page?.minus(1)!!)).toInt()
        query.maxResults = request.limit.toInt()
        println(query)
        val roleListingFilter = query.resultList
        val pagination = RoleDataService.Pagination(
            current_page = request.page,
            total_records = countResult,
            total_pages = calculateTotalNoPages(
                countResult.toDouble(),
                if (request.limit > 0) request.limit.toDouble() else countResult.toDouble()
            )
        )

        return RoleDataService.RoleDataResponseV3(countResult, roleListingFilter, pagination)

       /*
      return query.resultList
*/

       /*        val cb = entityManager.criteriaBuilder
      val cq: CriteriaQuery<Any> = cb.createQuery(Any::class.java)
      val role: Root<Role> = cq.from(Role::class.java)

// Step 2: Define the JOINs

// Step 2: Define the JOINs
      val level: Join<Role, Level>? = role.join<Role, Level>("levelId", JoinType.LEFT)
      //val roleModule: Join<Role, RoleModule>? = role.join<Role, RoleModule>("roleId", JoinType.LEFT).join("roleId")
 //   val roleModule=  entityManager.createQuery("FROM Role r LEFT JOIN FETCH RoleModule ma ON r.roleId = ma.roleId.roleId")
      val roleJoin: Join<RoleModule, Role> = roleModuleRoot.join("role", JoinType.LEFT)

// Step 3: Create the subquery for counting UserDetails

// Step 3: Create the subquery for counting UserDetails
      val userCountSubquery: Subquery<Long> = cq.subquery(Long::class.java)
      val userDetails: Root<UserDetails> = userCountSubquery.from(UserDetails::class.java)
      userCountSubquery.select(cb.count(userDetails))
      userCountSubquery.where(cb.equal(userDetails.get<Role>("roleId").get<Any>("roleId"), role.get<Any>("roleId")))

// Step 4: Construct the RoleListing object using multiselect

// Step 4: Construct the RoleListing object using multiselect
      cq.multiselect(
          role,
          roleModule,
          userCountSubquery
      )

// Step 5: Add the WHERE conditions

// Step 5: Add the WHERE conditions
     *//* if (level != null) {
          cq.where(
              cb.equal(level.get<Any>("levelNumber"), cb.parameter(Integer::class.java, "level")),
              cb.equal(role.get<Any>("aggregatorId"), cb.parameter(Long::class.java, "aggregatorId"))
          )
      }*//*

// Step 6: Create the query and set parameters

// Step 6: Create the query and set parameters
     val query = entityManager.createQuery<Any>(cq.distinct(true))
    // query.setParameter("level", 0)
    // query.setParameter("aggregatorId", 1L)

// Step 7: Execute the query

// Step 7: Execute the query
      val resultList = query.resultList
      return resultList*/
    }
}

fun calculateTotalNoPages(size: Double, limit: Double): Double {
    val totalNoPages = Math.ceil((size / limit).toDouble())
    return if (totalNoPages > 0) {
        totalNoPages
    } else {
        1.0
    }
}
