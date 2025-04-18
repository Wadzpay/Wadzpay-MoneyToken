package com.vacuumlabs.wadzpay.acquireradministration.dto

import com.opencsv.bean.CsvBindByName
import com.opencsv.bean.CsvBindByPosition
import com.opencsv.bean.CsvIgnore
import javax.persistence.Column
import javax.validation.constraints.AssertTrue
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern

data class AggregatorDTO(
    @field:NotBlank(message = "type must not be empty")
    @CsvBindByPosition(position = 0)
    var type: String? = null,
    @CsvBindByName(column = "Aggregator Id")
    @field:NotBlank(message = "Aggregator Id must not be empty")
    @CsvBindByPosition(position = 1)
    var aggregatorPreferenceId: String? = null,
    @CsvBindByName(column = "Aggregator Name")
    @CsvBindByPosition(position = 2)
/*
    @field:NotBlank(message = "Aggregator Name must not be empty")
*/
    var aggregatorName: String? = null,
    @CsvIgnore
    var aggregatorStatus: String? = null,
    @Column(nullable = true, unique = true)
    @CsvBindByPosition(position = 3)
    var aggregatorLogo: String? = null,
    @CsvBindByPosition(position = 4)
    @CsvBindByName(column = "Institution Id")
    var institutionId: String? = null,
    @CsvIgnore
    var institutionStatus: String? = null,
    @CsvBindByName(column = "Client Institution Id")
    @CsvBindByPosition(position = 5)
    var insitutionPreferenceId: String? = null,
    @CsvBindByPosition(position = 6)
    @CsvBindByName(column = "Institution Name")
    var insitutionName: String? = null,
    @CsvBindByPosition(position = 7)
    @CsvBindByName(column = "Institution Logo")
    var institutionLogo: String? = null,
    @CsvBindByPosition(position = 8)
    @CsvBindByName(column = "MerchantGroup Id")
    var merchantGroupPreferenceId: String? = null,
    @CsvBindByPosition(position = 9)
    @CsvBindByName(column = "MerchantGroup Name")
    var merchantGroupName: String? = null,
    @CsvIgnore
    var merchantGroupStatus: String? = null,
    @CsvBindByPosition(position = 10)
    @CsvBindByName(column = "MerchantGroup Logo")
    var merchantGroupLogo: String? = null,
    @CsvBindByPosition(position = 11)
    @CsvBindByName(column = "MerchantAcquirer Id")
    var merchantAcquirerId: String? = null,
    @CsvBindByPosition(position = 12)
    @CsvBindByName(column = "MerchantAcquirer Name")
    var merchantAcquirerName: String? = null,
    @CsvIgnore
    var merchantAcquirerStatus: String? = null,
    @CsvBindByPosition(position = 13)
    @CsvBindByName(column = "MerchantAcquirer Logo")
    var merchantAcquirerLogo: String? = null,
    @CsvBindByPosition(position = 14)
    @CsvBindByName(column = "Submerchant Id")
    var subMerchantAcquirerId: String? = null,
    @CsvBindByPosition(position = 15)
    @CsvBindByName(column = "Submerchant Name")
    var subMerchantAcquirerName: String? = null,
    @CsvIgnore
    var subMerchantAcquirerStatus: String? = null,
    @CsvBindByPosition(position = 16)
    @CsvBindByName(column = "Submerchant Logo")
    var subMerchantAcquirerLogo: String? = null,
    @CsvBindByPosition(position = 17)
    var outletId: String? = null,
    @CsvBindByName(column = "Outlet  Name")
    @CsvBindByPosition(position = 18)
    var outletName: String? = null,
    @CsvIgnore
    var outletStatus: String? = null,
    @CsvBindByPosition(position = 19)
    @CsvBindByName(column = "Outlet Logo")
    var outletLogo: String? = null,
    @CsvBindByName(column = "Address Line1")
    @CsvBindByPosition(position = 20)
    @field:NotBlank(message = "Address  Line1  must not be empty")
    var entityAddressAddressLine1: String? = null,
    @CsvBindByName(column = "Address Line2")
    @CsvBindByPosition(position = 21)
    var entityAddressAddressLine2: String? = null,
    @CsvBindByName(column = "Address Line3")
    @CsvBindByPosition(position = 22)
    var entityAddressAddressLine3: String? = null,
    @CsvBindByPosition(position = 23)
    @CsvBindByName(column = "City")
    @field:NotBlank(message = "City should not be empty")
    var entityAddressCity: String? = null,
    @CsvBindByPosition(position = 24)
    @CsvBindByName(column = "State")
    @field:NotBlank(message = "State should not be empty")
    var entityAddressState: String? = null,
    @CsvBindByPosition(position = 25)
    @CsvBindByName(column = "Country")
    @field:NotBlank(message = "Country should not be empty")
    var entityAddressCountry: String? = null,
    @CsvBindByPosition(position = 26)
    @CsvBindByName(column = "Postal Code")
    @field:NotBlank(message = "Postal code should not be empty")
    var entityAddressPostalCode: String? = null,
    @CsvBindByPosition(position = 27)
    @CsvBindByName(column = "Admin  FirstName")
    @field:NotBlank(message = " Admin First Name  should not be empty")
    var entityAdminDetailsFirstName: String? = null,
    @CsvBindByName(column = "Admin  MiddleName")
    @CsvBindByPosition(position = 28)
    var entityAdminDetailsMiddleName: String? = null,
    @CsvBindByName(column = "Admin  LastName")
    @field:NotBlank(message = "Last Name  should not be empty")
    @CsvBindByPosition(position = 29)
    var entityAdminDetailsLastName: String? = null,
    @CsvBindByPosition(position = 30)
    @CsvBindByName(column = "Admin  EmailId")
    @field:Email(message = "Email is not valid")
    var entityAdminDetailsEmailId: String? = null,
    @CsvBindByName(column = "Admin  MobileNumber")
    @CsvBindByPosition(position = 31)
    @field:Pattern(regexp = "^(\\d{10}|\\d{12}|\\d{11})\$", message = "Mobile number is  not valid")
    var entityAdminDetailsMobileNumber: String? = null,
    @CsvBindByName(column = "Admin  Department")
    @CsvBindByPosition(position = 32)
    var entityAdminDetailsDepartment: String? = null,
    @CsvBindByName(column = "Bank Name")
    @CsvBindByPosition(position = 33)
    var entityBankDetailsBankName: String? = null,
    @CsvBindByName(column = "Bank Account Number")
    @CsvBindByPosition(position = 34)
    @field:Pattern(regexp = "^[A-Za-z0-9]*$", message = "Account number must  be aplhanumeric only")
    var entityBankDetailsBankAccountNumber: String? = null,
    @CsvBindByName(column = "Bank Holder Name")
    @CsvBindByPosition(position = 35)
    var entityBankDetailsBankHolderName: String? = null,
    @CsvBindByName(column = "Bank Branch Code")
    @CsvBindByPosition(position = 36)
    var entityBankDetailsBranchCode: String? = null,
    @CsvBindByName(column = "Bank Branch Location")
    @CsvBindByPosition(position = 37)
    var entityBankDetailsBranchLocation: String? = null,
    @CsvBindByName(column = "ContactDetails FirstName")
    @CsvBindByPosition(position = 38)
    @field:NotBlank(message = "First Name  should not be empty")
    var entityContactDetailsFirstName: String? = null,
    @CsvBindByPosition(position = 39)
    @CsvBindByName(column = "ContactDetails MiddleName")
    var entityContactDetailsMiddleName: String? = null,
    @CsvBindByName(column = "ContactDetails LastName")
    @field:NotBlank(message = "Last Name  should not be empty")
    @CsvBindByPosition(position = 40)
    var entityContactDetailsLastName: String? = null,
    @CsvBindByName(column = "ContactDetails EmailId")
    @field:Email(message = "Email  is not valid")
    @CsvBindByPosition(position = 41)
    var entityContactDetailsEmailId: String? = null,
    @CsvBindByName(column = "ContactDetails MobileNumber")
    @CsvBindByPosition(position = 42)
    @field:Pattern(regexp = "^(\\d{10}|\\d{12}|\\d{11})\$", message = "Mobile number is  not valid")
    var entityContactDetailsMobileNumber: String? = null,
    @CsvBindByName(column = "ContactDetails Designation")
    @CsvBindByPosition(position = 43)
    var entityContactDetailsDesignation: String? = null,
    @CsvBindByName(column = "ContactDetails Department")
    @CsvBindByPosition(position = 44)
    var entityContactDetailsDepartment: String? = null,
    @CsvBindByName(column = "Abbrevation")
    @CsvBindByPosition(position = 45)
    var entityInfoAbbrevation: String? = null,
    @CsvBindByName(column = "Description")
    @CsvBindByPosition(position = 46)
    var entityInfoDescription: String? = null,
    @CsvBindByName(column = "Logo")
    @CsvBindByPosition(position = 47)
    var entityInfoLogo: String? = null,
    @CsvBindByName(column = "Region")
    @CsvBindByPosition(position = 48)
    var entityInfoRegion: String? = null,
    @CsvBindByName(column = "Timezone")
    @CsvBindByPosition(position = 49)
    @field:Pattern(regexp = "^[A-Za-z]+/[A-Za-z_]+\$")
    var entityInfoTimezone: String? = null,
    @CsvBindByName(column = "Outlet Type")
    @CsvBindByPosition(position = 50)
    var entityInfoType: String? = null,
    @CsvBindByPosition(position = 51)
    @CsvBindByName(column = "Default Digital Currency")
    var entityInfoDefaultDigitalCurrency: String? = null,
    @CsvBindByName(column = "Base Fiat Currency")
    @CsvBindByPosition(position = 52)
    var entityInfoBaseFiatCurrency: String? = null,
    @CsvBindByPosition(position = 53)
    @CsvBindByName(column = "Customer Offline Txn")
    var entityOthersCustomerOfflineTxn: String? = null,
    @CsvBindByName(column = "Merchant Offline Txn")
    @CsvBindByPosition(position = 54)
    var entityOthersMerchantOfflineTxn: String? = null,
    @CsvBindByName(column = "Approval WorkFlow")
    @CsvBindByPosition(position = 55)
    var entityOthersApprovalWorkFlow: String? = null,
    @CsvBindByName(column = "Activation Date")
    @CsvBindByPosition(position = 56)
    var entityOthersActivationDate: String? = null
) {
    @CsvBindByPosition(position = 57)
    @CsvBindByName(column = "Error")
    var error: String? = null

    private fun isValidAggregatorName(): @AssertTrue(message = "Field Aggregator name not valid") Boolean {
        // return pass == null && passVerify == null || pass.equals(passVerify);
        // Since Java 7:
        if (type?.trim()?.toLowerCase() == "aggregator" && aggregatorName != null) {
            return aggregatorName!!.isNotBlank()
        }
        return false
    }
    private fun isValidInstituteName(): @AssertTrue(message = "Field Institution name not valid") Boolean {
        if (type?.trim()?.toLowerCase() == "institution" && insitutionName != null) {
            return insitutionName!!.isNotBlank()
        }
        return false
    }
    private fun isValidInsitutionPreferenceId(): @AssertTrue(message = "Field Institution name not valid") Boolean {
        if (type?.trim()?.toLowerCase() == "institution" && insitutionPreferenceId != null) {
            return insitutionPreferenceId!!.isNotBlank()
        }
        return false
    }
    private fun isValidMerchantGroupName(): @AssertTrue(message = "Field Merchant group name not valid") Boolean {
        if (type?.trim()?.toLowerCase() == "merchantgroup" && merchantGroupName != null) {
            return merchantAcquirerName!!.isNotBlank()
        }
        return false
    }
    private fun isValidMerchantGroupId(): @AssertTrue(message = "Field Merchantgroup name not valid") Boolean {
        if (type?.trim()?.toLowerCase() == "merchantgroup" && merchantGroupPreferenceId != null) {
            return merchantGroupPreferenceId!!.isNotBlank()
        }
        return false
    }
    private fun isValidMerchantName(): @AssertTrue(message = "Field Merchant name not valid") Boolean {
        if (type?.trim()?.toLowerCase() == "merchant" && merchantAcquirerName != null) {
            return merchantAcquirerName!!.isNotBlank()
        }
        return false
    }
    private fun isValidMerchantId(): @AssertTrue(message = "Field Merchnat name not valid") Boolean {
        if (type?.trim()?.toLowerCase() == "institution" && merchantAcquirerId != null) {
            return merchantAcquirerId!!.isNotBlank()
        }
        return false
    }
    private fun isValidsubMerchantAcquirerName(): @AssertTrue(message = "Field subMerchantAcquirer name not valid") Boolean {
        if (type?.trim()?.toLowerCase() == "institution" && subMerchantAcquirerName != null) {
            return subMerchantAcquirerName!!.isNotBlank()
        }
        return false
    }
    private fun isValidSubMerchantId(): @AssertTrue(message = "Field Institution name not valid") Boolean {
        if (type?.trim()?.toLowerCase() == "submerchant" && subMerchantAcquirerId != null) {
            return subMerchantAcquirerId!!.isNotBlank()
        }
        return false
    }
    private fun isValidOutletName(): @AssertTrue(message = "Field Institution name not valid") Boolean {
        if (type?.trim()?.toLowerCase() == "institution" && outletName != null) {
            return outletName!!.isNotBlank()
        }
        return false
    }
    private fun isValidouteltId(): @AssertTrue(message = "Field Institution name not valid") Boolean {
        if (type?.trim()?.toLowerCase() == "institution" && outletId != null) {
            return outletId!!.isNotBlank()
        }
        return false
    }
}
