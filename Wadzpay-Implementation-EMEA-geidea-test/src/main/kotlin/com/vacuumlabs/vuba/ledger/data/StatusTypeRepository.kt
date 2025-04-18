package com.vacuumlabs.vuba.ledger.data

import com.vacuumlabs.vuba.ledger.model.StatusType
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface StatusTypeRepository : CrudRepository<StatusType, String>
