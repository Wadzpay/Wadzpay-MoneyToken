package com.vacuumlabs.wadzpay.accountowner

import com.vacuumlabs.wadzpay.auth.Role
import com.vacuumlabs.wadzpay.ledger.model.AccountOwner
import com.vacuumlabs.wadzpay.merchant.MerchantService
import com.vacuumlabs.wadzpay.user.UserAccountService
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Service

@Service
class AccountOwnerService(
    val merchantService: MerchantService,
    val userAccountService: UserAccountService
) {
    fun extractAccount(principal: Authentication, useMerchant: Boolean = false): AccountOwner {
        if (principal.authorities.contains(toAuthority(Role.MERCHANT))) {
            return merchantService.getMerchantByName(principal.name)
        } else {
            val user = userAccountService.getUserAccountByEmail(principal.name)

            return if (useMerchant &&
                principal.authorities.contains(toAuthority(Role.MERCHANT_ADMIN)) ||
                principal.authorities.contains(toAuthority(Role.MERCHANT_READER)) ||
                principal.authorities.contains(toAuthority(Role.MERCHANT_MERCHANT)) ||
                principal.authorities.contains(toAuthority(Role.MERCHANT_SUPERVISOR)) ||
                principal.authorities.contains(toAuthority(Role.MERCHANT_POSOPERATOR))
            ) {

                user.merchant!!
            } else {
                user
            }
        }
    }

    private fun toAuthority(role: Role): SimpleGrantedAuthority {
        return SimpleGrantedAuthority(role.toAuthority())
    }
}
