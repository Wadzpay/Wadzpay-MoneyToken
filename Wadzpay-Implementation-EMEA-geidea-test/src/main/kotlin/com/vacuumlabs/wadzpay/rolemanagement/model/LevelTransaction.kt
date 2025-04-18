package com.vacuumlabs.wadzpay.rolemanagement.model

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "levels_transaction")
data class LevelTransaction(
    val levelId: Short,
    val levelName: String,
    val levelNumber: Short
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val levelTransactionId: Long = 0
    var imageUrl: String = ""
    var createdUpdatedBy: Long = 0
    var createdUpdatedAt: Instant = Instant.now()
    var status: Boolean = false
}
@Repository
interface LevelTransactionRepository : CrudRepository<LevelTransaction, Long>
