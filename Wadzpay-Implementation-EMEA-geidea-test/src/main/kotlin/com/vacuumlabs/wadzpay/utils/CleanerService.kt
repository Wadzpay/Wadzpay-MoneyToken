package com.vacuumlabs.wadzpay.utils

import com.vacuumlabs.wadzpay.ledger.LedgerInitializerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager

@Service
class CleanerService @Autowired constructor(
    private val entityManager: EntityManager,
    val ledgerInitializerService: LedgerInitializerService
) {
    val MIGRATION_TABLE = "flyway_schema_history"

    @Transactional
    fun deleteData() {
        entityManager.flush()
        val tableNames = entityManager.createNativeQuery("select tablename from pg_tables where schemaname='public'").resultList.filter { it != MIGRATION_TABLE }
        entityManager.createNativeQuery("SET session_replication_role = 'replica'").executeUpdate()
        tableNames.forEach { tableName ->
            // delete data and reset sequences
            entityManager.createNativeQuery("TRUNCATE TABLE $tableName RESTART IDENTITY CASCADE").executeUpdate()
        }
        entityManager.createNativeQuery("SET session_replication_role = 'origin'").executeUpdate()
    }

    @Transactional
    fun createInitData() {
        ledgerInitializerService.createAssets()
        ledgerInitializerService.createAccounts()
    }
}
