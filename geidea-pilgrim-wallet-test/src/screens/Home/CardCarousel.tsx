import React, { useContext, useEffect, useRef } from "react"
import { Animated, FlatList, View, StyleSheet, Dimensions } from "react-native"
import LinearGradient from 'react-native-linear-gradient';

import {
  Container,
  FiatAmount,
  Icon,
  LoadingSpinner,
  spacing,
  theme,
  Typography,
  TypographyColorVariant
} from "~/components/ui"
import { CryptoFullName, Asset, TokenToAmount, FiatSignMap } from "~/constants/types"
import { isIOS ,sartTxt } from "~/utils"
import { UserContext } from "~/context"
import useFormatCurrencyAmount from "~/helpers/formatCurrencyAmount"
import { Image } from "react-native-elements"
import { FiatBalance } from "~/api/models"

const { width } = Dimensions.get("window")

// TODO refactor into separate file based on BE data
// It's ok for now to have it in 1 file
type Card = {
  cryptoFullName: CryptoFullName
  asset: Asset
  tokenImage: React.ReactNode
  cryptoBalance: number
  fiatBalance: number
  colorPrimary: TypographyColorVariant
  colorSecondary: TypographyColorVariant
  backgroundColorPrimary: string
  backgroundColorSecondary: string
}

const getCryptoBalance: (asset: Asset, balances?: TokenToAmount) => number = (
  asset,
  balances
) => Number(balances && balances[asset] ? balances[asset] : 0)

const getFiatBalance: (
  asset: Asset,
  rates?: TokenToAmount,
  balances?: TokenToAmount
) => number = (asset, rates, balances) =>
  Number(
    rates
      ? (
          (balances && balances[asset] ? balances[asset] : 0) / rates[asset]
        ).toFixed(2)
      : 0
  )

const useGetData: (
  exchangeRates?: TokenToAmount,
  balances?: TokenToAmount,
  sarTokens?: any
) => Card[] = (exchangeRates, balances, sarTokens) => {
  return [
   

    {
      cryptoFullName: "Available Balance",
      asset: "SART",
      tokenImage: <Icon name="ETH" size="xl" />,
      cryptoBalance: getCryptoBalance("SART", balances), //sarTokens, // removed older code added for sart
      fiatBalance: getFiatBalance("ETH", exchangeRates, balances),
      colorPrimary: "#4A4A4A",
      colorSecondary: "#0F151B",
      backgroundColorPrimary: theme.colors.crypto.SAR.primary,
      backgroundColorSecondary: theme.colors.crypto.SAR.secondary
    
    }
    // ,
    // {
    //   cryptoFullName: "Tether",
    //   asset: "USDT",
    //   tokenImage: <Icon name="USDT" size="xl" />,
    //   cryptoBalance: getCryptoBalance("USDT", balances),
    //   fiatBalance: getFiatBalance("USDT", exchangeRates, balances),
    //   colorPrimary: "white",
    //   colorSecondary: "white",
    //   backgroundColorPrimary: theme.colors.crypto.USDT.primary,
    //   backgroundColorSecondary: theme.colors.crypto.USDT.secondary
    // },
    // {
    //   cryptoFullName: "WadzPay Token",
    //   asset: "WTK",
    //   tokenImage: <Icon name="WTK" size="xl" />,
    //   cryptoBalance: getCryptoBalance("WTK", balances),
    //   fiatBalance: getFiatBalance("WTK", exchangeRates, balances),
    //   colorPrimary: "white",
    //   colorSecondary: "white",
    //   backgroundColorPrimary: theme.colors.crypto.WTK.primary,
    //   backgroundColorSecondary: theme.colors.crypto.WTK.secondary
    // },
    // {
    //   cryptoFullName: "Bitcoin",
    //   asset: "BTC",
    //   tokenImage: <Icon name="BTC" size="xl" />,
    //   cryptoBalance: getCryptoBalance("BTC", balances),
    //   fiatBalance: getFiatBalance("BTC", exchangeRates, balances),
    //   colorPrimary: "grayLight",
    //   colorSecondary: "white",
    //   backgroundColorPrimary: theme.colors.crypto.BTC.primary,
    //   backgroundColorSecondary: theme.colors.crypto.BTC.secondary
    // },
    // {
    //   cryptoFullName: "Fiat Card",
    //   asset: "AED",
    //   tokenImage: undefined,
    //   cryptoBalance: getCryptoBalance("WTK", balances),
    //   fiatBalance: getFiatBalance("WTK", exchangeRates, balances),
    //   colorPrimary: "white",
    //   colorSecondary: "white",
    //   backgroundColorPrimary: theme.colors.crypto.FIAT.primary,
    //   backgroundColorSecondary: theme.colors.crypto.FIAT.secondary
    // }
  ]
}

const createStyles = (boxWidth: number) =>
  StyleSheet.create({
    list: {
      maxHeight: 220
    },
    listContent: {
      paddingHorizontal: isIOS ? 0 : theme.spacing.md,
      paddingVertical: theme.spacing.md,
      ...(isIOS ? theme.shadow.card : {})
    },
    card: {
      height: "100%",
      width: boxWidth,
      borderRadius: theme.borderRadius.xl,
      padding: theme.spacing.lg,
      ...(!isIOS ? theme.shadow.card : {})
    },
    cardContent: {
      flex: 1
    },
    cardRow: {
      width: "100%"
    },
    controls: {
      width: theme.spacing.md,
      height: theme.borderWidth.md,
      backgroundColor: theme.colors.black,
      marginHorizontal: spacing(0.5)
    },
    cryptoBalanceLoadingContainer: {
      height: theme.fontHeight.subtitle
    }
  })

type Props = {
  exchangeRates: TokenToAmount | undefined
  balances: TokenToAmount | undefined
  isLoading: boolean
  fiatBalances: any,
  primaryColor:string,
  secondaryColor:string
}

const CardCarousel: React.FC<Props> = ({
  exchangeRates,
  balances,
  isLoading,
  fiatBalances, 
   primaryColor=theme.colors.crypto.SAR.primary,
  secondaryColor=theme.colors.crypto.SAR.secondary
}: Props) => {
  const formatter = useFormatCurrencyAmount()
  const { fiatAsset, user , userFiatBalance,  setUserFiatBalance, isBuySelltransactionExist , setBuySelltransactionExist, sarTokens , updateSarTokens} = useContext(UserContext)
 
  const data = useGetData(exchangeRates, balances,sarTokens)
  const [scrollViewWidth, setScrollViewWidth] = React.useState(width)
  const [showLocalBalance, setShowLocalBalance] = React.useState(false)
  
  const boxWidth = scrollViewWidth * 0.85
  const boxDistance = scrollViewWidth - boxWidth - theme.spacing.lg
  const halfBoxDistance = boxDistance / 2
  const scrollX = useRef(new Animated.Value(0)).current
  const position = Animated.divide(scrollX, boxWidth)
  const styles = createStyles(boxWidth)

  const aedFiatBal = fiatBalances?.find((item) => item.fiatasset === "AED")
  // console.log("aedFiatBal ", aedFiatBal, "isBuySelltransactionExist", isBuySelltransactionExist)
  !isBuySelltransactionExist &&  aedFiatBal && aedFiatBal.balance ? setUserFiatBalance(aedFiatBal.balance) : null



    useEffect(() => {

      if(isBuySelltransactionExist) {
        setShowLocalBalance(true)
      } 
         const fetchAPI = setTimeout(() => {
           setShowLocalBalance(false)
           setBuySelltransactionExist(false)
        }, 30000)
        return () => clearTimeout(fetchAPI)
   
  }, [isBuySelltransactionExist])

  const showAedBalancce = () => {

    if(!aedFiatBal?.balance || !userFiatBalance  ||  aedFiatBal?.balance === 0 || Number(userFiatBalance) === 0) {
      return 0
    }
    return showLocalBalance  ? Number(userFiatBalance).toFixed(2) :  Number(aedFiatBal?.balance ).toFixed(2)
  }

  const renderItem = ({ item, index }: { item: Card; index: number }) => {
    return (
      <Animated.View
        style={{
          transform: [
            {
              scale: scrollX.interpolate({
                inputRange: [
                  (index - 1) * boxWidth - halfBoxDistance,
                  index * boxWidth - halfBoxDistance,
                  (index + 1) * boxWidth - halfBoxDistance
                ],
                outputRange: [0.9, 1, 0.9], // scale down when out of scope
                extrapolate: "clamp"
              })
            }
          ]
        }}
      >
        <LinearGradient
          start={{ x: 0, y: 0 }}
          end={{ x: 1, y: 1 }}
          colors={[primaryColor?primaryColor:item.backgroundColorPrimary, secondaryColor?secondaryColor: item.backgroundColorSecondary]}
          style={styles.card}
        >
           {item.cryptoFullName === "Fiat Card" ? (
            <Container
              justify="space-between"
              noItemsStretch
              style={[styles.cardContent]}
            >
              <Image
                source={require("~images/uae.png")}
                style={{
                  width: 50,
                  height: 40,
                  borderRadius:10
                }}
              />
              <Typography
                variant="body"
                fontWeight="bold"
                color={item.colorPrimary}
              >
                AED
              </Typography>


              <Container
                direction="row"
                justify="space-between"
                alignItems="center"
                noItemsStretch
                style={styles.cardRow}
              >

                <Typography variant="subtitle" color={item.colorSecondary}>
                  {FiatSignMap["AED"]} {showAedBalancce() }
                </Typography>
              </Container> 
                
            
            </Container>
          ) :
          <Container
            justify="space-between"
            noItemsStretch
            style={styles.cardContent}
          >
            <Container
              direction="row"
              justify="center"
              alignItems="center"
              noItemsStretch
              style={styles.cardRow}
            >

              <Container spacing={1} alignItems="center" justify="center" noItemsStretch style={{top:10}}>

                {/* ******************* Token Header ******************* */}

                <Container justify="center" alignItems="center" style={{marginHorizontal: 20}} spacing={1} >
                  <Typography variant="heading" color={item.colorPrimary}>
                      {item.cryptoFullName}
                  </Typography>
                </Container>

                {/* ******************* Token Name ******************* */}
                
                <Container justify="center" spacing={1} >
                  <Typography variant="subtitle" color={item.colorPrimary}>

                    {!isLoading ? (
                    <Typography fontFamily="Helvetica-Bold" fontWeight="bold" color={item.colorSecondary} style={{textAlign:"center", fontSize:20}}>

                     
                      {item.asset == "SART" ? sartTxt : item.asset} 
                      {" "}
                      {formatter(item.cryptoBalance, {
                        asset: item.asset
                      })}
                      

                    </Typography>
                  ) : (
                    <View style={styles.cryptoBalanceLoadingContainer}>
                      <LoadingSpinner color="white" />
                    </View>
                  )}

                  </Typography>
                </Container>
              
              </Container>

            </Container>
          </Container> }
        </LinearGradient>
      </Animated.View>
    )
  }

  return (
    <View>
      <FlatList
        horizontal
        data={data}
        style={styles.list}
        contentContainerStyle={styles.listContent}
        contentInsetAdjustmentBehavior="never"
        snapToAlignment="center"
        decelerationRate="fast"
        automaticallyAdjustContentInsets={false}
        showsHorizontalScrollIndicator={false}
        showsVerticalScrollIndicator={false}
        scrollEventThrottle={1}
        snapToInterval={boxWidth}
        contentInset={{
          left: halfBoxDistance,
          right: halfBoxDistance
        }}
        contentOffset={{ x: halfBoxDistance * -1, y: 0 }}
        onLayout={(e) => {
          setScrollViewWidth(e.nativeEvent.layout.width)
        }}
        onScroll={Animated.event(
          [{ nativeEvent: { contentOffset: { x: scrollX } } }],
          {
            useNativeDriver: false
          }
        )}
        keyExtractor={(item, index) => `${index}-${item}`}
        renderItem={renderItem}
      />
      {/* <Container direction="row" justify="center" noItemsStretch>
        {data.map((_, index) => {
          const opacity = position.interpolate({
            inputRange: [index - 1, index, index + 1],
            outputRange: [0.2, 0.6, 0.2],
            extrapolate: "clamp"
          })
          return (
            <Animated.View
              key={index}
              style={{ opacity, ...styles.controls }}
            />
          )
        })}
      </Container> */}
    </View>
  )
}

export default CardCarousel
