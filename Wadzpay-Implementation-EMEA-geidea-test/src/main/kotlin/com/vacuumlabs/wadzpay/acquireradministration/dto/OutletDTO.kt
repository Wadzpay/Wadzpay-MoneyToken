package com.vacuumlabs.wadzpay.acquireradministration.dto

import com.opencsv.bean.CsvBindByName
import com.opencsv.bean.CsvBindByPosition
import com.opencsv.bean.CsvIgnore
import javax.persistence.Column
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern

data class OutletDTO(
    @field:NotBlank(message = "Aggregator  Id  must not be empty")
    @CsvBindByName(column = "Aggregator Id")
    @CsvBindByPosition(position = 0)
    var aggregatorPreferenceId: String? = null,
    @field:NotBlank(message = "Institution  Id  must not be empty")
    @CsvBindByName(column = "Institution  Id")
    @CsvBindByPosition(position = 1)
    var insitutionPreferenceId: String? = null,
    @field:NotBlank(message = "Merchant Group Id  must not be empty")
    @CsvBindByName(column = "Merchant Group Id")
    @CsvBindByPosition(position = 2)
    var merchantGroupPreferenceId: String? = null,
    @field:NotBlank(message = "Merchant  Id  must not be empty")
    @CsvBindByName(column = "Merchant Id")
    @CsvBindByPosition(position = 3)
    var merchantAcquirerPreferenceId: String? = null,
    @field:NotBlank(message = "Submerchant  Id  must not be empty")
    @CsvBindByName(column = "Merchant Group Id")
    @CsvBindByPosition(position = 4)
    var subMerchantPreferenceId: String? = null,
    @CsvIgnore
    var outletStatus: String? = null,
    @field:NotBlank(message = "Outlet  Id  must not be empty")
    @CsvBindByName(column = "Merchant Group Id")
    @CsvBindByPosition(position = 5)
    var outletId: String? = null,
    @field:NotBlank(message = "Outlet  Name  must not be empty")
    @CsvBindByName(column = "Merchant Group Id")
    @CsvBindByPosition(position = 6)
    var outletName: String? = null,
    /*@CsvBindByName(column = "Merchant Group Id")
    @CsvBindByPosition(position = 7)
    var outletStatus: String? = null,*/
    @CsvBindByName(column = "Merchant Group Logo")
    @CsvBindByPosition(position = 7)
    var outletLogo: String? = null,
    @CsvBindByName(column = "Address Line1")
    @CsvBindByPosition(position = 8)
    @field:NotBlank(message = "Address  Line1  must not be empty")
    var entityAddressAddressLine1: String? = null,
    @CsvBindByName(column = "Address Line2")
    @CsvBindByPosition(position = 9)
    var entityAddressAddressLine2: String? = null,
    @CsvBindByName(column = "Address Line3")
    @CsvBindByPosition(position = 10)
    var entityAddressAddressLine3: String? = null,
    @CsvBindByPosition(position = 11)
    @CsvBindByName(column = "City")
    @field:NotBlank(message = "City should not be empty")
    var entityAddressCity: String? = null,
    @CsvBindByPosition(position = 12)
    @CsvBindByName(column = "State")
    @field:NotBlank(message = "State should not be empty")
    var entityAddressState: String? = null,
    @CsvBindByPosition(position = 13)
    @CsvBindByName(column = "Country")
    @field:NotBlank(message = "Country should not be empty")
    var entityAddressCountry: String? = null,
    @CsvBindByPosition(position = 14)
    @CsvBindByName(column = "Postal Code")
    @field:NotBlank(message = "Postal code should not be empty")
    var entityAddressPostalCode: String? = null,
    @CsvBindByPosition(position = 15)
    @CsvBindByName(column = "Admin  FirstName")
    @field:NotBlank(message = " Admin First Name  should not be empty")
    var entityAdminDetailsFirstName: String? = null,
    @CsvBindByName(column = "Admin  MiddleName")
    @CsvBindByPosition(position = 16)
    var entityAdminDetailsMiddleName: String? = null,
    @CsvBindByName(column = "Admin  LastName")
    @field:NotBlank(message = "Last Name  should not be empty")
    @CsvBindByPosition(position = 17)
    var entityAdminDetailsLastName: String? = null,
    @CsvBindByPosition(position = 18)
    @CsvBindByName(column = "Admin  EmailId")
    @field:Email(message = "Email is not valid")
    var entityAdminDetailsEmailId: String? = null,
    @CsvBindByName(column = "Admin  MobileNumber")
    @CsvBindByPosition(position = 19)
    @field:Pattern(regexp = "^(\\d{10}|\\d{12}|\\d{11})\$", message = "Mobile number is  not valid")
    var entityAdminDetailsMobileNumber: String? = null,
    @CsvBindByName(column = "Admin  Department")
    @CsvBindByPosition(position = 20)
    var entityAdminDetailsDepartment: String? = null,
    @CsvBindByName(column = "Bank Name")
    @CsvBindByPosition(position = 21)
    var entityBankDetailsBankName: String? = null,
    @CsvBindByName(column = "Bank Account Number")
    @CsvBindByPosition(position = 22)
    @field:Pattern(regexp = "^[A-Za-z0-9]*$", message = "Account number must  be aplhanumeric only")
    var entityBankDetailsBankAccountNumber: String? = null,
    @CsvBindByName(column = "Bank Holder Name")
    @CsvBindByPosition(position = 23)
    var entityBankDetailsBankHolderName: String? = null,
    @CsvBindByName(column = "Bank Branch Code")
    @CsvBindByPosition(position = 24)
    var entityBankDetailsBranchCode: String? = null,
    @CsvBindByName(column = "Bank Branch Location")
    @CsvBindByPosition(position = 25)
    var entityBankDetailsBranchLocation: String? = null,
    @CsvBindByName(column = "ContactDetails FirstName")
    @CsvBindByPosition(position = 26)
    @field:NotBlank(message = "First Name  should not be empty")
    var entityContactDetailsFirstName: String? = null,
    @CsvBindByPosition(position = 27)
    @CsvBindByName(column = "ContactDetails MiddleName")
    var entityContactDetailsMiddleName: String? = null,
    @CsvBindByName(column = "ContactDetails LastName")
    @field:NotBlank(message = "Last Name  should not be empty")
    @CsvBindByPosition(position = 28)
    var entityContactDetailsLastName: String? = null,
    @CsvBindByName(column = "ContactDetails EmailId")
    @field:Email(message = "Email  is not valid")
    @CsvBindByPosition(position = 29)
    var entityContactDetailsEmailId: String? = null,
    @CsvBindByName(column = "ContactDetails MobileNumber")
    @CsvBindByPosition(position = 30)
    @field:Pattern(regexp = "^(\\d{10}|\\d{12}|\\d{11})\$", message = "Mobile number is  not valid")
    var entityContactDetailsMobileNumber: String? = null,
    @CsvBindByName(column = "ContactDetails Designation")
    @CsvBindByPosition(position = 31)
    var entityContactDetailsDesignation: String? = null,
    @CsvBindByName(column = "ContactDetails Department")
    @CsvBindByPosition(position = 32)
    var entityContactDetailsDepartment: String? = null,
    @CsvBindByName(column = "Abbrevation")
    @CsvBindByPosition(position = 33)
    var entityInfoAbbrevation: String? = null,
    @CsvBindByName(column = "Description")
    @CsvBindByPosition(position = 34)
    var entityInfoDescription: String? = null,
    @CsvBindByName(column = "Logo")
    @CsvBindByPosition(position = 35)
    var entityInfoLogo: String? = null,
    @CsvBindByName(column = "Region")
    @CsvBindByPosition(position = 36)
    var entityInfoRegion: String? = null,
    @CsvBindByName(column = "Timezone")
    @CsvBindByPosition(position = 37)
    @field:Pattern(regexp = "^[A-Za-z]+/[A-Za-z_]+\$")
    var entityInfoTimezone: String? = null,
    @CsvBindByName(column = "Outlet Type")
    @CsvBindByPosition(position = 38)
    var entityInfoType: String? = null,
    @CsvBindByPosition(position = 39)
    @CsvBindByName(column = "Default Digital Currency")
    var entityInfoDefaultDigitalCurrency: String? = null,
    @CsvBindByName(column = "Base Fiat Currency")
    @CsvBindByPosition(position = 40)

    var entityInfoBaseFiatCurrency: String? = null,
    @CsvBindByPosition(position = 41)
    @CsvBindByName(column = "Customer Offline Txn")
    @Column(nullable = true, unique = true)
    var entityOthersCustomerOfflineTxn: String? = null,
    @CsvBindByName(column = "Merchant Offline Txn")
    @CsvBindByPosition(position = 42)

    var entityOthersMerchantOfflineTxn: String? = null,
    @CsvBindByName(column = "Approval WorkFlow")
    @CsvBindByPosition(position = 43)

    var entityOthersApprovalWorkFlow: String? = null,
    @CsvBindByName(column = "Activation Date")
    @CsvBindByPosition(position = 44)

    var entityOthersActivationDate: String? = null
) {
    @CsvBindByPosition(position = 45)
    @CsvBindByName(column = "Error")

    var error: String? = null
}
