package com.vacuumlabs.wadzpay.exchange

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.vacuumlabs.wadzpay.configuration.CryptoCompareConfiguration
import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import com.vacuumlabs.wadzpay.merchant.model.FiatCurrencyUnit
import com.vacuumlabs.wadzpay.pos.ConversionMarkupRepository
import com.vacuumlabs.wadzpay.services.RedisService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

@Service
class ExchangeService(
    val restTemplate: RestTemplate,
    val cryptoCompareConfiguration: CryptoCompareConfiguration,
    val objectMapper: ObjectMapper,
    val redisService: RedisService,
    val conversionMarkupRepository: ConversionMarkupRepository
) {
    val logger: Logger = LoggerFactory.getLogger(javaClass)
    fun getExchangeRate(to: CurrencyUnit, from: FiatCurrencyUnit): BigDecimal {
        val rates = getExchangeRates(from)

        return rates[to]!!
    }
    fun getExchangeRateFake(to: CurrencyUnit, from: FiatCurrencyUnit): BigDecimal {
        val rates = getExchangeRatesFake(from)

        return rates[to]!!
    }

    fun getExchangeRates(from: FiatCurrencyUnit): Map<CurrencyUnit, BigDecimal> {

        if (checkExchangeRatesPresentCache(from)) {
            return queryCachedExchangeRates(from)
        }

        val response = restTemplate.getForObject(
            UriComponentsBuilder.fromHttpUrl(cryptoCompareConfiguration.baseUrl)
                .path("/data/price")
                .queryParam("fsym", from)
                .queryParam("tsyms", CurrencyUnit.values().joinToString(","))
                .build()
                .toUri(),
            String::class.java
        )

        var result = objectMapper.readValue<MutableMap<CurrencyUnit, BigDecimal>>(response!!)
        // fix for usdca - vijayD
        logger.info("exchange rates : $result")
        if (result.containsKey(CurrencyUnit.USDC)) {
            val usdUnitValue = result[CurrencyUnit.USDC]!!
            result[CurrencyUnit.USDCA] = usdUnitValue
        } else {
            result[CurrencyUnit.USDCA] = BigDecimal.ZERO
        }
        // TODO fix for SART - Anita Prajapati
        result.put(CurrencyUnit.SART, 0.toBigDecimal())
        logger.info("exchange rates sartUnitValue : $result")
        storeExchangeRates(from, result)

        return result
    }

    fun getExchangeRate(from: CurrencyUnit, to: CurrencyUnit): MutableMap<CurrencyUnit, BigDecimal> {
        val response = restTemplate.getForObject(
            UriComponentsBuilder.fromHttpUrl(cryptoCompareConfiguration.baseUrl)
                .path("/data/price")
                .queryParam("fsym", from)
                .queryParam("tsyms", to)
                .build()
                .toUri(),
            String::class.java
        )

        val result = objectMapper.readValue<MutableMap<CurrencyUnit, BigDecimal>>(response!!)
        // fix for usdca - vijayD
        logger.info("exchange rates from to : $result")
        if (result.containsKey(CurrencyUnit.USDC)) {
            val usdUnitValue = result[CurrencyUnit.USDC]!!
            result[CurrencyUnit.USDCA] = usdUnitValue
        } else {
            result[CurrencyUnit.USDCA] = BigDecimal.ZERO
        }
        logger.info("exchange rates from to after usdca : $result")
        return result
    }

    fun getExchangeRatesFake(from: FiatCurrencyUnit): Map<CurrencyUnit, BigDecimal> {

        val response = restTemplate.getForObject(
            UriComponentsBuilder.fromHttpUrl(cryptoCompareConfiguration.baseUrl)
                .path("/data/price")
                .queryParam("fsym", from)
                .queryParam("tsyms", CurrencyUnit.values().joinToString(","))
                .build()
                .toUri(),
            String::class.java
        )

        var result = objectMapper.readValue<MutableMap<CurrencyUnit, BigDecimal>>(response!!)
        result[CurrencyUnit.BTC] = when (from) {
            FiatCurrencyUnit.USD -> {
                (1 / 0.72).toBigDecimal().round(MathContext(8, RoundingMode.UP))
            }
            FiatCurrencyUnit.SGD -> {
                (1 / 1.39).toBigDecimal().round(MathContext(8, RoundingMode.UP))
            }
            FiatCurrencyUnit.AED -> {
                (1 / 2.65).toBigDecimal().round(MathContext(8, RoundingMode.UP))
            }
            FiatCurrencyUnit.EUR -> {
                (1 / 0.72).toBigDecimal().round(MathContext(8, RoundingMode.UP))
            }
            FiatCurrencyUnit.GBP -> {
                (1 / 0.62).toBigDecimal().round(MathContext(8, RoundingMode.UP))
            }
            FiatCurrencyUnit.IDR -> {
                (1 / 10704.70).toBigDecimal().round(MathContext(8, RoundingMode.UP))
            }
            FiatCurrencyUnit.INR -> {
                (1 / 57.40).toBigDecimal().round(MathContext(8, RoundingMode.UP))
            }
            FiatCurrencyUnit.PHP -> {
                (1 / 40.47).toBigDecimal().round(MathContext(8, RoundingMode.UP))
            }
            FiatCurrencyUnit.PKR -> {
                (1 / 158.54).toBigDecimal().round(MathContext(8, RoundingMode.UP))
            }
            FiatCurrencyUnit.SAR -> {
                (1 / 2.71).toBigDecimal().round(MathContext(8, RoundingMode.UP))
            }
            FiatCurrencyUnit.THB -> {
                (1 / 26.38).toBigDecimal().round(MathContext(8, RoundingMode.UP))
            }
            else -> {
                BigDecimal.ONE
            }
        }
        return result
    }

    /* var conversionMarkupData: MutableList<ConversionMarkup> =
         conversionMarkupRepository.findAll() as MutableList<ConversionMarkup>
     var conversionMarkupDataCurrency: MutableList<String> = mutableListOf()
     var conversionMarkupDataPercentage: MutableList<BigDecimal> = mutableListOf()
     fun getMarkupPercentage(cryptoAmount: BigDecimal, asset: CurrencyUnit): BigDecimal {
 //        var conversionMarkupDecimal:ConversionMarkup = conversionMarkupRepository.getByDigitalCurrency(asset.toString())
         for (i in conversionMarkupData.indices) {
             conversionMarkupDataCurrency.add(conversionMarkupData[i].digitalCurrency.toString())
             conversionMarkupDataPercentage.add(conversionMarkupData[i].markUpPercentage)
         }
         var totalMarkUp: BigDecimal = BigDecimal.ZERO
         println(conversionMarkupDataCurrency[1])
         if (conversionMarkupDataPercentage[conversionMarkupDataCurrency.indexOf(asset.toString())] > BigDecimal.ZERO) {
             totalMarkUp =
                 cryptoAmount + (cryptoAmount * conversionMarkupDataPercentage[conversionMarkupDataCurrency.indexOf(asset.toString())])
         }
         totalMarkUp = totalMarkUp.setScale(6, RoundingMode.UP).stripTrailingZeros()
         println(totalMarkUp)
         return totalMarkUp
     }*/

    /*fun getConversionMarkupList(): MutableList<ConversionMarkup>? {
//        val conversionMarkupData = conversionMarkupRepository.findAll().toString()
        var resultConversionMarkupData: MutableList<ConversionMarkup> = mutableListOf()
        resultConversionMarkupData = conversionMarkupRepository.findAll() as MutableList<ConversionMarkup>
        return resultConversionMarkupData
    }*/

    private fun checkExchangeRatesPresentCache(from: FiatCurrencyUnit): Boolean {
        return redisService.getExchangeRatesPresent(from)
    }

    private fun queryCachedExchangeRates(from: FiatCurrencyUnit): Map<CurrencyUnit, BigDecimal> {
        return CurrencyUnit.values().associateWith { redisService.queryExchangeRates(from, it) }
    }

    private fun storeExchangeRates(from: FiatCurrencyUnit, rates: Map<CurrencyUnit, BigDecimal>) {
        redisService.setExchangeRatesPresent(from)

        rates.entries.forEach {
            redisService.storeExchangeRates(from, it.key, it.value)
        }
    }
}
