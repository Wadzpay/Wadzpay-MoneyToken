package com.vacuumlabs.wadzpay.rolemanagement.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.Instant
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "modules")
data class Module(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var moduleId: Short = 0,
    @Column(nullable = true, unique = true)
    var moduleName: String?,
    @Column(nullable = true, unique = true)
    var moduleType: String?,
    @Column(nullable = true, unique = true)
    var moduleUrl: String?,
    @Column(nullable = true, unique = true)
    var imageUrl: String?,
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = true)
    var parent: Module?,
    @JsonIgnore
    var sorting: BigDecimal = BigDecimal("4"),
    @Column(nullable = true, unique = true)
    var createdAt: Instant? = Instant.now(),
    @Column(nullable = true, unique = true)
    var createdBy: Long? = 91,
    @Column(nullable = true, unique = true)
    var updatedAt: Instant? = Instant.now(),
    @Column(nullable = true, unique = true)
    var updatedBy: Long? = 91,
    @Column(nullable = true)
    var parentName: String?,
    @Transient
    var key: Short?,
    @Transient
    var name: String?,
    @Column(nullable = true, unique = true)
    var status: Boolean?,
    @OneToMany(mappedBy = "parent", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val children: MutableList<Module> = mutableListOf()

) {

    override fun toString(): String {
        return "Module(id=$moduleId, name='$moduleName')"
    }
}

@Repository
interface ModuleRepository : CrudRepository<Module, Long> {
    fun getByModuleId(moduleId: Short): Module
    @Query("select rm from Module rm where rm.parent.moduleId is null")
    fun findRootModules(): MutableList<Module>

    fun findAllByParentModuleIdIsNull(): List<Module>

    @Query("select rm from Module rm where rm.parent.moduleId = :parentId")
    fun findAllByParentId(parentId: Short): MutableList<Module>

    fun getByModuleIdAndStatus(moduleId: Short, status: Boolean?): MutableList<Module>?
}
