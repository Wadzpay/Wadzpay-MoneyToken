package com.vacuumlabs.vuba.ledger.data

import com.vacuumlabs.vuba.ledger.model.Subaccount
import com.vacuumlabs.vuba.ledger.model.SubaccountEntry
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface SubaccountEntryRepository : CrudRepository<SubaccountEntry, Long> {
    fun findFirstBySubaccountReferenceOrderByIdDesc(reference: String): SubaccountEntry?

    fun findByCommitReferenceAndSubaccount(reference: String, subAccountReferences: Subaccount): SubaccountEntry?
}
