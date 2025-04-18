package com.vacuumlabs.wadzpay.merchant

import com.vacuumlabs.wadzpay.auth.Role
import com.vacuumlabs.wadzpay.common.EntityNotFoundException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.services.InvitedMerchants
import com.vacuumlabs.wadzpay.services.RedisService
import org.springframework.stereotype.Service

@Service
class MerchantDashboardService(
    val redisService: RedisService,
    val merchantService: MerchantService,
) {
    fun getInvitation(email: String): Invitation? {
        val invitation = redisService.getInvitation(email) ?: return null

        val merchantId = invitation.substringBeforeLast(',').toLong()
        val merchant = merchantService.merchantRepository.findById(merchantId)
            .orElseThrow { EntityNotFoundException(ErrorCodes.MERCHANT_NOT_FOUND) }
        val role = Role.valueOf(invitation.substringAfterLast(','))

        return Invitation(
            merchant.name,
            role
        )
    }

    fun getInvitations(merchantId: Long): ArrayList<InvitedMerchants> {
        return redisService.getInvitations(merchantId)
    }
}

data class Invitation(
    val merchantName: String,
    val role: Role
)
