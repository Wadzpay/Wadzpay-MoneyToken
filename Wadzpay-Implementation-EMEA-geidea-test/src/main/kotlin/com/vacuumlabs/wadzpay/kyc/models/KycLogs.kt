package com.vacuumlabs.wadzpay.kyc.models

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.vacuumlabs.wadzpay.user.UserAccount
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
data class KycLogs(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    val id: Long = 0,
    @Column
    val call_back_type: String?,
    @Column
    val
    callback_date: String?,
    @Column
    val
    client_ip: String?,
    @Column
    val
    customer_id: String?,
    @Column
    val
    first_attempt_date: String?,
    @Column
    val id_first_name: String?,
    @Column
    val id_dob: String?,
    @Column
    val id_country: String?,
    @Column
    val id_last_name: String?,
    @Column
    val id_number: String?,
    @Column
    val id_scan_image: String?,
    @Column
    val id_scan_image_backside: String?,
    @Column
    val
    id_scan_image_face: String?,
    @Column
    val id_scan_source: String?,
    @Column
    val idScanStatus: String?,
    @Column
    val id_subtype: String?,
    @Column
    val identity_verification: String?,
    @Column
    val jumio_id_scan_reference: String?,
    @Column
    val merchant_id_scan_reference: String?,
    @Column
    val
    personal_number: String?,
    @Column
    val transaction_date: String?,
    @Column
    @Enumerated(EnumType.STRING)
    val verificationStatus: VerificationStatus,
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_account_id", nullable = true)
    var userAccount: UserAccount? = null
)

@Repository
interface KycLogRepository : CrudRepository<KycLogs, Long> {
    fun findByIdScanStatus(string: String?): List<KycLogs>?
}

enum class VerificationStatus {
    PENDING, IMAGE_MISMATCH, APPROVED_VERIFIED, IN_PROGRESS, NAME_MISMATCH, DENIED_FRAUD, DENIED_UNSUPPORTED_ID_TYPE, DENIED_UNSUPPORTED_ID_COUNTRY, ERROR_NOT_READABLE_ID, NO_ID_UPLOADED, UNKNOWN, NULL;
}
