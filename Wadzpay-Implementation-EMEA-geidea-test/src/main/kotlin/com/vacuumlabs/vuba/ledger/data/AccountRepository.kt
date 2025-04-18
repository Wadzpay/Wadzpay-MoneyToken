package com.vacuumlabs.vuba.ledger.data

import com.vacuumlabs.vuba.ledger.model.Account
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface AccountRepository : CrudRepository<Account, String>
