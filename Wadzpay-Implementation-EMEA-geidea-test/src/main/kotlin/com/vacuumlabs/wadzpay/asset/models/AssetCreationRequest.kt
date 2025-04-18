package com.vacuumlabs.wadzpay.asset.models

import com.vacuumlabs.wadzpay.asset.AssetCustomController
import com.vacuumlabs.wadzpay.user.UserAccount
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "asset_creation_request")
data class AssetCreationRequest(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val assetCreationRequestId: Long = 0,

    @Column(unique = true, nullable = false)
    var assetName: String,

    var assetUnit: String,

    var assetAmount: Long,

    var decimalPlaces: String ? = null,

    @ManyToOne
    @JoinColumn(name = "requester_id", nullable = false)
    var requesterId: UserAccount,

    @Enumerated(EnumType.STRING)
    var requestState: AssetCustomController.RequestState ? = null,

    var requestDate: Instant = Instant.now(),

    var completedState: String ? = null,

    var dateOfProvision: Instant? = null,

    var provisionAccount: String ? = null,

    @ManyToOne
    @JoinColumn(name = "provision_user_id", nullable = false)
    var provisionUserId: UserAccount ? = null,

    var provisionedTokenId: String ? = null,
    var assetType: String? = null,
    var assetCategory: String? = null,
    var assetUnitQuantity: BigDecimal? = null

)
@Repository
interface AssetCreationRequestRepository :
    PagingAndSortingRepository<AssetCreationRequest, Long>,
    JpaSpecificationExecutor<AssetCreationRequest> {
    fun getByRequestStateAndAssetName(requestState: AssetCustomController.RequestState?, assetName: String): AssetCreationRequest?
    fun getByAssetName(assetUnit: String): AssetCreationRequest?

    fun getByRequestState(requestState: AssetCustomController.RequestState?): MutableList<AssetCreationRequest>?

    fun getByRequestStateAndRequesterId(requestState: AssetCustomController.RequestState?, requesterId: UserAccount): MutableList<AssetCreationRequest>?

    fun getByRequestStateAndRequesterIdAndAssetName(requestState: AssetCustomController.RequestState?, requesterId: UserAccount, assetUnit: String): MutableList<AssetCreationRequest>?
}
