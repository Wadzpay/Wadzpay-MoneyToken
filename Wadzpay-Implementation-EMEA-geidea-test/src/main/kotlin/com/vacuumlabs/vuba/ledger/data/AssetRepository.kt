package com.vacuumlabs.vuba.ledger.data

import com.vacuumlabs.vuba.ledger.model.Asset
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface AssetRepository : CrudRepository<Asset, String>
