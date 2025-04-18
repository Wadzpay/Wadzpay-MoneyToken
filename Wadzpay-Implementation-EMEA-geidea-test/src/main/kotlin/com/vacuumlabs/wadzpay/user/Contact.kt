package com.vacuumlabs.wadzpay.user

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne

@Entity
data class Contact(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    val id: Long = 0,

    var nickname: String?,

    @JsonIgnore
    @ManyToOne
    /*reference to the userAccount which nickname refers*/
    val userAccount: UserAccount,

    @JsonIgnore
    @ManyToOne
    /*userAccount reference which has this contact entity in the address book*/
    val owner: UserAccount
)

data class ContactViewModel(
    val nickname: String?,
    val phoneNumber: String?,
    val email: String?,
    val cognitoUsername: String
)

fun Contact.toViewModel(): ContactViewModel {
    return ContactViewModel(nickname, userAccount.phoneNumber, userAccount.customerId ?: userAccount.email, userAccount.cognitoUsername)
}

@Repository
interface ContactRepository : CrudRepository<Contact, Long>
