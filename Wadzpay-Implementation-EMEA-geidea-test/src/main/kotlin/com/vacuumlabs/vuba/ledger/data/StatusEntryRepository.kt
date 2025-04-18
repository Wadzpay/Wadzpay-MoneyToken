package com.vacuumlabs.vuba.ledger.data

import com.vacuumlabs.vuba.ledger.model.StatusEntry
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface StatusEntryRepository : CrudRepository<StatusEntry, Long> {
    fun findFirstByStatusReferenceOrderByIdDesc(reference: String): StatusEntry?
}
