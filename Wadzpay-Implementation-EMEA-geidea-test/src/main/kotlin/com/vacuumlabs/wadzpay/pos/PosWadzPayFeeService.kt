package com.vacuumlabs.wadzpay.pos

import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class PosWadzPayFeeService {

    fun getRefundDigitalAmountAfterFee(originalDigitalAmount: BigDecimal, digitalAsset: CurrencyUnit): BigDecimal {
        return when (digitalAsset) {
            CurrencyUnit.ETH -> {
                subtractPercentOnETH(originalDigitalAmount)
            }

            CurrencyUnit.USDT -> {
                subtractPercentUSDT(originalDigitalAmount)
            }

            else -> {
                originalDigitalAmount
            }
        }
    }

    private fun subtractPercentUSDT(originalDigitalAmount: BigDecimal): BigDecimal {
        return subtractPercent(originalDigitalAmount, 2)
    }

    private fun subtractPercentOnETH(originalDigitalAmount: BigDecimal): BigDecimal {
        return subtractPercent(originalDigitalAmount, 4)
    }
    private fun addPercentUSDT(originalDigitalAmount: BigDecimal): BigDecimal {
        return subtractPercent(originalDigitalAmount, 2)
    }

    private fun addPercentOnETH(originalDigitalAmount: BigDecimal): BigDecimal {
        return subtractPercent(originalDigitalAmount, 4)
    }
    private fun subtractPercent(originalDigitalAmount: BigDecimal, percent: Int): BigDecimal {
        return originalDigitalAmount - (originalDigitalAmount * percent.toBigDecimal() / (100).toBigDecimal())
    }
    private fun addPercent(originalDigitalAmount: BigDecimal, percent: Int): BigDecimal {
        return originalDigitalAmount + (originalDigitalAmount * percent.toBigDecimal() / (100).toBigDecimal())
    }
}
