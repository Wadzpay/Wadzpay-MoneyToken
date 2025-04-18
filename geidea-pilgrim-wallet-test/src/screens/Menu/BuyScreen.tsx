import React, { useContext, useEffect, useState } from "react"
import { useTranslation } from "react-i18next"
import {
  Alert,
  TouchableOpacity,
  StyleSheet,
  View,
  Dimensions,
  TextInput,
} from "react-native"
import { NativeStackScreenProps } from "@react-navigation/native-stack"


import { MenuStackParamList } from "~/components/navigators"
import {
  Typography,
  ErrorModal,
  LoadingSpinner,
  Container,
  ScreenLayoutTab,
  theme,
  Icon,
  Modal,
  AvailableBalance,
  FiatAmount,
  Button
} from "~/components/ui"
import {
  useBuyDigitalCurrency,
  useGetFiatBalance,
} from "~/api/user"
import { UserContext } from "~/context"
import { calculateTimeLeft, isProd,sartTxt } from "~/utils"
import Withdraw from "~/icons/Withdraw"
import { useGetExchangeRate } from "~/api/onRamp"
import { Image, Tooltip } from "react-native-elements"
import { Dropdown } from "react-native-element-dropdown"
import { Asset, FiatSignMap } from "~/constants/types"
import { AssetFractionDigits } from "~/api/constants"
import { useAssetFractionDigits } from "~/helpers/formatCurrencyAmount"
import { EXCHANGE_RATES_REFRESH_SEC, EXCHANGE_RATES_REFRESH_TIME, ResetPasswordForm, TIMER_MINUTES, useValidationSchemas } from "~/constants"
import { addMinutes, addSeconds } from "date-fns"
import { useForm } from "react-hook-form"
import { yupResolver } from "@hookform/resolvers/yup"
import Clipboard from '@react-native-clipboard/clipboard';

const { width } = Dimensions.get("window")

type Props = NativeStackScreenProps<MenuStackParamList, "BuyScreen">

const BuyScreen: React.FC<Props> = ({ navigation }: Props) => {
  const { t } = useTranslation()
  const [amount, setAmount] = useState("")
  const [value, setValue] = useState<{ value: any }>("AED")
  const [valueRight, setValueRight] = useState<{ valueRight: string  }>("ETH")
  const [isFocus, setIsFocus] = useState(false)
  const [showFiatArea, setshowFiatArea] = useState(true)
  const [fiatLimitExceed, setfiatLimitExceed] = useState(false)
  const [isValidationError, setIsValidationError] = useState(false)
  const [isValidationErrorTextInput, setIsValidationErrorTextInput] = useState(false)
  const [errorMessage, setErrorMessage] = useState("")
  const [errorMessageTextInput, setErrorMessageTextInput] = useState("")
  const [fetchBestPrice, setFetchBestPrice] = useState(false)
  const {setBuySelltransactionExist } = useContext(UserContext)
  const [confirmClicked, setConfirmClicked] = useState(false)


    const {
      data: fiatBalancesData,
      isFetching: isFetchingfiatBalancesData,
      isSuccess: isSuccessfiatBalancesData,
      refetch: refetchfiatBalancesData,
      isFetching: isFetchingBalance,
    } = useGetFiatBalance()

  const {
    mutate: buyDigitalCurrency,
    isLoading: isbuyDigitalCurrencyLoading,
    error: buyDigitalCurrencyError,
    isSuccess: isbuyDigitalCurrencySuccess,
    data: buyDigitalCurrencyData
  } = useBuyDigitalCurrency()

  const { 
    data: exchangeRatesData,
    refetch: refetchExchangeRates ,
    isFetching: isFetchingExhchangeRates,
    isSuccess: isExchangeRateSuccess
  } = useGetExchangeRate(value)
const fiatBalanceAed =    fiatBalancesData?.find((item) => item.fiatasset === "AED")

const isFiatAmountAvailable = (amount) =>  {
  return fiatBalanceAed && fiatBalanceAed?.balance >= amount
}

const equivalentAmountCryptoToFiat =  exchangeRatesData ? Number(amount) / exchangeRatesData[valueRight] : 0 
const isEquivalentfiatAvailable = (fiatAmount) => {
  // console.log("iatBalanceAed && fiatBalanceAed.balance >= fiatAmount ",  fiatAmount)
  return fiatBalanceAed && fiatBalanceAed.balance >= fiatAmount
}
const [targetDate, setTargetDate] = useState(
  addSeconds(new Date(), 30)
)
const [timeLeft, setTimeLeft] = useState(30)


useEffect(() => {
  const timer = setTimeout(() => {
    setTimeLeft(calculateTimeLeft(targetDate))
  }, 1000)
  return () => clearTimeout(timer)
})

useEffect(() => {
  const interval = setInterval(() => {
    refetchExchangeRates()
    setTargetDate(addSeconds(new Date(),30))
    setTimeLeft(30)
  }, EXCHANGE_RATES_REFRESH_TIME); //30000 - 30 sec
  return () => clearInterval(interval);
}, []);





const callAPI = (amountCryptoToFiat: number) => {
  // console.log("her her herer herer")
  if(showFiatArea) { 
    !isValidationError && 
    !fiatLimitExceed &&
    !isValidationErrorTextInput && 
    buyDigitalCurrency({
      fiatAmount:  Number(amount).toFixed(2),
      fiatAsset: value,
      digitalAsset: valueRight,
})
  } else { 
    !isValidationError && 
    !fiatLimitExceed &&
    !isValidationErrorTextInput && 
    buyDigitalCurrency({
      fiatAmount:  Number(amountCryptoToFiat).toFixed(2),
      fiatAsset: value,
      digitalAsset: valueRight,
})
  }
       
}
  const onConfirm = () => {
    // console.log("api is loading : ",isbuyDigitalCurrencyLoading)
    // console.log("calling API" , amount,showFiatArea)
    let amountCryptoToFiat 
    if(!showFiatArea) {
      amountCryptoToFiat =  Number(amount) / exchangeRatesData[valueRight]
    } else {
      amountCryptoToFiat = Number(amount)
    }
    if(!amount) {
      setIsValidationError(true) 
      setErrorMessage("Enter Amount")
    }
    else if (amount.toString().includes(".") && amount.toString().split(".").length>2) {
      setIsValidationErrorTextInput(true) 
      setErrorMessageTextInput("Decimals allowed only once")
    }
    else if (isValidationErrorTextInput == true) {
      setErrorMessage("Special characters are not allowed except decimal")
    } 
    else if(showFiatArea && !isFiatAmountAvailable(amount)) { 
        // avaialble balance in ETH  which is equivalent to enter aed
        // console.log("available balances" )
        setIsValidationError(true) 
        setErrorMessage("Insufficient Funds to Execute Buy")
      } 
    else if(showFiatArea && amount && amount.toString().includes(".") && amount.toString().split(".")[1].length > 2 ) {
        // 2 decimal points in AED 
        // console.log("2 decimal " )
        setIsValidationErrorTextInput(true) 
        setErrorMessageTextInput("Amount allowed till 2 decimal places only")
      } 
    else if(showFiatArea && amount < 10) {
        // recived AED of worth ETH should range  10
        // console.log("fiatLimitExceed 10" , fiatLimitExceed )
        setfiatLimitExceed(true)
        setIsValidationError(false) 
        setIsValidationErrorTextInput(false) 
      } 
    else if(showFiatArea && amount > 5000) {
        // recived AED of worth ETH should range  5000
        // console.log("fiatLimitExceed 5000", fiatLimitExceed)
        setfiatLimitExceed(true)
        setIsValidationError(false) 
        setIsValidationErrorTextInput(false) 
      }
    else if(!showFiatArea && !isFiatAmountAvailable(amountCryptoToFiat)) { 
        // available balance of digital currency
        // console.log("available balances in Digital currency" )
        setIsValidationError(true) 
        setErrorMessage("Insufficient Funds to Execute Buy")
      } 
    else if (!showFiatArea && amount && amount.toString().includes(".") && amount.toString().split(".")[1].length > 8) {
        // 8 decimal points for  digital currency
        // console.log("8 decimal in Digital currency" )
        setIsValidationErrorTextInput(true) 
        setErrorMessageTextInput("Amount allowed till 8 decimal places only")
      }
    else if(!showFiatArea && amountCryptoToFiat < 10 ) {
        // equivalent aed of entered Digital currency should range in between 10 to 5000
        // console.log("10 aed  in Digital currency" )
        setfiatLimitExceed(true)
        setIsValidationError(false) 
        setIsValidationErrorTextInput(false)
      } 
    else if(!showFiatArea && amountCryptoToFiat > 5000 ) {
        // equivalent aed of entered Digital currency should range in between 10 to 5000
        // console.log("5000 aed in Digital currency" )
        setfiatLimitExceed(true)
        setIsValidationError(false) 
        setIsValidationErrorTextInput(false)
      }
    else if(confirmClicked == false){
      setConfirmClicked(true)
      setIsValidationErrorTextInput(false)
      setIsValidationError(false) 
      setfiatLimitExceed(false)
      callAPI(amountCryptoToFiat)
    }
  }



  useEffect(() => {
    // console.log("here error")
    if (buyDigitalCurrencyError) {
      setAmount("")
      navigation.navigate("BuySellReciptScreen",{
        title: "Buy",
        uuid : "",
        transactionType : "Buy" ,
        from : `${valueRight}`,
        to: "AED" ,
        totalAmount: showFiatArea? equivalentAmountCryptoToFiat.toString() : amount ,
        createdAt: "",
        status: "Failed",
        description : buyDigitalCurrencyError?.message || "Server Request Failed",
        statusHeader:"Transaction Failed"
      })
    }
  }, [buyDigitalCurrencyError])

  useEffect(() => {
    // console.log("here buy ")
    setAmount("")
    if (buyDigitalCurrencyData) {
      let response = buyDigitalCurrencyData
      setBuySelltransactionExist(true)
      navigation.navigate("BuySellReciptScreen",{
        title: "Buy",
        uuid : response?.uuid,
        transactionType : response?.transactionType ,
        from : `${valueRight}`,
        to: "AED" ,
        totalAmount: response?.totalAmount ,
        createdAt: response?.createdAt,
        status: response?.status,
        description:response?.description,
        statusHeader:"Transaction Successful"
      })
    // refetchfiatBalancesData()
    }
  }, [isbuyDigitalCurrencySuccess])


  const data = [
   
    { label: "ETH", value: "ETH", isSelected: true },
    { label: "USDT", value: "USDT" , isSelected: false },
    { label: "BTC", value: "BTC", isSelected: false  },
    { label: "WTK", value: "WTK" , isSelected: false },
    { label: sartTxt, value: "SART" , isSelected: false }
    // { label: "XSGD", value:"XSGD"}
  ]

  const [dropdownData, setDropDownData] = useState(data)
  let modifiedData: any = []
  const dataFiat = [
    { label: "AED", value: "AED" }
  ]
  const renderItem = (item: any) => {
    return (
      <Container direction="row" alignItems="flex-start" noItemsStretch justify="flex-start"  spacing={1}style={styles.item}>
        <Icon style={styles.icon} name={item.value} size="md" />
        <Typography variant="label"  fontFamily="helvetica" color="darkBlack">{item.label}</Typography>
      </Container>
    )
  }

  const getFlagImageFait = (value: any) => {
  
      return require("~images/uae.png")
    
  }
  const renderItemFiat = (item: any) => {
    return (
      <View style={styles.item}>
        <Image
          source={getFlagImageFait(item.value)}
          style={{ margin: 2, width: 25, height: 20 }}
        />
        <Typography  fontFamily="helvetica" variant="label">{item.label}</Typography>
      </View>
    )
  }


  const getBestPrice = () => {
    let cryptoToFiat = exchangeRatesData
      ? Number(amount) / exchangeRatesData[valueRight]
      : "Fetching best prices"
    return Number(cryptoToFiat).toFixed(2)
  }

  const showFiatTocrypto = () => {
    let fiatToCrypto = exchangeRatesData
       ? amount * exchangeRatesData[valueRight]
       : 0
       return `${Number(fiatToCrypto).toFixed(8)}`
   }
  const getconversionRate = () => {
    // let valueInFiat = getBestPrice()
    let cryptoToFiat = exchangeRatesData
      ? 1 / exchangeRatesData[valueRight]
      : " "
    return `${Number(cryptoToFiat).toFixed(2)} ${value}`
  }
  const getconversionRateInCrypto = () => {
    let fiatToCrypto = exchangeRatesData
      ? 1 * exchangeRatesData[valueRight]
      : " "
    return `${Number(fiatToCrypto).toFixed(8)} ${valueRight}`
  }

  const tooltipItem = () => {
    return (
      <Tooltip
      height={300}
      width={300}
        backgroundColor={"#9ac2df"}
        popover={
          <Typography fontFamily="Montserrat-Regular" color="darkBlack">
            {t(
              "Your transactions will incur a network charge. In the event there are insufficient funds in the form of inadequate ETH balance in your account, please note that the transaction will not be successfully processed. Please ensure that there is sufficient balance in your account. CBD shall not be liable for any unsuccessful transactions or loss of opportunities or any kind of losses whatsoever, in the event of such an occurrence."
            )}
          </Typography>
        }
      >
        <Icon  style={styles.icon} name={"Info"} size="xxs" />
      </Tooltip>
    )
  }

  const onDisMissClick = () => {
    navigation.goBack()
  }

  const onReverseClick = () => {
    if (showFiatArea) {
      setshowFiatArea(false)
    } else {
      setshowFiatArea(true)
      
    }
    setfiatLimitExceed(false)
    setIsValidationError(false)
  }
  return (
    <ScreenLayoutTab
      title={t("Buy")}
      leftIconName="ArrowLeft"
      onLeftIconClick={onDisMissClick}
    >
      <Container spacing={4} style={styles.container}>
       <Container style={{marginTop:10}} alignItems="center" noItemsStretch justify="center">
       {isFetchingfiatBalancesData ? 
       <Container alignItems="center" noItemsStretch>
            <LoadingSpinner color="orange" />
        </Container> 
        : <AvailableBalance 
       fiatAsset={"AED"}
       fiatBalancesData={fiatBalancesData}
        isAmountAvailable={isFiatAmountAvailable(showFiatArea ? amount :  Number(amount) / exchangeRatesData[valueRight] ) }
        variant={"body"}>
        </AvailableBalance> }
       </Container>
        <Container
          justify="center"
          alignItems="center"
          noItemsStretch
          spacing={1}
        >
          <Container alignItems="center"  justify="flex-start" noItemsStretch spacing={2} >
          <Container direction="row" alignItems="center" justify="flex-start" noItemsStretch style={{
            borderColor: "#707070",
            borderWidth: 0.25,
            borderRadius: 10,
            // paddingVertical: 2,
            paddingHorizontal:10,
            height:40,
            width:190
          }}>
           {showFiatArea ? <Typography style={{width:35 }} variant="button" fontFamily="Montserrat-Regular">{FiatSignMap[value]}</Typography> : <Icon style={{width:35 }} name={valueRight} size="lg" /> }
            <TextInput
              value={amount}
              placeholder="Enter Amount"
              keyboardType="numeric"
              contextMenuHidden={true} 
              onFocus={() => Clipboard.setString('')} 
              onSelectionChange={() => Clipboard.setString('')}
              onChangeText={(newText) => {
                if (newText.includes(",") || newText.includes(" ") || newText.includes("+") || newText.includes("-")) {
                  // console.log("not allowed")
                  setIsValidationErrorTextInput(true)
                  setIsValidationError(false) 
                  setErrorMessageTextInput("Special characters are not allowed except decimal")
                } else {
                  setAmount(newText)
                  setIsValidationErrorTextInput(false)
                  setIsValidationError(false)
                  setfiatLimitExceed(false)
                }
              } }
              defaultValue={amount}
             style={{width: 190, fontFamily: "Montserrat-Light"}}
            />
            
          </Container>

        <Container style={{marginTop:-10}}>
           {/* for placeholderc text */}
          <Typography variant="chip" style={{marginTop:0}} fontFamily="HelveticaNeue-Medium" color={fiatLimitExceed ? "error" : "grayLight"}>Spend amount should be between 10 to 5000 AED</Typography>

          {isValidationErrorTextInput ? <Typography style={{marginTop:10}} fontFamily="HelveticaNeue-Medium" variant="chip" color="error">{errorMessageTextInput}</Typography> : undefined}
          {isValidationError ? <Typography style={{marginTop:10}} fontFamily="HelveticaNeue-Medium" variant="chip" color="error">{errorMessage}</Typography> : undefined}
        </Container>
          
          </Container>
          <TouchableOpacity onPress={() => onReverseClick()} style={styles.imageView}>
          <Image source= {require("~images/arrow.png")} style={styles.image} />
          </TouchableOpacity>
          <Typography variant="body" fontFamily="helvetica" color="darkBlack">
          {amount? showFiatArea ? `You will receive ${showFiatTocrypto()} ${valueRight}`: `You will spend ${FiatSignMap[value]} ${getBestPrice()}` : `Fetching Best Price...`}
        </Typography>
        </Container>
        
        <Container
          direction="row"
          alignItems="center"
          justify="space-around"
          noItemsStretch
         
        >
          <Dropdown
           style={[styles.dropdown , styles.shadowProp]}
            placeholderStyle={styles.placeholderStyle}
            selectedTextStyle={styles.selectedTextStyle}
            inputSearchStyle={styles.inputSearchStyle}
            iconStyle={{width:0,height:0}}
            disable
            data={dataFiat}
            search={false}
            maxHeight={300}
            labelField="label"
            valueField="value"
            placeholder={!isFocus ? "Select item" : "..."}
            searchPlaceholder="Search..."
            value={value}
            onFocus={() => setIsFocus(true)}
            onBlur={() => setIsFocus(false)}
            onChange={(item) => {
              setValue(item.value)
              setIsFocus(false)
              setFetchBestPrice(true)
            }}
            renderLeftIcon={() => (
              <Image
                source={getFlagImageFait(value)}
                style={{ marginLeft: 25, marginRight:5, width: 20, height: 10 }}/>
            )}
            renderItem={renderItemFiat}
          />
          <Icon name={"ArrowRight"} color="black" size="md" />
          <Dropdown
            style={[styles.dropdown , styles.shadowProp]}
            placeholderStyle={styles.placeholderStyle}
            selectedTextStyle={styles.selectedTextStyle}
            inputSearchStyle={styles.inputSearchStyle}
            containerStyle={[styles.dropdownListStyle]}
            iconStyle={styles.iconStyle}
            data={dropdownData}
            statusBarIsTranslucent={true}
            search={false}
            maxHeight={300}
            labelField="label"
            valueField="value"
            placeholder={valueRight}
            searchPlaceholder="Search..."
            value={valueRight}
            onFocus={() => setIsFocus(true)}
            onBlur={() => setIsFocus(false)}
             onChange={(item) => {
              modifiedData = []
              data.forEach((ele) => {
                if (item.value !== ele.value) {
                  modifiedData.push(ele)
                } 
              })
              // console.log("modifiedData ", modifiedData)
              setDropDownData(modifiedData)
              // console.log("dropdownData ", dropdownData)
              setValueRight(item.value)
              setIsFocus(false)
              setFetchBestPrice(false)
            }}
            renderLeftIcon={() => (
              <Icon style={styles.icon} name={valueRight} size="md" />
            )}
            renderItem={renderItem}
          />
        </Container>

        <Container
          style={{ marginVertical:10,  marginHorizontal: 30 }}
          alignItems="flex-start"
          justify="flex-start"
          noItemsStretch
        > 
        {isFetchingExhchangeRates ?  
        <Container alignItems="center" noItemsStretch>
        <LoadingSpinner color="orange" />
         </Container> : 
          <Typography style={{ margin: 2 }} fontFamily={"Montserrat-Regular"} variant="label" color="darkBlack">
           {showFiatArea  || !isFetchingExhchangeRates? `1 ${value} ~ ${getconversionRateInCrypto()}`: `1 ${valueRight} ~ ${getconversionRate()}`}
          </Typography> }
          <Typography
          fontFamily="Montserrat-LightItalic"
            style={{ margin: 2}}
            variant="chip"
            color="darkBlack"
          >
            {t("*Network Fee Disclaimer")}
            {tooltipItem()}
          </Typography>
        </Container>

        <Container alignItems="center" justify="center" noItemsStretch spacing={1}>
        {timeLeft > 0 ? <Typography fontFamily="HelveticaNeue-Light" variant="chip" color="darkBlack">
            Rates will be valid for {timeLeft} seconds
          </Typography> : <Typography fontFamily="HelveticaNeue-Light" variant="chip" color="darkBlack">

          </Typography> }
          <Button
          style={{
            height:65,
            alignItems: "center",
            justifyContent: "center",
            borderColor: theme.colors.transparent,
            borderRadius: 15,
            borderWidth: 2,
            paddingHorizontal: 100,
          }}
            text={"Buy"}
            fontWeight="bold"
            fontFamily={"Helvetica-Bold"}
            loading={isbuyDigitalCurrencyLoading}
            disabled = {isbuyDigitalCurrencyLoading ? true : false}
            onPress={() => {
              onConfirm()
            }}
          />
    </Container>
       
      </Container>
    </ScreenLayoutTab>
  )
}

export default BuyScreen

const styles = StyleSheet.create({
  container: {
    backgroundColor: "white",
    paddingHorizontal: theme.spacing.md,
    paddingVertical: theme.spacing.xs,
    marginVertical: theme.spacing.xs,
  },
  dropdown: {
    width: 130,
    height: 50,
    borderColor: theme.colors.transparent,
    backgroundColor: "#F7FBFE",
    borderWidth: 0.5,
    borderRadius: 35,
    paddingHorizontal: 6
    
  },
  dropdownListStyle:{
    borderColor: theme.colors.transparent,
    backgroundColor: "#F0F7FD",
    borderWidth: 0.5,
    borderRadius: 20,
    paddingHorizontal: 21,
    shadowColor: theme.colors.gray.dark,
    shadowOffset: {width: -2, height: 4},
    shadowOpacity: 0.2,
    shadowRadius: 1,
    
  },
  shadowProp: {
    shadowColor: theme.colors.gray.dark,
    shadowOffset: {width: -2, height: 4},
    shadowOpacity: 0.2,
    shadowRadius: 1,
    elevation: 20,
  },
  placeholderStyle: {
    fontSize: theme.fontSize.label,
    fontFamily: "helvetica"
  },
  selectedTextStyle: {
    fontSize: theme.fontSize.label,
    fontFamily: "helvetica"
  },
  iconStyle: {
    width: 25,
    height: 25
  },
  inputSearchStyle: {
    height: 40,
    fontSize: theme.fontSize.body,
    fontFamily: "helvetica"
  },
  icon: {
    marginLeft: 1, marginRight:5
  },
  item: {
   marginVertical:5,
  },
  textItem: {
    flex: 1,
    fontSize: 16
  },
  image: {
    margin:10,
    width:30,
    height:30
  },
  imageView:{
    justifyContent: "center",
    alignItems: "center", 
  }
})


