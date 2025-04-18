package com.vacuumlabs.wadzpay.acquireradministration.dto

import com.opencsv.bean.CsvBindByName
import com.opencsv.bean.CsvBindByPosition
import com.opencsv.bean.CsvIgnore
import javax.persistence.Column
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern

data class SubMerchantAcquirerDTO(

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
    @CsvIgnore
    var subMerchantStatus: String? = null,
    @field:NotBlank(message = "Submerchant  Id  must not be empty")
    @CsvBindByName(column = "Submerchant Id")
    @CsvBindByPosition(position = 4)
    var subMerchantAcquirerId: String? = null,
    @CsvBindByName(column = "Submerchant Name")
    @field:NotBlank(message = "Submerchant   Name must not be empty")
    @CsvBindByPosition(position = 5)
    var subMerchantAcquirerName: String? = null,
   /* @Column(nullable = true, unique = true)
    @CsvBindByName(column = "Status")
    @CsvBindByPosition(position = 6)
    var subMerchantAcquirerStatus: String? = null,*/
    @Column(nullable = true, unique = true)
    @CsvBindByName(column = "Submerchant Logo")
    @CsvBindByPosition(position = 6)
    var subMerchantAcquirerLogo: String? = null,
    @CsvBindByName(column = "Address Line1")
    @CsvBindByPosition(position = 7)
    @field:NotBlank(message = "Address  Line1  must not be empty")
    var entityAddressAddressLine1: String? = null,
    @CsvBindByName(column = "Address Line2")
    @CsvBindByPosition(position = 8)
    var entityAddressAddressLine2: String? = null,
    @CsvBindByName(column = "Address Line3")
    @CsvBindByPosition(position = 9)
    var entityAddressAddressLine3: String? = null,
    @CsvBindByPosition(position = 10)
    @CsvBindByName(column = "City")
    @field:NotBlank(message = "City should not be empty")
    var entityAddressCity: String? = null,
    @CsvBindByPosition(position = 11)
    @CsvBindByName(column = "State")
    @field:NotBlank(message = "State should not be empty")
    var entityAddressState: String? = null,
    @CsvBindByPosition(position = 12)
    @CsvBindByName(column = "Country")
    @field:NotBlank(message = "Country should not be empty")
    var entityAddressCountry: String? = null,
    @CsvBindByPosition(position = 13)
    @CsvBindByName(column = "Postal Code")
    @field:NotBlank(message = "Postal code should not be empty")
    var entityAddressPostalCode: String? = null,
    @CsvBindByPosition(position = 14)
    @CsvBindByName(column = "Admin  FirstName")
    @field:NotBlank(message = " Admin First Name  should not be empty")
    var entityAdminDetailsFirstName: String? = null,
    @CsvBindByName(column = "Admin  MiddleName")
    @CsvBindByPosition(position = 15)
    var entityAdminDetailsMiddleName: String? = null,
    @CsvBindByName(column = "Admin  LastName")
    @field:NotBlank(message = "Last Name  should not be empty")
    @CsvBindByPosition(position = 16)
    var entityAdminDetailsLastName: String? = null,
    @CsvBindByPosition(position = 17)
    @CsvBindByName(column = "Admin  EmailId")
    @field:Email(message = "Email is not valid")
    var entityAdminDetailsEmailId: String? = null,
    @CsvBindByName(column = "Admin  MobileNumber")
    @CsvBindByPosition(position = 18)
    @field:Pattern(regexp = "^(\\d{10}|\\d{12}|\\d{11})\$", message = "Mobile number is  not valid")
    var entityAdminDetailsMobileNumber: String? = null,
    @CsvBindByName(column = "Admin  Department")
    @CsvBindByPosition(position = 19)
    var entityAdminDetailsDepartment: String? = null,
    @CsvBindByName(column = "Bank Name")
    @CsvBindByPosition(position = 20)
    var entityBankDetailsBankName: String? = null,
    @CsvBindByName(column = "Bank Account Number")
    @CsvBindByPosition(position = 21)
    @field:Pattern(regexp = "^[A-Za-z0-9]*$", message = "Account number must  be aplhanumeric only")
    var entityBankDetailsBankAccountNumber: String? = null,
    @CsvBindByName(column = "Bank Holder Name")
    @CsvBindByPosition(position = 22)
    var entityBankDetailsBankHolderName: String? = null,
    @CsvBindByName(column = "Bank Branch Code")
    @CsvBindByPosition(position = 23)
    var entityBankDetailsBranchCode: String? = null,
    @CsvBindByName(column = "Bank Branch Location")
    @CsvBindByPosition(position = 24)
    var entityBankDetailsBranchLocation: String? = null,
    @CsvBindByName(column = "ContactDetails FirstName")
    @CsvBindByPosition(position = 25)
    @field:NotBlank(message = "First Name  should not be empty")
    var entityContactDetailsFirstName: String? = null,
    @CsvBindByPosition(position = 26)
    @CsvBindByName(column = "ContactDetails MiddleName")
    var entityContactDetailsMiddleName: String? = null,
    @CsvBindByName(column = "ContactDetails LastName")
    @field:NotBlank(message = "Last Name  should not be empty")
    @CsvBindByPosition(position = 27)
    var entityContactDetailsLastName: String? = null,
    @CsvBindByName(column = "ContactDetails EmailId")
    @field:Email(message = "Email  is not valid")
    @CsvBindByPosition(position = 28)
    var entityContactDetailsEmailId: String? = null,
    @CsvBindByName(column = "ContactDetails MobileNumber")
    @CsvBindByPosition(position = 29)
    @field:Pattern(regexp = "^(\\d{10}|\\d{12}|\\d{11})\$", message = "Mobile number is  not valid")
    var entityContactDetailsMobileNumber: String? = null,
    @CsvBindByName(column = "ContactDetails Designation")
    @CsvBindByPosition(position = 30)
    var entityContactDetailsDesignation: String? = null,
    @CsvBindByName(column = "ContactDetails Department")
    @CsvBindByPosition(position = 31)
    var entityContactDetailsDepartment: String? = null,
    @CsvBindByName(column = "Abbrevation")
    @CsvBindByPosition(position = 32)
    var entityInfoAbbrevation: String? = null,
    @CsvBindByName(column = "Description")
    @CsvBindByPosition(position = 33)
    var entityInfoDescription: String? = null,
    @CsvBindByName(column = "Logo")
    @CsvBindByPosition(position = 34)
    var entityInfoLogo: String? = null,
    @CsvBindByName(column = "Region")
    @CsvBindByPosition(position = 35)
    var entityInfoRegion: String? = null,
    @CsvBindByName(column = "Timezone")
    @CsvBindByPosition(position = 36)
    @field:Pattern(regexp = "^[A-Za-z]+/[A-Za-z_]+\$")
    var entityInfoTimezone: String? = null,
    @CsvBindByName(column = "Outlet Type")
    @CsvBindByPosition(position = 37)
    var entityInfoType: String? = null,
    @CsvBindByPosition(position = 38)
    @CsvBindByName(column = "Default Digital Currency")
    var entityInfoDefaultDigitalCurrency: String? = null,
    @CsvBindByName(column = "Base Fiat Currency")
    @CsvBindByPosition(position = 39)

    var entityInfoBaseFiatCurrency: String? = null,
    @CsvBindByPosition(position = 40)
    @CsvBindByName(column = "Customer Offline Txn")
    var entityOthersCustomerOfflineTxn: String? = null,
    @CsvBindByName(column = "Merchant Offline Txn")
    @CsvBindByPosition(position = 41)
    var entityOthersMerchantOfflineTxn: String? = null,
    @CsvBindByName(column = "Approval WorkFlow")
    @CsvBindByPosition(position = 42)
    var entityOthersApprovalWorkFlow: String? = null,
    @CsvBindByName(column = "Activation Date")
    @CsvBindByPosition(position = 43)
    var entityOthersActivationDate: String? = null
) {
    @CsvBindByPosition(position = 44)
    @CsvBindByName(column = "Error")
    var error: String? = null
}
