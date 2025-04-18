package com.vacuumlabs.vuba.ledger.data

import com.vacuumlabs.vuba.ledger.model.ResponseHistory
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface HistoryRepository : CrudRepository<ResponseHistory, Long> {
    fun findByIdempotencyNamespaceAndIdempotencyKey(idempotencyNamespace: String, idempotencyKey: String): ResponseHistory?
}
