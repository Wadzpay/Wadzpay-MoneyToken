package com.vacuumlabs.wadzpay.user

import com.vacuumlabs.wadzpay.common.DuplicateEntityException
import com.vacuumlabs.wadzpay.common.EntityNotFoundException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.UnprocessableEntityException
import org.springframework.stereotype.Service

@Service
class ContactService(val contactRepository: ContactRepository) {
    fun getContacts(userAccount: UserAccount, search: String?): List<ContactViewModel?> {
        val contacts = userAccount.ownedContacts.map { it.toViewModel() }

        return if (!search.isNullOrEmpty()) {
            contacts.filter {
                it.email!!.contains(search, ignoreCase = true) ||
                    it.phoneNumber?.contains(search, ignoreCase = true) ?: false ||
                    it.nickname != null && it.nickname.toLowerCase().contains(search, ignoreCase = true)
            }
        } else {
            contacts
        }
    }

    fun addContact(userAccount: UserAccount, contactUserAccount: UserAccount, nickname: String?) {
        if (userAccount == contactUserAccount) {
            throw UnprocessableEntityException(ErrorCodes.OWN_ACCOUNT_CONTACT)
        }

        if (userAccount.ownedContacts.any { nickname != null && it.nickname == nickname && it.userAccount !== contactUserAccount }) {
            throw DuplicateEntityException(ErrorCodes.NICKNAME_ALREADY_USED)
        }

        val contact = userAccount.ownedContacts.find { it.userAccount == contactUserAccount }
        if (contact == null) {
            contactRepository.save(Contact(nickname = nickname, userAccount = contactUserAccount, owner = userAccount))
        } else {
            throw DuplicateEntityException(ErrorCodes.CONTACT_ALREADY_EXISTS)
        }
    }

    fun updateContact(userAccount: UserAccount, contactUserAccount: UserAccount, nickname: String?) {
        if (userAccount.ownedContacts.any { (nickname != null && it.nickname == nickname) && it.userAccount !== contactUserAccount }) {
            throw DuplicateEntityException(ErrorCodes.NICKNAME_ALREADY_USED)
        }

        val contact = userAccount.ownedContacts.find { it.userAccount == contactUserAccount }
        if (contact == null) {
            throw EntityNotFoundException(ErrorCodes.CONTACT_NOT_FOUND)
        } else {
            contact.nickname = nickname
            contactRepository.save(contact)
        }
    }

    fun deleteContact(userAccount: UserAccount, contactUserAccount: UserAccount, nickname: String?) {
        val contact = userAccount.ownedContacts.find { it.userAccount == contactUserAccount }
            ?: throw EntityNotFoundException(ErrorCodes.CONTACT_NOT_FOUND)

        contactRepository.delete(contact)
    }
}
