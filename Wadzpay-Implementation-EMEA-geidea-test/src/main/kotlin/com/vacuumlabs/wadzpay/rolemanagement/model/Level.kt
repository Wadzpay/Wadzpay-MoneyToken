package com.vacuumlabs.wadzpay.rolemanagement.model

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "levels")
data class Level(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val levelId: Short = 0,
    var levelName: String,
    var levelNumber: Short = 0
) {
    var imageUrl: String = ""
    var createdBy: Long = 0
    var createdAt: Instant = Instant.now()
    var updatedBy: Long = 0
    var updatedAt: Instant = Instant.now()
    var status: Boolean? = null
}
@Repository
interface LevelRepository : CrudRepository<Level, Short> {
    @Query("from Level l order by l.levelNumber asc ")
    fun findAllLevels(): MutableList<Level>
    fun findByLevelNumber(levelNumber: Short): Level?
    fun getByLevelIdAndStatus(levelId: Short, status: Boolean): MutableList<Level>
}
