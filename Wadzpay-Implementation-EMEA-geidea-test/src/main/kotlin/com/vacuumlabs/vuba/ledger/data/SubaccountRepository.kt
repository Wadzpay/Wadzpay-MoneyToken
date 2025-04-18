package com.vacuumlabs.vuba.ledger.data

import com.vacuumlabs.vuba.ledger.model.Subaccount
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface SubaccountRepository : CrudRepository<Subaccount, String> {
    fun findAllByAccountReference(ref: String): List<Subaccount>
    fun findAllByAccountReferenceAndAssetIdentifier(accountRef: String, assetId: String): List<Subaccount>
}
