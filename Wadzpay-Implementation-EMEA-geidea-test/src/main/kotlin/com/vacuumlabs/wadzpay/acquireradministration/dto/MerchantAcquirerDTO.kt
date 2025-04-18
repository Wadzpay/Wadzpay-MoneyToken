package com.vacuumlabs.wadzpay.acquireradministration.dto
import com.fasterxml.jackson.annotation.JsonInclude
import com.opencsv.bean.CsvBindByName
import com.opencsv.bean.CsvBindByPosition
import com.opencsv.bean.CsvIgnore
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern

data class MerchantAcquirerDTO(
    @CsvBindByName(column = "Aggregator Id")
    @CsvBindByPosition(position = 0)
    var aggregatorPreferenceId: String? = null,
    @field:NotBlank(message = "Institution  Id  must not be empty")
    @CsvBindByName(column = "Institution  Id")
    @CsvBindByPosition(position = 1)
    var insitutionPreferenceId: String? = null,
    @CsvIgnore
    var merchantStatus: String? = null,
    @field:NotBlank(message = "Merchant Group Id  must not be empty")
    @CsvBindByName(column = "Merchant Group Id")
    @CsvBindByPosition(position = 2)
    var merchantGroupPreferenceId: String? = null,
    @field:NotBlank(message = "Merchant Id must not be empty")
    @CsvBindByPosition(position = 3)
    @CsvBindByName(column = "Merchant Id")
    var merchantAcquirerId: String? = null,
    @CsvBindByPosition(position = 4)
    @CsvBindByName(column = "Merchant Name")
    @field:NotBlank(message = "Merchant Name must not be empty")
    var merchantAcquirerName: String? = null,
    @CsvBindByPosition(position = 5)
    var merchantAcquirerLogo: String? = null,
    @CsvBindByName(column = "Address Line1")
    @CsvBindByPosition(position = 6)
    @field:NotBlank(message = "Address  Line1  must not be empty")
    var entityAddressAddressLine1: String? = null,
    @CsvBindByName(column = "Address Line2")
    @CsvBindByPosition(position = 7)
    var entityAddressAddressLine2: String? = null,
    @CsvBindByName(column = "Address Line3")
    @CsvBindByPosition(position = 8)
    var entityAddressAddressLine3: String? = null,
    @CsvBindByPosition(position = 9)
    @CsvBindByName(column = "City")
    @field:NotBlank(message = "City should not be empty")
    var entityAddressCity: String? = null,
    @CsvBindByPosition(position = 10)
    @CsvBindByName(column = "State")
    @field:NotBlank(message = "State should not be empty")
    var entityAddressState: String? = null,
    @CsvBindByPosition(position = 11)
    @CsvBindByName(column = "Country")
    @field:NotBlank(message = "Country should not be empty")
    var entityAddressCountry: String? = null,
    @CsvBindByPosition(position = 12)
    @CsvBindByName(column = "Postal Code")
    @field:NotBlank(message = "Postal code should not be empty")
    var entityAddressPostalCode: String? = null,
    @CsvBindByPosition(position = 13)
    @CsvBindByName(column = "Admin  FirstName")
    @field:NotBlank(message = " Admin First Name  should not be empty")
    var entityAdminDetailsFirstName: String? = null,
    @CsvBindByName(column = "Admin  MiddleName")
    @CsvBindByPosition(position = 14)
    var entityAdminDetailsMiddleName: String? = null,
    @CsvBindByName(column = "Admin  LastName")
    @field:NotBlank(message = "Last Name  should not be empty")
    @CsvBindByPosition(position = 15)
    var entityAdminDetailsLastName: String? = null,
    @CsvBindByPosition(position = 16)
    @CsvBindByName(column = "Admin  EmailId")
    @field:Email(message = "Email is not valid")
    var entityAdminDetailsEmailId: String? = null,
    @CsvBindByName(column = "Admin  MobileNumber")
    @CsvBindByPosition(position = 17)
    @field:Pattern(regexp = "^(\\d{10}|\\d{12}|\\d{11})\$", message = "Mobile number is  not valid")
    var entityAdminDetailsMobileNumber: String? = null,
    @CsvBindByName(column = "Admin  Department")
    @CsvBindByPosition(position = 18)
    var entityAdminDetailsDepartment: String? = null,
    @CsvBindByName(column = "Bank Name")
    @CsvBindByPosition(position = 19)
    var entityBankDetailsBankName: String? = null,
    @CsvBindByName(column = "Bank Account Number")
    @CsvBindByPosition(position = 20)
    @field:Pattern(regexp = "^[A-Za-z0-9]*$", message = "Account number must  be aplhanumeric only")
    var entityBankDetailsBankAccountNumber: String? = null,
    @CsvBindByName(column = "Bank Holder Name")
    @CsvBindByPosition(position = 21)
    var entityBankDetailsBankHolderName: String? = null,
    @CsvBindByName(column = "Bank Branch Code")
    @CsvBindByPosition(position = 22)
    var entityBankDetailsBranchCode: String? = null,
    @CsvBindByName(column = "Bank Branch Location")
    @CsvBindByPosition(position = 23)
    var entityBankDetailsBranchLocation: String? = null,
    @CsvBindByName(column = "ContactDetails FirstName")
    @CsvBindByPosition(position = 24)
    @field:NotBlank(message = "First Name  should not be empty")
    var entityContactDetailsFirstName: String? = null,
    @CsvBindByPosition(position = 25)
    @CsvBindByName(column = "ContactDetails MiddleName")
    var entityContactDetailsMiddleName: String? = null,
    @CsvBindByName(column = "ContactDetails LastName")
    @field:NotBlank(message = "Last Name  should not be empty")
    @CsvBindByPosition(position = 26)
    var entityContactDetailsLastName: String? = null,
    @CsvBindByName(column = "ContactDetails EmailId")
    @field:Email(message = "Email  is not valid")
    @CsvBindByPosition(position = 27)
    var entityContactDetailsEmailId: String? = null,
    @CsvBindByName(column = "ContactDetails MobileNumber")
    @CsvBindByPosition(position = 28)
    @field:Pattern(regexp = "^(\\d{10}|\\d{12}|\\d{11})\$", message = "Mobile number is  not valid")
    var entityContactDetailsMobileNumber: String? = null,
    @CsvBindByName(column = "ContactDetails Designation")
    @CsvBindByPosition(position = 29)
    var entityContactDetailsDesignation: String? = null,
    @CsvBindByName(column = "ContactDetails Department")
    @CsvBindByPosition(position = 30)
    var entityContactDetailsDepartment: String? = null,
    @CsvBindByName(column = "Abbrevation")
    @CsvBindByPosition(position = 31)
    var entityInfoAbbrevation: String? = null,
    @CsvBindByName(column = "Description")
    @CsvBindByPosition(position = 32)
    var entityInfoDescription: String? = null,
    @CsvBindByName(column = "Logo")
    @CsvBindByPosition(position = 33)
    var entityInfoLogo: String? = null,
    @CsvBindByName(column = "Region")
    @CsvBindByPosition(position = 34)
    var entityInfoRegion: String? = null,
    @CsvBindByName(column = "Timezone")
    @CsvBindByPosition(position = 35)
    @field:Pattern(regexp = "^[A-Za-z]+/[A-Za-z_]+\$")
    var entityInfoTimezone: String? = null,
    @CsvBindByName(column = "Outlet Type")
    @CsvBindByPosition(position = 36)
    var entityInfoType: String? = null,
    @CsvBindByPosition(position = 37)
    @CsvBindByName(column = "Default Digital Currency")
    var entityInfoDefaultDigitalCurrency: String? = null,
    @CsvBindByName(column = "Base Fiat Currency")
    @CsvBindByPosition(position = 38)
    var entityInfoBaseFiatCurrency: String? = null,
    @CsvBindByPosition(position = 39)
    @CsvBindByName(column = "Customer Offline Txn")
    var entityOthersCustomerOfflineTxn: String? = null,
    @CsvBindByName(column = "Merchant Offline Txn")
    @CsvBindByPosition(position = 40)
    var entityOthersMerchantOfflineTxn: String? = null,
    @CsvBindByName(column = "Approval WorkFlow")
    @CsvBindByPosition(position = 41)
    var entityOthersApprovalWorkFlow: String? = null,
    @CsvBindByName(column = "Activation Date")
    @CsvBindByPosition(position = 42)
    var entityOthersActivationDate: String? = null
) {
    @CsvBindByPosition(position = 43)
    @CsvBindByName(column = "Error")
    var error: String? = null
    @JsonInclude()
    @Transient
    @CsvBindByName(column = "type")
    var type: String? = null
}
