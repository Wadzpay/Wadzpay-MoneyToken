package com.vacuumlabs.vuba.ledger.data

import com.vacuumlabs.vuba.ledger.model.Status
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface StatusRepository : CrudRepository<Status, String> {
    fun findAllByAccountReference(ref: String): List<Status>
    fun findAllByAccountReferenceAndTypeReference(accountRef: String, statusTypeRef: String): List<Status>
}
