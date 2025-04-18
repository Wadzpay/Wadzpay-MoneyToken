package com.vacuumlabs.wadzpay.services

import com.opencsv.CSVParser
import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReader
import com.opencsv.CSVReaderBuilder
import com.opencsv.bean.ColumnPositionMappingStrategy
import com.opencsv.bean.CsvToBean
import com.opencsv.bean.CsvToBeanBuilder
import com.opencsv.bean.CsvToBeanFilter
import com.opencsv.bean.HeaderColumnNameTranslateMappingStrategy
import com.opencsv.bean.MappingStrategy
import com.opencsv.bean.StatefulBeanToCsv
import com.opencsv.bean.StatefulBeanToCsvBuilder
import com.vacuumlabs.wadzpay.acquireradministration.DTOEntityConverterService
import com.vacuumlabs.wadzpay.acquireradministration.dto.AggregatorDTO
import com.vacuumlabs.wadzpay.acquireradministration.dto.InstitutionDTO
import com.vacuumlabs.wadzpay.acquireradministration.dto.MerchantAcquirerDTO
import com.vacuumlabs.wadzpay.acquireradministration.dto.MerchantGroupDTO
import com.vacuumlabs.wadzpay.acquireradministration.dto.OutletDTO
import com.vacuumlabs.wadzpay.acquireradministration.dto.SubMerchantAcquirerDTO
import com.vacuumlabs.wadzpay.acquireradministration.model.Aggregator
import com.vacuumlabs.wadzpay.acquireradministration.model.AggregatorLevels
import com.vacuumlabs.wadzpay.acquireradministration.model.AggregatorRepository
import com.vacuumlabs.wadzpay.acquireradministration.model.EntityAddress
import com.vacuumlabs.wadzpay.acquireradministration.model.EntityAdminDetails
import com.vacuumlabs.wadzpay.acquireradministration.model.EntityBankDetails
import com.vacuumlabs.wadzpay.acquireradministration.model.EntityContactDetails
import com.vacuumlabs.wadzpay.acquireradministration.model.EntityInfo
import com.vacuumlabs.wadzpay.acquireradministration.model.EntityOthers
import com.vacuumlabs.wadzpay.acquireradministration.model.EntityOthersRepository
import com.vacuumlabs.wadzpay.acquireradministration.model.Institution
import com.vacuumlabs.wadzpay.acquireradministration.model.InstitutionRepository
import com.vacuumlabs.wadzpay.acquireradministration.model.MerchantAcquirer
import com.vacuumlabs.wadzpay.acquireradministration.model.MerchantAcquirerRepository
import com.vacuumlabs.wadzpay.acquireradministration.model.MerchantGroup
import com.vacuumlabs.wadzpay.acquireradministration.model.MerchantGroupRepository
import com.vacuumlabs.wadzpay.acquireradministration.model.Outlet
import com.vacuumlabs.wadzpay.acquireradministration.model.OutletRepository
import com.vacuumlabs.wadzpay.acquireradministration.model.PosRepository
import com.vacuumlabs.wadzpay.acquireradministration.model.SubMerchantAcquirer
import com.vacuumlabs.wadzpay.acquireradministration.model.SubMerchantRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.StringJoiner
import javax.persistence.EntityManager
import javax.servlet.http.HttpServletResponse
import javax.validation.ConstraintViolation
import javax.validation.Validation

// import com.fasterxml.jackson.dataformat.

@Service
class AcquirerCsvServcive(
    val aggregatorRepository: AggregatorRepository,
    val institutionRepository: InstitutionRepository,
    val merchantGroupRepository: MerchantGroupRepository,
    val merchantAcquirerRepository: MerchantAcquirerRepository,
    val subMerchantRepository: SubMerchantRepository,
    val outletRepository: OutletRepository,
    val posRepository: PosRepository,
    val entityOthersRepository: EntityOthersRepository,
    val dtoService: DTOEntityConverterService,
    private val entityManager: EntityManager
) {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)
    val dateFormatterDb: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    fun writeOutletToCSV(errorList: List<OutletDTO>, response: HttpServletResponse) {
        val mappingStrategy = object : ColumnPositionMappingStrategy<OutletDTO>() {
            override fun generateHeader(bean: OutletDTO): Array<String> {
                super.generateHeader(bean)
                return arrayOf(
                    "Aggregator Id", "Insitution Id",	"Merchant Group Id",	"Merchant  Id",	"Sub Merchant  Id",	"Outlet Id",	"Outlet Name",	"Outlet Logo",	"Address Line1",	"Address Line2", "Address Line3",	"City", "State",	"Country",	"Postal Code",	"Admin FirstName",	"Admin MiddleName",	"Admin LastName",	"Admin EmailId", "AdminDetails MobileNumber",	"Admin Department", "Bank Name",	"Bank Account Number",	"Bank Holder Name",	"Bank Branch Code",	"Bank Branch Location",	"ContactDetails FirstName",	"ContactDetails MiddleName",	"ContactDetails LastName",	"ContactDetails EmailId",	"ContactDetails MobileNumber",	"ContactDetails Designation",	"ContactDetails Department", "Abbrevation",	"Description",	"Logo",	"Region",	"Timezone",	"Outlet Type",	"Default Digital Currency",	"Base Fiat Currency",	"Customer Offline Txn",	"Merchant Offline Txn",	"Approval WorkFlow",	"Activation Date", "error"
                )

                return super.generateHeader(bean)
            }
        }
        mappingStrategy.type = OutletDTO::class.java
        val writer = OutputStreamWriter(response.getOutputStream())
        val csvWriter: StatefulBeanToCsv<OutletDTO> = StatefulBeanToCsvBuilder<OutletDTO>(writer).withMappingStrategy(mappingStrategy)
            .withSeparator(',') // Customize separator if needed
            .build()
        csvWriter.write(errorList)
        writer.flush()
    }
    fun writeSubMerchantoCSV(errorList: List<SubMerchantAcquirerDTO>, response: HttpServletResponse) {
        val mappingStrategy = object : ColumnPositionMappingStrategy<SubMerchantAcquirerDTO>() {
            override fun generateHeader(bean: SubMerchantAcquirerDTO): Array<String> {
                super.generateHeader(bean)
                return arrayOf(
                    "Aggregator Id", "Insitution Id",	"Merchant Group Id",	"Merchant  Id",	"Sub Merchant  Id",	"Sub Merchant Name",	"Sub Merchant Logo",	"Address Line1",	"Address Line2", "Address Line3",	"City", "State",	"Country",	"Postal Code",	"Admin FirstName",	"Admin MiddleName",	"Admin LastName",	"Admin EmailId", "AdminDetails MobileNumber",	"Admin Department", "Bank Name",	"Bank Account Number",	"Bank Holder Name",	"Bank Branch Code",	"Bank Branch Location",	"ContactDetails FirstName",	"ContactDetails MiddleName",	"ContactDetails LastName",	"ContactDetails EmailId",	"ContactDetails MobileNumber",	"ContactDetails Designation",	"ContactDetails Department", "Abbrevation",	"Description",	"Logo",	"Region",	"Timezone",	"Sub Merchant Type",	"Default Digital Currency",	"Base Fiat Currency",	"Customer Offline Txn",	"Merchant Offline Txn",	"Approval WorkFlow",	"Activation Date", "error"
                )

                return super.generateHeader(bean)
            }
        }
        /*val mappingStrategy: ColumnPositionMappingStrategy<OutletDTO> =
            ColumnPositionMappingStrategy<OutletDTO>()
*/ mappingStrategy.type = SubMerchantAcquirerDTO::class.java
        // mappingStrategy.setColumnMapping("Aggregator Id"	,"Insitution Id",	"Merchant Group Id",	"Merchant  Id",	"Sub Merchant  Id",	"Outlet Id",	"Outlet Name",	"Outlet Logo",	"Address Line1",	"Address Line2"	,"Address Line3",	"City", 	"State",	"Country",	"Postal Code",	"Admin FirstName",	"Admin MiddleName",	"Admin LastName",	"Admin EmailId",	"Admin Department",	 "Bank Name",	"Bank Account Number",	"Bank Holder Name",	"Bank Branch Code",	"Bank Branch Location",	"ContactDetails FirstName",	"ContactDetails MiddleName",	"ContactDetails LastName",	"ContactDetails EmailId",	"ContactDetails MobileNumber",	"ContactDetails Designation",	"ContactDetails Department","Abbrevation",	"Description",	"Logo",	"Region",	"Timezone",	"Outlet Type",	"Default Digital Currency",	"Base Fiat Currency",	"Customer Offline Txn",	"Merchant Offline Txn",	"Approval WorkFlow",	"Activation Date","error")
        val writer = OutputStreamWriter(response.getOutputStream())
        // mappingStrategy.generateHeader(OutletDTO("Aggregator Id"	,"Insitution Id",	"Merchant Group Id",	"Merchant  Id",	"Sub Merchant  Id",	"Outlet Id",	"Outlet Name",	"Outlet Logo",	"Address Line1",	"Address Line2"	,"Address Line3",	"City", 	"State",	"Country",	"Postal Code",	"Admin FirstName",	"Admin MiddleName",	"Admin LastName",	"Admin EmailId",	"Admin Department",	 "Bank Name",	"Bank Account Number",	"Bank Holder Name",	"Bank Branch Code",	"Bank Branch Location",	"ContactDetails FirstName",	"ContactDetails MiddleName",	"ContactDetails LastName",	"ContactDetails EmailId",	"ContactDetails MobileNumber",	"ContactDetails Designation",	"ContactDetails Department","Abbrevation",	"Description",	"Logo",	"Region",	"Timezone",	"Outlet Type",	"Default Digital Currency",	"Base Fiat Currency",	"Customer Offline Txn",	"Merchant Offline Txn",	"Approval WorkFlow",	"Activation Date","error"))
        val csvWriter: StatefulBeanToCsv<SubMerchantAcquirerDTO> = StatefulBeanToCsvBuilder<SubMerchantAcquirerDTO>(writer).withMappingStrategy(mappingStrategy)
            .withSeparator(',') // Customize separator if needed
            .build()
        csvWriter.write(errorList)
        writer.flush()
    }
    fun writeMerchantoCSV(errorList: List<MerchantAcquirerDTO>, response: HttpServletResponse) {
        val mappingStrategy = object : ColumnPositionMappingStrategy<MerchantAcquirerDTO>() {
            override fun generateHeader(bean: MerchantAcquirerDTO): Array<String> {
                super.generateHeader(bean)
                return arrayOf(
                    "Aggregator Id", "Insitution Id",	"Merchant Group Id",	"Merchant  Id",	" Merchant Name",	" Merchant Logo",	"Address Line1",	"Address Line2", "Address Line3",	"City", "State",	"Country",	"Postal Code",	"Admin FirstName",	"Admin MiddleName",	"Admin LastName",	"Admin EmailId", "AdminDetails MobileNumber",	"Admin Department", "Bank Name",	"Bank Account Number",	"Bank Holder Name",	"Bank Branch Code",	"Bank Branch Location",	"ContactDetails FirstName",	"ContactDetails MiddleName",	"ContactDetails LastName",	"ContactDetails EmailId",	"ContactDetails MobileNumber",	"ContactDetails Designation",	"ContactDetails Department", "Abbrevation",	"Description",	"Logo",	"Region",	"Timezone",	" Merchant Type",	"Default Digital Currency",	"Base Fiat Currency",	"Customer Offline Txn",	"Merchant Offline Txn",	"Approval WorkFlow",	"Activation Date", "error"
                )

                return super.generateHeader(bean)
            }
        }
        mappingStrategy.type = MerchantAcquirerDTO::class.java
        val writer = OutputStreamWriter(response.getOutputStream())
        val csvWriter: StatefulBeanToCsv<MerchantAcquirerDTO> = StatefulBeanToCsvBuilder<MerchantAcquirerDTO>(writer).withMappingStrategy(mappingStrategy)
            .withSeparator(',') // Customize separator if needed
            .build()
        csvWriter.write(errorList)
        writer.flush()
    }
    fun writeMerchanGrouptoCSV(errorList: List<MerchantGroupDTO>, response: HttpServletResponse) {
        val mappingStrategy = object : ColumnPositionMappingStrategy<MerchantGroupDTO>() {
            override fun generateHeader(bean: MerchantGroupDTO): Array<String> {
                super.generateHeader(bean)
                return arrayOf(
                    "Aggregator Id", "Insitution Id",	"Merchant Group Id",	" Merchant Group Name",	" Merchant Logo",	"Address Line1",	"Address Line2", "Address Line3",	"City", "State",	"Country",	"Postal Code",	"Admin FirstName",	"Admin MiddleName",	"Admin LastName",	"Admin EmailId", "AdminDetails MobileNumber",	"Admin Department", "Bank Name",	"Bank Account Number",	"Bank Holder Name",	"Bank Branch Code",	"Bank Branch Location",	"ContactDetails FirstName",	"ContactDetails MiddleName",	"ContactDetails LastName",	"ContactDetails EmailId",	"ContactDetails MobileNumber",	"ContactDetails Designation",	"ContactDetails Department", "Abbrevation",	"Description",	"Logo",	"Region",	"Timezone",	" Merchant Group Type",	"Default Digital Currency",	"Base Fiat Currency",	"Customer Offline Txn",	"Merchant Offline Txn",	"Approval WorkFlow",	"Activation Date", "error"
                )

                return super.generateHeader(bean)
            }
        }
        mappingStrategy.type = MerchantGroupDTO::class.java
        val writer = OutputStreamWriter(response.getOutputStream())
        val csvWriter: StatefulBeanToCsv<MerchantGroupDTO> = StatefulBeanToCsvBuilder<MerchantGroupDTO>(writer).withMappingStrategy(mappingStrategy)
            .withSeparator(',') // Customize separator if needed
            .build()
        csvWriter.write(errorList)
        writer.flush()
    }

    fun writeInstitutiontoCSV(errorList: List<InstitutionDTO>, response: HttpServletResponse) {
        val mappingStrategy = object : ColumnPositionMappingStrategy<InstitutionDTO>() {
            override fun generateHeader(bean: InstitutionDTO): Array<String> {
                super.generateHeader(bean)
                return arrayOf(
                    "Aggregator Id", "Institution Id",	"Client  Institution Id",	" Institution Name",	" Institution Logo",	"Address Line1",	"Address Line2", "Address Line3",	"City", "State",	"Country",	"Postal Code",	"Admin FirstName",	"Admin MiddleName",	"Admin LastName",	"Admin EmailId", "AdminDetails MobileNumber",	"Admin Department", "Bank Name",	"Bank Account Number",	"Bank Holder Name",	"Bank Branch Code",	"Bank Branch Location",	"ContactDetails FirstName",	"ContactDetails MiddleName",	"ContactDetails LastName",	"ContactDetails EmailId",	"ContactDetails MobileNumber",	"ContactDetails Designation",	"ContactDetails Department", "Abbrevation",	"Description",	"Logo",	"Region",	"Timezone",	" Institution Type",	"Default Digital Currency",	"Base Fiat Currency",	"Customer Offline Txn",	"Merchant Offline Txn",	"Approval WorkFlow",	"Activation Date", "error"
                )

                return super.generateHeader(bean)
            }
        }
        mappingStrategy.type = InstitutionDTO::class.java
        val writer = OutputStreamWriter(response.getOutputStream())
        val csvWriter: StatefulBeanToCsv<InstitutionDTO> = StatefulBeanToCsvBuilder<InstitutionDTO>(writer).withMappingStrategy(mappingStrategy)
            .withSeparator(',') // Customize separator if needed
            .build()
        csvWriter.write(errorList)
        writer.flush()
    }

    fun writeAggregatorToCSV(errorList: List<AggregatorDTO>, response: HttpServletResponse) {
        val mappingStrategy = object : ColumnPositionMappingStrategy<AggregatorDTO>() {
            override fun generateHeader(bean: AggregatorDTO): Array<String> {
                super.generateHeader(bean)
                return arrayOf(
                    "type", "Aggregator Id",	" Aggregator Name",	" Aggregator Logo",	"institutionId", "clientInstitutionId", "institution Name", "institution Logo",
                    "Address Line1",	"Address Line2", "Address Line3",	"City", "State",	"Country",	"Postal Code",	"Admin FirstName",	"Admin MiddleName",	"Admin LastName",	"Admin EmailId", "AdminDetails MobileNumber",	"Admin Department", "Bank Name",	"Bank Account Number",	"Bank Holder Name",	"Bank Branch Code",	"Bank Branch Location",	"ContactDetails FirstName",	"ContactDetails MiddleName",	"ContactDetails LastName",	"ContactDetails EmailId",	"ContactDetails MobileNumber",	"ContactDetails Designation",	"ContactDetails Department", "Abbrevation",	"Description",	"Logo",	"Region",	"Timezone",	" Aggregator Type",	"Default Digital Currency",	"Base Fiat Currency",	"Customer Offline Txn",	"Merchant Offline Txn",	"Approval WorkFlow",	"Activation Date", "error"
                )

                return super.generateHeader(bean)
            }
        }
        mappingStrategy.type = AggregatorDTO::class.java
        val writer = OutputStreamWriter(response.getOutputStream())
        val csvWriter: StatefulBeanToCsv<AggregatorDTO> = StatefulBeanToCsvBuilder<AggregatorDTO>(writer).withMappingStrategy(mappingStrategy)
            .withSeparator(',') // Customize separator if needed
            .build()
        csvWriter.write(errorList)
        writer.flush()
    }

    fun readCsvAggregator2(fileReader: BufferedReader): AggregatorLevels {

        var errorListAggregator = mutableListOf<Aggregator>()
        var errorListInstitution = mutableListOf<Institution>()
        var errorListMerchantGroup = mutableListOf<MerchantGroup>()
        var errorListMerchant = mutableListOf<MerchantAcquirer>()
        var errorListSubMerchant = mutableListOf<SubMerchantAcquirer>()
        var errorListOutlet = mutableListOf<Outlet>()

        val csvReader1: CsvToBean<AggregatorDTO> = CsvToBeanBuilder<AggregatorDTO>(fileReader)
            .withType(AggregatorDTO::class.java)
            .withSkipLines(1)
            .withSeparator(',')
            .withIgnoreLeadingWhiteSpace(true)
            .withIgnoreEmptyLine(true)
            .build()

        val results: List<AggregatorDTO> = csvReader1.parse()
        val factory = Validation.buildDefaultValidatorFactory()
        val validator = factory.validator
        results.forEach { it ->
            val violations: Set<ConstraintViolation<AggregatorDTO>> = validator.validate(it)
            var error = StringBuilder()
            val joiner = StringJoiner(",")
            if (violations.isNotEmpty()) {
                if (violations.isNotEmpty()) {
                    violations.forEach {
                        if (!joiner.toString().contains(it.message))
                            joiner.add(it.message)
                    }
                    // error.append(joiner)
                }
            }

            if (joiner.toString().isNotEmpty()) {
                it.error = joiner.toString()
            }
            if (it.entityOthersActivationDate.toString().isNotEmpty()) {
                try {
                    val activationDate = LocalDate.parse(it.entityOthersActivationDate.toString().trim(), dateFormatter)
                    if (activationDate > LocalDate.now()
                    ) {
                        if (it.type == "institution") {
                            it.institutionStatus = "pending"
                        }
                        if (it.type == "merchantGroup") {
                            it.merchantGroupStatus = "pending"
                        }
                        if (it.type == "merchant") {
                            it.merchantAcquirerStatus = "pending"
                        }
                        if (it.type == "submerchant") {
                            it.subMerchantAcquirerStatus = "pending"
                        }
                        if (it.type == "outlet") {
                            it.outletStatus = "pending"
                        } else {
                            it.aggregatorStatus = "pending"
                        }
                    } else {
                        if (it.type == "institution") {
                            it.institutionStatus = "active"
                        } else {
                            it.aggregatorStatus = "active"
                        }
                    }
                    it.entityOthersActivationDate = activationDate.format(dateFormatterDb)
                } catch (e: DateTimeParseException) {
                    logger.error("Error occurred in date parsing   == ${e.message} ")
                    it.error = joiner.add("Un parseable Date Please provide desired format").toString()
                }
            }
            if (it.type.toString() == "aggregator") {
                errorListAggregator.add(dtoService.toAggregatorModel(it))
            }
            if (it.type.toString() == "institution") {
                errorListInstitution.add(dtoService.toInstitution1(it))
            }
            if (it.type.toString() == "merchantGroup") {
                errorListMerchantGroup.add(dtoService.toMerchantGroup1(it))
            }
            if (it.type.toString() == "merchant") {
                errorListMerchant.add(dtoService.toMerchant1(it))
            }
            if (it.type.toString() == "submerchant") {
                errorListSubMerchant.add(dtoService.toSubMerchant1(it))
            }
            if (it.type.toString() == "outlet") {
                errorListOutlet.add(dtoService.toOutletModel1(it))
            }
        }
        println(results)
        return AggregatorLevels(errorListAggregator, errorListInstitution, errorListMerchantGroup, errorListMerchant, errorListSubMerchant, errorListOutlet)
    }

    private fun buildCsvReader(fileReader: BufferedReader, csvParser: CSVParser): CSVReader {
        return CSVReaderBuilder(fileReader)
            .withSkipLines(1)
            .withCSVParser(csvParser)
            .build()
    }

    fun readCsvInstitute(fileReader: BufferedReader): List<Institution> {

        val strategy: HeaderColumnNameTranslateMappingStrategy<Institution> =
            HeaderColumnNameTranslateMappingStrategy<Institution>()
        strategy.setType(Institution::class.java)
        var errorList = mutableListOf<Institution>()
        val csvReader1: CsvToBean<InstitutionDTO> = CsvToBeanBuilder<InstitutionDTO>(fileReader)
            .withType(InstitutionDTO::class.java)
            .withSkipLines(1)
            .withSeparator(',')
            .withIgnoreLeadingWhiteSpace(true)
            .withIgnoreEmptyLine(true)
            .build()

        val results: List<InstitutionDTO> = csvReader1.parse()
        val factory = Validation.buildDefaultValidatorFactory()
        val validator = factory.validator
        results.forEach { it ->
            val violations: Set<ConstraintViolation<InstitutionDTO>> = validator.validate(it)
            var error = StringBuilder()
            val joiner = StringJoiner(",")
            if (violations.isNotEmpty()) {
                violations.forEach {
                    if (!joiner.toString().contains(it.message))
                        joiner.add(it.message)
                }
                // error.append(joiner)
            }

            if (joiner.toString().isNotEmpty()) {
                it.error = joiner.toString()
            }
            try {
                if (it.entityOthersActivationDate.toString().isNotEmpty()) {
                    val activationDate = LocalDate.parse(it.entityOthersActivationDate.toString().trim(), dateFormatter)
                    if (activationDate > LocalDate.now()
                    ) {
                        it.institutionStatus = "pending"
                    } else {
                        it.institutionStatus = "active"
                    }
                    it.entityOthersActivationDate = activationDate.format(dateFormatterDb)
                }
            } catch (e: DateTimeParseException) {
                logger.error("Error occurred in date parsing for Institution  == ${e.message} ")
                it.error = joiner.add("Un parseable Date Please provide desired format").toString()
            }
            errorList.add(dtoService.toInstitution(it))
        }
        println(results)
        return errorList
    }
    fun readCsvMerchantGroup(fileReader: BufferedReader): List<MerchantGroup> {

        val strategy: HeaderColumnNameTranslateMappingStrategy<MerchantGroup> =
            HeaderColumnNameTranslateMappingStrategy<MerchantGroup>()
        strategy.setType(MerchantGroup::class.java)
        var errorList = mutableListOf<MerchantGroup>()
        val csvReader1: CsvToBean<MerchantGroupDTO> = CsvToBeanBuilder<MerchantGroupDTO>(fileReader)
            .withType(MerchantGroupDTO::class.java)
            .withSkipLines(1)
            .withSeparator(',')
            .withIgnoreLeadingWhiteSpace(true)
            .withIgnoreEmptyLine(true)
            .build()

        val results: List<MerchantGroupDTO> = csvReader1.parse()
        val factory = Validation.buildDefaultValidatorFactory()
        val validator = factory.validator
        results.forEach { it ->
            val violations: Set<ConstraintViolation<MerchantGroupDTO>> = validator.validate(it)
            var error = StringBuilder()
            val joiner = StringJoiner(",")
            if (violations.isNotEmpty()) {
                violations.forEach {
                    if (!joiner.toString().contains(it.message))
                        joiner.add(it.message)
                }
                // error.append(joiner)
            }

            if (joiner.toString().isNotEmpty()) {
                it.error = joiner.toString()
            }
            try {
                if (it.entityOthersActivationDate.toString().isNotEmpty()) {
                    val activationDate = LocalDate.parse(it.entityOthersActivationDate.toString().trim(), dateFormatter)
                    if (activationDate > LocalDate.now()
                    ) {
                        it.merchantGroupStatus = "pending"
                    } else {
                        it.merchantGroupStatus = "active"
                    }
                    it.entityOthersActivationDate = activationDate.format(dateFormatterDb)
                }
            } catch (e: DateTimeParseException) {
                logger.error("Error occurred in date parsing  for Merchantgroup  == ${e.message} ")
                it.error = joiner.add("Un parseable Date Please provide desired format").toString()
            }
            errorList.add(dtoService.toMerchantGroup(it))
        }
        println(results)
        return errorList
    }
    fun readCsvMerchant(fileReader: BufferedReader): List<MerchantAcquirer> {
        val strategy: HeaderColumnNameTranslateMappingStrategy<MerchantAcquirer> =
            HeaderColumnNameTranslateMappingStrategy<MerchantAcquirer>()
        strategy.setType(MerchantAcquirer::class.java)
        var errorList = mutableListOf<MerchantAcquirer>()
        val csvReader: CsvToBean<MerchantAcquirerDTO> = CsvToBeanBuilder<MerchantAcquirerDTO>(fileReader)
            .withType(MerchantAcquirerDTO::class.java)
            .withSkipLines(1)
            .withSeparator(',')
            .withIgnoreLeadingWhiteSpace(true)
            .withIgnoreEmptyLine(true)
            .build()

        val results: List<MerchantAcquirerDTO> = csvReader.parse()
        val factory = Validation.buildDefaultValidatorFactory()
        val validator = factory.validator
        results.forEach { it ->
            val violations: Set<ConstraintViolation<MerchantAcquirerDTO>> = validator.validate(it)
            var error = StringBuilder()
            val joiner = StringJoiner(",")
            if (violations.isNotEmpty()) {
                violations.forEach {
                    if (!joiner.toString().contains(it.message))
                        joiner.add(it.message)
                }
                // error.append(joiner)
            }

            if (joiner.toString().isNotEmpty()) {
                it.error = joiner.toString()
            }
            try {
                if (it.entityOthersActivationDate.toString().isNotEmpty()) {
                    val activationDate = LocalDate.parse(it.entityOthersActivationDate.toString().trim(), dateFormatter)
                    if (activationDate > LocalDate.now()
                    ) {
                        it.merchantStatus = "pending"
                    } else {
                        it.merchantStatus = "active"
                    }
                    it.entityOthersActivationDate = activationDate.format(dateFormatterDb)
                }
            } catch (e: DateTimeParseException) {
                logger.error("Error occurred in date parsing  for Merchant  == ${e.message} ")
                it.error = joiner.add("Un parseable Date Please provide desired format").toString()
            }
            errorList.add(dtoService.toMerchant(it))
        }
        println(results)
        return errorList
    }
    fun readCsvSubMerchant(fileReader: BufferedReader): List<SubMerchantAcquirer> {
        val strategy: HeaderColumnNameTranslateMappingStrategy<SubMerchantAcquirer> =
            HeaderColumnNameTranslateMappingStrategy<SubMerchantAcquirer>()
        strategy.setType(SubMerchantAcquirer::class.java)
        var errorList = mutableListOf<SubMerchantAcquirer>()
        val csvReader: CsvToBean<SubMerchantAcquirerDTO> = CsvToBeanBuilder<SubMerchantAcquirerDTO>(fileReader)
            .withType(SubMerchantAcquirerDTO::class.java)
            .withSkipLines(1)
            .withSeparator(',')
            .withIgnoreLeadingWhiteSpace(true)
            .withIgnoreEmptyLine(true)
            .build()

        val results: List<SubMerchantAcquirerDTO> = csvReader.parse()
        val factory = Validation.buildDefaultValidatorFactory()
        val validator = factory.validator
        results.forEach { it ->
            val violations: Set<ConstraintViolation<SubMerchantAcquirerDTO>> = validator.validate(it)
            var error = StringBuilder()
            val joiner = StringJoiner(",")
            if (violations.isNotEmpty()) {
                violations.forEach {
                    if (!joiner.toString().contains(it.message))
                        joiner.add(it.message)
                }
                // error.append(joiner)
            }

            if (joiner.toString().isNotEmpty()) {
                it.error = joiner.toString()
            }
            try {
                if (it.entityOthersActivationDate.toString().isNotEmpty()) {
                    val activationDate = LocalDate.parse(it.entityOthersActivationDate.toString().trim(), dateFormatter)
                    if (activationDate > LocalDate.now()
                    ) {
                        it.subMerchantStatus = "pending"
                    } else {
                        it.subMerchantStatus = "active"
                    }
                    it.entityOthersActivationDate = activationDate.format(dateFormatterDb)
                }
            } catch (e: DateTimeParseException) {
                logger.error("Error occurred in date parsing  for subMerchant  == ${e.message} ")
                it.error = joiner.add("Un parseable Date Please provide desired format").toString()
            }
            errorList.add(dtoService.toSubMerchant(it))
        }
        println(results)
        return errorList
    }

    fun readCsvOutlet(fileReader: BufferedReader): List<Outlet> {
        val strategy: HeaderColumnNameTranslateMappingStrategy<Outlet> =
            HeaderColumnNameTranslateMappingStrategy<Outlet>()
        strategy.setType(Outlet::class.java)
        var errorList = mutableListOf<Outlet>()
        val csvReader: CsvToBean<OutletDTO> = CsvToBeanBuilder<OutletDTO>(fileReader)
            .withType(OutletDTO::class.java)
            .withSkipLines(1)
            .withSeparator(',')
            .withIgnoreLeadingWhiteSpace(true)
            .withIgnoreEmptyLine(true)
            .build()

        val results: List<OutletDTO> = csvReader.parse().filter { it -> it.aggregatorPreferenceId?.isNotEmpty() == true }
        val factory = Validation.buildDefaultValidatorFactory()
        val validator = factory.validator
        results.forEach { it ->
            val violations: Set<ConstraintViolation<OutletDTO>> = validator.validate(it)
            var error = StringBuilder()
            val joiner = StringJoiner(",")
            if (violations.isNotEmpty()) {
                violations.forEach {
                    if (!joiner.toString().contains(it.message))
                        joiner.add(it.message)
                }
                // error.append(joiner)
            }

            if (joiner.toString().isNotEmpty()) {
                it.error = joiner.toString()
            }
            try {
                if (it.entityOthersActivationDate.toString().isNotEmpty()) {
                    val activationDate = LocalDate.parse(it.entityOthersActivationDate.toString().trim(), dateFormatter)
                    if (activationDate > LocalDate.now()
                    ) {
                        it.outletStatus = "pending"
                    } else {
                        it.outletStatus = "active"
                    }
                    it.entityOthersActivationDate = activationDate.format(dateFormatterDb)
                }
            } catch (e: DateTimeParseException) {
                logger.error("Error occurred in date parsing  for outlet  == ${e.message} ")
                it.error = joiner.add("Un parseable Date Please provide desired format").toString()
            }
            errorList.add(dtoService.toOutletModel(it))
        }
        println("errotList $errorList")
        return errorList
    }

    private fun buildCsvParser(): CSVParser {
        return CSVParserBuilder()
            .withSeparator(',')
            .withIgnoreQuotations(true)
            .build()
    }

    private operator fun <T : Any> Array<T>.component6(): Any {
        return this[5]
    }
    private operator fun <T : Any> Array<T>.component7(): Any {
        return this[6]
    }
    private operator fun <T : Any> Array<T>.component8(): Any {
        return this[7]
    }
    private operator fun <T : Any> Array<T>.component9(): Any {
        return this[8]
    } private operator fun <T : Any> Array<T>.component10(): Any {
        return this[9]
    } private operator fun <T : Any> Array<T>.component11(): Any {
        return this[10]
    } private operator fun <T : Any> Array<T>.component12(): Any {
        return this[11]
    } private operator fun <T : Any> Array<T>.component13(): Any {
        return this[12]
    }
    private operator fun <T : Any> Array<T>.component14(): Any {
        return this[13]
    }
    private operator fun <T : Any> Array<T>.component15(): Any {
        return this[14]
    }
    private operator fun <T : Any> Array<T>.component16(): Any {
        return this[15]
    }
    private operator fun <T : Any> Array<T>.component17(): Any {
        return this[16]
    } private operator fun <T : Any> Array<T>.component18(): Any {
        return this[17]
    } private operator fun <T : Any> Array<T>.component19(): Any {
        return this[18]
    } private operator fun <T : Any> Array<T>.component20(): Any {
        return this[19]
    } private operator fun <T : Any> Array<T>.component21(): Any {
        return this[20]
    } private operator fun <T : Any> Array<T>.component22(): Any {
        return this[21]
    }
    private operator fun <T : Any> Array<T>.component23(): Any {
        return this[22]
    }
    private operator fun <T : Any> Array<T>.component24(): Any {
        return this[23]
    }
    private operator fun <T : Any> Array<T>.component25(): Any {
        return this[24]
    } private operator fun <T : Any> Array<T>.component26(): Any {
        return this[25]
    } private operator fun <T : Any> Array<T>.component27(): Any {
        return this[26]
    } private operator fun <T : Any> Array<T>.component28(): Any {
        return this[27]
    } private operator fun <T : Any> Array<T>.component29(): Any {
        return this[28]
    } private operator fun <T : Any> Array<T>.component30(): Any {
        return this[29]
    }
    private operator fun <T : Any> Array<T>.component31(): Any {
        return this[30]
    }
    private operator fun <T : Any> Array<T>.component32(): Any {
        return this[31]
    }
    private operator fun <T : Any> Array<T>.component33(): Any {
        return this[32]
    } private operator fun <T : Any> Array<T>.component34(): Any {
        return this[33]
    } private operator fun <T : Any> Array<T>.component35(): Any {
        return this[34]
    } private operator fun <T : Any> Array<T>.component36(): Any {
        return this[35]
    } private operator fun <T : Any> Array<T>.component37(): Any {
        return this[36]
    } private operator fun <T : Any> Array<T>.component38(): Any {
        return this[37]
    }
    private operator fun <T : Any> Array<T>.component39(): Any {
        return this[38]
    }
    private operator fun <T : Any> Array<T>.component40(): Any {
        return this[39]
    }
    private operator fun <T : Any> Array<T>.component41(): Any {
        return this[40]
    }
    private operator fun <T : Any> Array<T>.component42(): Any {
        return this[41]
    }

    private operator fun <T : Any> Array<T>.component43(): Any {
        return this[42]
    }
    private operator fun <T : Any> Array<T>.component44(): Any {
        return this[43]
    }
    private operator fun <T : Any> Array<T>.component45(): Any {
        return this[44]
    }
    private operator fun <T : Any> Array<T>.component46(): Any {
        return this[45]
    }
}

fun updateMapping(mapping: MutableMap<String, String>): MutableMap<String, String> {
    mapping["entityAddressAddressLine1"] = "entityAddressAddressLine1"
    mapping["entityAddressAddressLine2"] = "entityAddressAddressLine2"
    mapping["entityAddressAddressLine3"] = "entityAddressAddressLine3"
    mapping["entityAddressCity"] = "entityAddressCity"
    mapping["entityAddressState"] = "entityAddressState"
    mapping["entityAddressCountry"] = "entityAddressCountry"
    mapping["entityAddressPostalCode"] = "entityAddressPostalCode"
    mapping["entityAdminDetailsFirstName"] = "entityAdminDetailsFirstName"
    mapping["entityAdminDetailsMiddleName"] = "entityAdminDetailsMiddleName"
    mapping["entityAdminDetailsLastName"] = "entityAdminDetailsLastName"
    mapping["entityAdminDetailsEmailId"] = "entityAdminDetailsEmailId"
    mapping["entityAdminDetailsMobileNumber"] = "entityAdminDetailsMobileNumber"
    mapping["entityAdminDetailsDepartment"] = "entityAdminDetailsDepartment"
    mapping["entityBankDetailsBankName"] = "entityBankDetailsBankName"
    mapping["entityBankDetailsBankAccountNumber"] = "entityBankDetailsBankAccountNumber"
    mapping["entityBankDetailsBankHolderName"] = "entityBankDetailsBankHolderName"
    mapping["entityBankDetailsBranchCode"] = "entityBankDetailsBranchCode"
    mapping["entityBankDetailsBranchLocation"] = "entityBankDetailsBranchLocation"
    mapping["entityContactDetailsFirstName"] = "entityContactDetailsFirstName"
    mapping["entityContactDetailsMiddleName"] = "entityContactDetailsMiddleName"
    mapping["entityContactDetailsLastName"] = "entityContactDetailsLastName"
    mapping["entityContactDetailsEmailId"] = "entityContactDetailsEmailId"
    mapping["entityContactDetailsMobileNumber"] = "entityContactDetailsMobileNumber"
    mapping["entityBankDetailsBankHolderName"] = "entityBankDetailsBankHolderName"
    mapping["entityContactDetailsDesignation"] = "entityContactDetailsDesignation"
    mapping["entityContactDetailsDepartment"] = "entityContactDetailsDepartment"
    mapping["entityInfoAbbrevation"] = "entityInfoAbbrevation"
    mapping["entityInfoDescription"] = "entityInfoDescription"
    mapping["entityInfoLogo"] = "entityInfoLogo"
    mapping["entityInfoRegion"] = "entityInfoRegion"
    mapping["entityInfoTimezone"] = "entityInfoTimezone"
    mapping["entityInfoType"] = "entityInfoType"
    mapping["entityInfoDefaultDigitalCurrency"] = "entityInfoDefaultDigitalCurrency"
    mapping["entityInfoBaseFiatCurrency"] = "entityInfoBaseFiatCurrency"
    mapping["entityOthersCustomerOfflineTxn"] = "entityOthersCustomerOfflineTxn"
    mapping["entityOthersMerchantOfflineTxn"] = "entityOthersMerchantOfflineTxn"
    mapping["entityOthersApprovalWorkFlow"] = "entityOthersApprovalWorkFlow"
    mapping["entityOthersActivationDate"] = "entityOthersActivationDate"
    return mapping
}
fun updateAddress(address: EntityAddress, entityAddressAddressLine1: String, entityAddressAddressLine2: String, entityAddressAddressLine3: String, entityAddressCity: String, entityAddressState: String, entityAddressCountry: String, entityAddressPostalCode: String): EntityAddress {
    address.entityAddressAddressLine1 = entityAddressAddressLine1
    address.entityAddressAddressLine2 = entityAddressAddressLine2
    address.entityAddressAddressLine3 = entityAddressAddressLine3
    address.entityAddressCity = entityAddressCity
    address.entityAddressState = entityAddressState
    address.entityAddressCountry = entityAddressCountry
    address.entityAddressPostalCode = entityAddressPostalCode
    return address
}
fun updateAdmin(
    adminDetails: EntityAdminDetails,
    entityAdminDetailsFirstName: String,
    entityAdminDetailsMiddleName: String,
    entityAdminDetailsLastName: String,
    entityAdminDetailsEmailId: String,
    entityAdminDetailsMobileNumber: String,
    entityAdminDetailsDepartment: String
): EntityAdminDetails {
    adminDetails.entityAdminDetailsFirstName = entityAdminDetailsFirstName
    adminDetails.entityAdminDetailsMiddleName = entityAdminDetailsMiddleName
    adminDetails.entityAdminDetailsLastName = entityAdminDetailsLastName
    adminDetails.entityAdminDetailsEmailId = entityAdminDetailsEmailId
    adminDetails.entityAdminDetailsMobileNumber = entityAdminDetailsMobileNumber
    adminDetails.entityAdminDetailsDepartment = entityAdminDetailsDepartment
    return adminDetails
}
fun updateBankDetails(bankDetails: EntityBankDetails, entityBankDetailsBankName: String, entityBankDetailsBankAccountNumber: String, entityBankDetailsBankHolderName: String, entityBankDetailsBranchCode: String, entityBankDetailsBranchLocation: String): EntityBankDetails {
    bankDetails.entityBankDetailsBankName = entityBankDetailsBankName
    bankDetails.entityBankDetailsBankAccountNumber = entityBankDetailsBankAccountNumber
    bankDetails.entityBankDetailsBankHolderName = entityBankDetailsBankHolderName
    bankDetails.entityBankDetailsBranchCode = entityBankDetailsBranchCode
    bankDetails.entityBankDetailsBranchLocation = entityBankDetailsBranchLocation
    return bankDetails
}
fun updateContactDetails(
    contactDetails: EntityContactDetails,
    entityContactDetailsFirstName: String,
    entityContactDetailsMiddleName: String,
    entityContactDetailsLastName: String,
    entityContactDetailsEmailId: String,
    entityContactDetailsMobileNumber: String,
    entityContactDetailsDesignation: String,
    entityContactDetailsDepartment: String
): EntityContactDetails {
    contactDetails.entityContactDetailsFirstName = entityContactDetailsFirstName
    contactDetails.entityContactDetailsMiddleName = entityContactDetailsMiddleName
    contactDetails.entityContactDetailsLastName = entityContactDetailsLastName
    contactDetails.entityContactDetailsEmailId = entityContactDetailsEmailId
    contactDetails.entityContactDetailsMobileNumber = entityContactDetailsMobileNumber
    contactDetails.entityContactDetailsDesignation = entityContactDetailsDesignation
    contactDetails.entityContactDetailsDepartment = entityContactDetailsDepartment
    return contactDetails
}
fun updateInfo(
    entityInfo: EntityInfo,
    entityInfoAbbrevation: String,
    entityInfoDescription: String,
    entityInfoLogo: String,
    entityInfoRegion: String,
    entityInfoTimezone: String,
    entityInfoType: String,
    entityInfoDefaultDigitalCurrency: String,
    entityInfoBaseFiatCurrency: String
): EntityInfo {
    entityInfo.entityInfoAbbrevation = entityInfoAbbrevation
    entityInfo.entityInfoDescription = entityInfoDescription
    entityInfo.entityInfoLogo = entityInfoLogo
    entityInfo.entityInfoRegion = entityInfoRegion
    entityInfo.entityInfoTimezone = entityInfoTimezone
    entityInfo.entityInfoType = entityInfoType
    entityInfo.entityInfoDefaultDigitalCurrency = entityInfoDefaultDigitalCurrency
    entityInfo.entityInfoBaseFiatCurrency = entityInfoBaseFiatCurrency
    return entityInfo
}
fun updateOther(
    other: EntityOthers,
    entityOthersCustomerOfflineTxn: String,
    entityOthersMerchantOfflineTxn: String,
    entityOthersApprovalWorkFlow: String,
    entityOthersActivationDate: String
): EntityOthers {
    other.entityOthersCustomerOfflineTxn = entityOthersCustomerOfflineTxn
    other.entityOthersMerchantOfflineTxn = entityOthersMerchantOfflineTxn
    other.entityOthersApprovalWorkFlow = entityOthersApprovalWorkFlow
    other.entityOthersActivationDate = entityOthersActivationDate
    return other
}
private class EmptyLineFilter(
    private val strategy: MappingStrategy< *>
) : CsvToBeanFilter {

    override fun allowLine(line: Array<out String>): Boolean {
        val blankLine = line.size == 1 && line[0].isEmpty()
        return !blankLine
    }
}
