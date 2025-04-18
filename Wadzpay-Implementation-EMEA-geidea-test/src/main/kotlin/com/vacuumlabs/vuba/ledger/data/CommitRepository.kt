package com.vacuumlabs.vuba.ledger.data

import com.vacuumlabs.vuba.ledger.model.Commit
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CommitRepository : CrudRepository<Commit, String>
