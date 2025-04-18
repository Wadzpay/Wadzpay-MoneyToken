package com.vacuumlabs.wadzpay.merchant.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.vacuumlabs.wadzpay.ledger.model.AccountOwner
import com.vacuumlabs.wadzpay.pos.MerchantPos
import com.vacuumlabs.wadzpay.user.UserAccount
import com.vacuumlabs.wadzpay.webhook.OrderWebhook
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.OneToMany
import javax.persistence.OneToOne

@Entity
data class Merchant(
    @Column(unique = true)
    val name: String,

    @Enumerated(EnumType.STRING)
    var countryOfRegistration: CountryCode,

    val registrationCode: String,

    var primaryContactFullName: String,
    var primaryContactEmail: String,
    var primaryContactPhoneNumber: String,

    val companyType: String?,

    @Enumerated(EnumType.STRING)
    val industryType: IndustryType?,

    val merchantId: String?,

    val orderExpTimeInMin: Long = 10,

    val settlementCommissionPercent: BigDecimal = 0.5.toBigDecimal(),
    val merchantCommissionPercent: BigDecimal = BigDecimal.ZERO,
    val wadzpayCommissionPercent: BigDecimal = BigDecimal.ZERO,
    var defaultRefundableFiatValue: BigDecimal = BigDecimal("500"),
    var defaultTimeZone: String? = "+5:30",
    var mdrPercentage: BigDecimal = BigDecimal.ZERO,
    var tnc: String? = null
) : AccountOwner() {
    @JsonIgnore
    @OneToMany(mappedBy = "merchant", cascade = [CascadeType.REMOVE])
    val apiKey: MutableList<MerchantApiKey> = mutableListOf()

    @JsonIgnore
    @OneToOne(mappedBy = "webhookOwner", cascade = [CascadeType.REMOVE])
    val webhook: OrderWebhook? = null

    @JsonIgnore
    @OneToMany(mappedBy = "merchant", cascade = [CascadeType.REMOVE])
    val users: MutableList<UserAccount> = mutableListOf()

    @OneToMany(mappedBy = "merchant", cascade = [CascadeType.REMOVE])
    val merchantPoses: MutableList<MerchantPos> = mutableListOf()
}

enum class CountryCode {
    AC, AD, AE, AF, AG, AI, AL, AM, AN, AO, AQ, AR, AS, AT, AU, AW, AX, AZ, BA, BB, BD, BE, BF, BG, BH, BI, BJ, BL, BM, BN, BO, BQ, BR, BS, BT, BU, BV, BW, BY, BZ, CA, CC, CD, CF, CG, CH, CI, CK, CL, CM, CN, CO, CP, CR, CS, CU, CV, CW, CX, CY, CZ, DE, DG, DJ, DK, DM, DO, DZ, EA, EC, EE, EG, EH, ER, ES, ET, EU, EZ, FI, FJ, FK, FM, FO, FR, FX, GA, GB, GD, GE, GF, GG, GH, GI, GL, GM, GN, GP, GQ, GR, GS, GT, GU, GW, GY, HK, HM, HN, HR, HT, HU, IC, ID, IE, IL, IM, IN, IO, IQ, IR, IS, IT, JE, JM, JO, JP, KE, KG, KH, KI, KM, KN, KP, KR, KW, KY, KZ, LA, LB, LC, LI, LK, LR, LS, LT, LU, LV, LY, MA, MC, MD, ME, MF, MG, MH, MK, ML, MM, MN, MO, MP, MQ, MR, MS, MT, MU, MV, MW, MX, MY, MZ, NA, NC, NE, NF, NG, NI, NL, NO, NP, NR, NT, NU, NZ, OM, PA, PE, PF, PG, PH, PK, PL, PM, PN, PR, PS, PT, PW, PY, QA, RE, RO, RS, RU, RW, SA, SB, SC, SD, SE, SF, SG, SH, SI, SJ, SK, SL, SM, SN, SO, SR, SS, ST, SU, SV, SX, SY, SZ, TA, TC, TD, TF, TG, TH, TJ, TK, TL, TM, TN, TO, TP, TR, TT, TV, TW, TZ, UA, UG, UK, UM, UNDEFINED, US, UY, UZ, VA, VC, VE, VG, VI, VN, VU, WF, WS, XK, YE, YT, YU, ZA, ZM, ZR, ZW
}

enum class IndustryType {
    ACCOUNTING,
    AIRLINES_AVIATION,
    ALTERNATIVE_DISPUTE_RESOLUTION,
    ALTERNATIVE_MEDICINE,
    ANIMATION,
    APPAREL_FASHION,
    ARCHITECTURE_PLANNING,
    ARTS_CRAFTS,
    AUTOMOTIVE,
    AVIATION_AEROSPACE,
    BANKING_MORTGAGE,
    BIOTECHNOLOGY_GREENTECH,
    BROADCAST_MEDIA,
    BUILDING_MATERIALS,
    BUSINESS_SUPPLIES_EQUIPMENT,
    CAPITAL_MARKETS_HEDGE_FUND_PRIVATE_EQUITY,
    CHEMICALS,
    CIVIC_SOCIAL_ORGANIZATION,
    CIVIL_ENGINEERING,
    COMMERCIAL_REAL_ESTATE,
    COMPUTER_GAMES,
    COMPUTER_HARDWARE,
    COMPUTER_NETWORKING,
    COMPUTER_SOFTWARE_ENGINEERING,
    COMPUTER_NETWORK_SECURITY,
    CONSTRUCTION,
    CONSUMER_ELECTRONICS,
    CONSUMER_GOODS,
    CONSUMER_SERVICES,
    COSMETICS,
    DAIRY,
    DEFENSE_SPACE,
    DESIGN,
    E_LEARNING,
    EDUCATION_MANAGEMENT,
    ELECTRICAL_ELECTRONIC_MANUFACTURING,
    ENTERTAINMENT_MOVIE_PRODUCTION,
    ENVIRONMENTAL_SERVICES,
    EVENTS_SERVICES,
    EXECUTIVE_OFFICE,
    FACILITIES_SERVICES,
    FARMING,
    FINANCIAL_SERVICES,
    FINE_ART,
    FISHERY,
    FOOD_PRODUCTION,
    FOOD_BEVERAGES,
    FUNDRAISING,
    FURNITURE,
    GAMBLING_CASINOS,
    GLASS_CERAMICS_CONCRETE,
    GOVERNMENT_ADMINISTRATION,
    GOVERNMENT_RELATIONS,
    GRAPHIC_DESIGN_WEB_DESIGN,
    HEALTH_FITNESS,
    HIGHER_EDUCATION_ACADAMIA,
    HOSPITAL_HEALTH_CARE,
    HOSPITALITY,
    HUMAN_RESOURCES_HR,
    IMPORT_EXPORT,
    INDIVIDUAL_FAMILY_SERVICES,
    INDUSTRIAL_AUTOMATION,
    INFORMATION_SERVICES,
    INFORMATION_TECHNOLOGY_IT,
    INSURANCE,
    INTERNATIONAL_AFFAIRS,
    INTERNATIONAL_TRADE_DEVELOPMENT,
    INTERNET,
    INVESTMENT_BANKING_VENTURE,
    INVESTMENT_MANAGEMENT_HEDGE_FUND_PRIVATE_EQUITY,
    JUDICIARY,
    LAW_ENFORCEMENT,
    LAW_PRACTICE_LAW_FIRMS,
    LEGAL_SERVICES,
    LEGISLATIVE_OFFICE,
    LEISURE_TRAVEL,
    LIBRARY,
    LOGISTICS_PROCUREMENT,
    LUXURY_GOODS_JEWELRY,
    MACHINERY,
    MANAGEMENT_CONSULTING,
    MARITIME,
    MARKET_RESEARCH,
    MARKETING_ADVERTISING_SALES,
    MECHANICAL_OR_INDUSTRIAL_ENGINEERING,
    MEDIA_PRODUCTION,
    MEDICAL_EQUIPMENT,
    MEDICAL_PRACTICE,
    MENTAL_HEALTH_CARE,
    MILITARY_INDUSTRY,
    MINING_METALS,
    MOTION_PICTURES_FILM,
    MUSEUMS_INSTITUTIONS,
    MUSIC,
    NANOTECHNOLOGY,
    NEWSPAPERS_JOURNALISM,
    NON_PROFIT_VOLUNTEERING,
    OIL_ENERGY_SOLAR_GREENTECH,
    ONLINE_PUBLISHING,
    OTHER_INDUSTRY,
    OUTSOURCING_OFFSHORING,
    PACKAGE_FREIGHT_DELIVERY,
    PACKAGING_CONTAINERS,
    PAPER_FOREST_PRODUCTS,
    PERFORMING_ARTS,
    PHARMACEUTICALS,
    PHILANTHROPY,
    PHOTOGRAPHY,
    PLASTICS,
    POLITICAL_ORGANIZATION,
    PRIMARY_SECONDARY_EDUCATION,
    PRINTING,
    PROFESSIONAL_TRAINING,
    PROGRAM_DEVELOPMENT,
    PUBLIC_RELATIONS_PR,
    PUBLIC_SAFETY,
    PUBLISHING_INDUSTRY,
    RAILROAD_MANUFACTURE,
    RANCHING,
    REAL_ESTATE_MORTGAGE,
    RECREATIONAL_FACILITIES_SERVICES,
    RELIGIOUS_INSTITUTIONS,
    RENEWABLES_ENVIRONMENT,
    RESEARCH_INDUSTRY,
    RESTAURANTS,
    RETAIL_INDUSTRY,
    SECURITY_INVESTIGATIONS,
    SEMICONDUCTORS,
    SHIPBUILDING,
    SPORTING_GOODS,
    SPORTS,
    STAFFING_RECRUITING,
    SUPERMARKETS,
    TELECOMMUNICATIONS,
    TEXTILES,
    THINK_TANKS,
    TOBACCO,
    TRANSLATION_LOCALIZATION,
    TRANSPORTATION,
    UTILITIES,
    VENTURE_CAPITAL_VC,
    VETERINARY,
    WAREHOUSING,
    WHOLESALE,
    WINE_SPIRITS,
    WIRELESS,
    WRITING_EDITING,
    GAMING
}

@Repository
interface MerchantRepository : CrudRepository<Merchant, Long> {
    fun getByName(name: String): Merchant?
    @Query("Select * From Merchant WHERE name=:userName AND primary_contact_email=:email", nativeQuery = true)
    fun findByNameAndPrimaryContactEmail(userName: String, email: String): Merchant?
}
