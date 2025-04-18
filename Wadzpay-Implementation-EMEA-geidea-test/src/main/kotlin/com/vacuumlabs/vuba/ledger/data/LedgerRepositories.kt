package com.vacuumlabs.vuba.ledger.data

import org.springframework.stereotype.Component

@Component
class LedgerRepositories(
    val forAccount: AccountRepository,
    val forAsset: AssetRepository,
    val forCommit: CommitRepository,
    val forStatusEntry: StatusEntryRepository,
    val forStatus: StatusRepository,
    val forStatusType: StatusTypeRepository,
    val forSubaccountEntry: SubaccountEntryRepository,
    val forSubaccount: SubaccountRepository
)
