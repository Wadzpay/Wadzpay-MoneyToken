import React, { useContext, useEffect } from "react"

import Typography from "./Typography"
import { TypographyColorVariant, TypographyVariant } from "./theme"

import useFormatCurrencyAmount from "~/helpers/formatCurrencyAmount"
import { Asset, FiatAsset, FiatSignMap, FiatTokenToAmount, TokenToAmount } from "~/constants/types"
import Container from "./Container"
import { AssetFractionDigits } from "~/api/constants"
import { useTranslation } from "react-i18next"
import { FiatBalanceData } from "~/api/models"
import { UserContext } from "~/context"

type AvailableBalanceProps = {
  fiatBalancesData?: FiatBalanceData
  fiatAsset?: FiatAsset,
  assetSign?:Asset,
  digitalBalancesData? : TokenToAmount
  variant?: TypographyVariant
  color?: TypographyColorVariant,
  isAmountAvailable?:boolean
}

const AvailableBalance: React.FC<AvailableBalanceProps> = ({
  fiatBalancesData,
  fiatAsset,
  digitalBalancesData,
  assetSign,
  variant,
  color,
  isAmountAvailable
}: AvailableBalanceProps) => {
  const fiatSign = FiatSignMap[fiatAsset || "AED"]
  const { t } = useTranslation()
  const { isBuySelltransactionExist , setBuySelltransactionExist, setUserFiatBalance, userFiatBalance} = useContext(UserContext)
  const [showLocalBalance, setShowLocalBalance] = React.useState(false)
  const fiatBalance =    fiatBalancesData?.find((item) => item.fiatasset === fiatAsset)

  !isBuySelltransactionExist &&  fiatBalance && fiatBalance.balance ? setUserFiatBalance(`${fiatBalance?.balance}`) : null

  useEffect(() => {
    //console.log("isBuySelltransactionExist ", isBuySelltransactionExist)
    if(isBuySelltransactionExist) {
      setShowLocalBalance(true)
    } else {
       const fetchAPI = setTimeout(() => {
        //console.log("here enabling local bal in available bal")
         setShowLocalBalance(false)
         setBuySelltransactionExist(false)
      }, 30000)
      return () => clearTimeout(fetchAPI)
    }
}, [isBuySelltransactionExist])


const showAedBalancce = () => {

  if(!fiatBalance?.balance || !userFiatBalance  ||  fiatBalance?.balance === 0 || Number(userFiatBalance) === 0) {
    return 0
  }
return showLocalBalance  ? Number(userFiatBalance).toFixed(2) :  Number(fiatBalance?.balance).toFixed(2)
}

  // console.log("digitalBalancesData ", digitalBalancesData)
  // // console.log("fiatBalancesData ", fiatBalancesData)
  // console.log("fiatAsset ", fiatAsset)
  // console.log("assetSign ", assetSign)
  // console.log("variant ", variant)
  //console.log("isAmountAvailable ", isAmountAvailable)

 


  return (
    <Container direction="row">
    <Typography
      color={isAmountAvailable ? "darkBlack" : "error"}
      variant= {variant}
     // fontWeight={"bold"}
      fontFamily={"Montserrat-Regular"}
    >{t("Available balance")} = </Typography>
      {
        digitalBalancesData && 
        (
          <Typography
            variant={variant}
            //fontWeight={"bold"}
            fontFamily={"Montserrat-Regular"}
            color={isAmountAvailable ? "darkBlack" : "error"}
          >
            {Number(digitalBalancesData[assetSign == "XSGD" ? "BTC" : assetSign]).toFixed(
              AssetFractionDigits[assetSign == "XSGD" ? "BTC" : assetSign]
            )}{" "}{assetSign}
          </Typography>
        )
      }
      {
        fiatBalance && 
        (
          <Typography
            variant={variant}
            // fontWeight={"bold"}
            color={isAmountAvailable ? "darkBlack" : "error"}
            fontFamily={"Montserrat-Regular"}
          >
            {fiatAsset}{" "}{showAedBalancce()}
          </Typography>
        )
      }
    
  </Container>
)}

export default AvailableBalance
