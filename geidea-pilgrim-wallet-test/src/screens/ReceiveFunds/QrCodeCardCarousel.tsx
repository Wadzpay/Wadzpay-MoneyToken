import React, { useContext, useEffect, useState } from "react"
import { View, StyleSheet, Dimensions } from "react-native"

import {
  Container,
  Typography,
  spacing,
  theme,
} from "~/components/ui"
import { Asset } from "~/constants/types"
import { isIOS } from "~/utils"
import { UserContext } from "~/context"
import QRCode from "react-native-qrcode-svg"
import { useGetEncryptedData } from "~/api/user"

const { width } = Dimensions.get("window")


const createStyles = (boxWidth:number) =>
  StyleSheet.create({
    list: {
      maxHeight: 380
    },
    listContent: {
      paddingHorizontal: isIOS ? 20 : theme.spacing.md,
      paddingVertical: theme.spacing.md,
      ...(isIOS ? theme.shadow.card : {})
    },
    card: {
      height: "100%",
      width: boxWidth-70,
      borderRadius: 10,
      shadowColor: theme.colors.gray.light,
      shadowOffset: {width: -2, height: 1},
      shadowOpacity: 0.6,
      shadowRadius: 6,
      elevation: 6,
      marginLeft: 5, 
      marginRight: 5
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
  walletAddress: string
  amount: any
  digitalAsset: Asset
}

const QrCodeCardCarousel: React.FC<Props> = ({
  walletAddress,
  amount,
  digitalAsset
}: Props) => {
   const [scrollViewWidth, setScrollViewWidth] = React.useState(width)
  const boxWidth = scrollViewWidth * 0.95
  const styles = createStyles(boxWidth)
  const { user } = useContext(UserContext)
  const [encryptedText, setEncryptedText] = useState("")


  const {
    mutate: getEncryptedData,
    isLoading: isGetEncryptedDataLoading,
    error: getEncryptedDataError,
    isSuccess: isGetEncryptedDataSuccess,
    data: getEncryptedSuccessData
  } = useGetEncryptedData()



  useEffect(() => {
    if (isGetEncryptedDataSuccess && getEncryptedSuccessData) 
     {
      let response = getEncryptedSuccessData 
      setEncryptedText(response)
    }
  }, [isGetEncryptedDataSuccess, getEncryptedSuccessData])

  useEffect(() => {
    getEncryptedData({
      data: `transactionID:00000|blockchainAddress:${walletAddress}|type:SART|transactionAmount:0|merchantId:000000|posId:000000|merchantDisplayName:${user?.attributes?.email}`
    })
  }, [])

  let logoFromFile = require('../../../assets/icon.png');
  
  return (
    <View>
       <Container
            alignItems="center"
            justify="center"
            noItemsStretch
          //  style={styles.cardContent}
          >
            <Container
              direction="row"
              justify="space-between"
              alignItems="center"
              noItemsStretch
              //style={styles.cardRow}
            >
              {encryptedText.length > 0 ? (<QRCode
              // swati test
                value={encryptedText}
                size={250}
                color="black"
                logo={logoFromFile}
                logoBackgroundColor="transparent"
                linearGradient={['rgb(255,120,0)','rgb(255,140,0)']}
                enableLinearGradient={false}
                backgroundColor="white"
              />):(<Typography>Loading...</Typography>)}
            </Container>
          </Container>
    </View>
  )
}

export default QrCodeCardCarousel
