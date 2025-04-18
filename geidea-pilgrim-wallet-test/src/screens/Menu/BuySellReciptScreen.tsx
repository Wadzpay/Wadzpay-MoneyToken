import React, { useEffect, useRef , useState} from "react"
import { useTranslation } from "react-i18next"
import { NativeStackScreenProps } from "@react-navigation/native-stack"

import {
  StyleSheet,
  Image,
  TouchableOpacity,
  Dimensions,
  View,
  BackHandler
} from "react-native"
import { CommonActions, RouteProp } from "@react-navigation/native"
import ViewShot, { captureScreen } from "react-native-view-shot"

import Clipboard from '@react-native-clipboard/clipboard';
import Share from 'react-native-share';

import {
    MenuStackParamList,
  SendFundsStackNavigatorParamList,
  TransactionStackNavigatorParamList
} from "~/components/navigators"
import {
  Button,
  Container,
  FiatAmount,
  Icon,
  ScreenLayoutTab,
  theme,
  Typography
} from "~/components/ui"
import { useUserTransaction } from "~/api/user"
import useFormatCurrencyAmount from "~/helpers/formatCurrencyAmount"
import { isIOS, showToast } from "~/utils"
import { FiatSignMap } from "~/constants/types"

const { width } = Dimensions.get("window")
const containerMargin = theme.spacing.md
const rowWidth =
  width - containerMargin * 2 - theme.iconSize.md - theme.spacing.xs

const styles = StyleSheet.create({
  container: {
    flex: 1,
    marginHorizontal: theme.spacing.lg,
    marginTop: theme.spacing.xs,
    marginBottom: theme.spacing.lg
  },
  image: {
    width:90,
    height:100,
    marginTop: theme.spacing.xs,
    marginBottom: theme.spacing.xs
  },
  buttonContainer: {
    marginHorizontal: theme.spacing.md
  },
  wadz_pay_logo: {
    marginTop: theme.spacing.xs,
    marginBottom: theme.spacing.xs
  },
  row: {
    width: rowWidth
  },
  separator: {
    marginTop: 4,
    marginBottom: 5,
    width: rowWidth,
    height: theme.borderWidth.xs,
    backgroundColor: theme.colors.gray.light
  }
})

type Props = {
  navigation: NativeStackScreenProps<
    TransactionStackNavigatorParamList,
    "Transactions"
  >
  route: RouteProp<MenuStackParamList, "BuySellReciptScreen">
}

// type Props = StackScreenProps<
//   SendFundsStackNavigatorParamList,
//   "PaymentSummary"
// >

const BuySellReciptScreen: React.FC<Props> = ({ navigation, route }: Props) => {
  const [shareRecipt, setShareRecipt] = useState(false)
  const [backButtonPressed, setBackButtonPressed] = useState(false)
  const [imageURI, setImageURI] = useState('')

  const formatter = useFormatCurrencyAmount()
  const { t } = useTranslation()
    const { title, uuid, transactionType, from, to, totalAmount, createdAt, status, description, statusHeader } = route.params
  
  const date = new Date(createdAt)
  const viewShotRef = useRef()
  const copyToClipboard = (value: string) => {
    /// console.log(value)
    Clipboard.setString(value)
    isIOS ? alert(`Copied!`) : showToast("Copied!")
  }



  const onDone = (): any => {
    console.log("test")
    if(backButtonPressed == false) {
      console.log("testing")
      setBackButtonPressed(true)
      navigation.dispatch(
        CommonActions.reset({
          index: 0,
          routes: [
            {
              name: "Menu"
            }
          ]
        })
      )
    }
  }

  useEffect(() => {
    function handleBackButton() {
      onDone()
      return true
    }

    const backHandler = BackHandler.addEventListener(
      "hardwareBackPress",
      handleBackButton
    )

    return () => backHandler.remove()
  }, [navigation])
  
  let isFailedTransaction = status === "Failed" ? true : false
 

  const onShareClick = () => {
    setShareRecipt(true)
    setTimeout(() => {
      console.log("here time out")
      captureViewShot()
    }, 80)
  }

  async function captureViewShot() {
   // const imageURI = await viewShotRef.current.capture()
    viewShotRef.current.capture().then(uri => {
      console.log('do something with ', uri);
      setImageURI(uri);
    });
    // console.log(imageURI)
    const options = {
      url: imageURI,
      message: 'recipt',
    };
    await Share.open(options);
    setShareRecipt(false);

  }

  useEffect(() => {
    if(!isFailedTransaction){

      const txt = "Digital currency balances should be updated in 30 secs to 2 minutes"
      isIOS ? alert(txt) : showToast(txt)
      navigation.removeListener('focus',{})

   }
    
  },[navigation])

  return (
    <ScreenLayoutTab
      title={title}
      leftIconName="ArrowLeft"
      onLeftIconClick={onDone}
    >
        <Container justify="space-between" style={styles.container}>
          <ViewShot
            style={{ backgroundColor: theme.colors.white, paddingTop: 0 }}
            ref={viewShotRef}
            options={{ format: "jpg", quality: 0.5 }}
          >
            <Container alignItems="center" noItemsStretch spacing={1}>
              <Image
                source={!isFailedTransaction ? require("~images/successful_menu.png") : require("~images/transaction_failed.png") }
                style={styles.image}
              />
              <Typography fontFamily="Helvetica-Bold" color={!isFailedTransaction ?"success" : "error" } variant="subtitle" style={{ marginTop: 10, marginBottom:40 }}>
                    {statusHeader}
                  </Typography>
              {!isFailedTransaction ? (!shareRecipt && <TouchableOpacity onPress={() => copyToClipboard(uuid)}>
                <Container
                  direction="row"
                  noItemsStretch
                  alignItems="center"
                  justify="space-evenly"
                >
                  <Typography fontFamily="HelveticaNeue-Medium" color="darkBlack"  style={{ marginRight: 8 }}>
                    Transaction Id
                  </Typography>
                  <Typography
                  fontFamily="HelveticaNeue-Medium"
                    style={{ width: 180, marginRight: 8 }}
                    numberOfLines={1}
                    ellipsizeMode="tail"
                  >
                    {uuid}
                  </Typography>
                  <Icon name="CopyIcon" size="xs" />
                </Container>
              </TouchableOpacity>)
              : undefined }


              {!isFailedTransaction ? (shareRecipt && <Container
                direction="row"
                noItemsStretch
                alignItems="flex-start"
                justify="flex-start"
              >
                <Typography fontFamily="HelveticaNeue-Medium" color="darkBlack"  style={{ marginRight: 8 }}>
                  Txn Id : {uuid}
                </Typography>
        
              </Container>
              
              ):undefined}

            </Container>

            <Container
              alignItems="center"
              noItemsStretch
              spacing={1}
              style={{ marginVertical: 50 }}
            >
              
              
              <Container
                direction="row"
                justify="space-between"
                style={styles.row}
              >
                <Typography  fontFamily="HelveticaNeue-Medium" color="grayDarkest"textAlign="left">Transaction Type</Typography>
                <Container>
                  <Typography   fontFamily="HelveticaNeue-Medium" color="darkBlack" textAlign="right">
                  {transactionType}
                  </Typography>
                </Container>
              </Container>
            
              <View style={styles.separator} />

              <Container
                direction="row"
                justify="space-between"
                alignItems="center"
                noItemsStretch
                style={styles.row}
              >
                <Typography  fontFamily="HelveticaNeue-Medium" color="grayDarkest" textAlign="left">Amount</Typography>
                <Container>
                  <Typography fontFamily="HelveticaNeue-Medium" color="darkBlack" textAlign="right">
                    {formatter(Number(totalAmount), {
                      asset: from
                    })}{" "}
                    {from}
                  </Typography>
                </Container>
              </Container>

              <View style={styles.separator} />
              { !isFailedTransaction ? 
              <Container
                direction="row"
                justify="space-between"
                style={styles.row}
              >
                <Typography fontFamily="HelveticaNeue-Medium" color="grayDarkest" textAlign="left">Time</Typography>
                <Container>
                  <Typography fontFamily="HelveticaNeue-Medium" color="darkBlack" textAlign="right">
                    {`${date.toDateString()} ${date.toLocaleTimeString()}`}
                  </Typography>
                </Container>
              </Container> :  null }
              { !isFailedTransaction ?  <View style={styles.separator} /> : null }

              <Container
              direction="row"
              justify="space-between"
              style={[styles.row , {marginTop:-1}]}
              >
              <Typography color="grayDarkest" fontFamily="HelveticaNeue-Medium" textAlign="left">Description </Typography>
                  <Typography  color={isFailedTransaction   ? "error" : "darkBlack" }  fontFamily="HelveticaNeue-Medium"  style={{width:190}} textAlign="right">
                 {description}
                  </Typography>
              </Container>

             <View style={styles.separator} /> 

              <Container
                direction="row"
                justify="space-between"
                style={styles.row}
              >
                <Typography color="grayDarkest" fontFamily="HelveticaNeue-Medium" textAlign="left">Status</Typography>
                <Container>
                  <Typography textAlign="right"  fontFamily="HelveticaNeue-Medium" color={!isFailedTransaction? "success" : "error"}>
                    {status}
                  </Typography>
                </Container>
              </Container>
            </Container>
          </ViewShot>

          <Container alignItems="center" justify="center" noItemsStretch spacing={1}>
            <Typography fontFamily="HelveticaNeue-Light" variant="chip" color="darkBlack">
            {/* Rates will be refreshed in 30 seconds */}
            </Typography>

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
              text= {"Share"}
              fontWeight="bold"
              fontFamily={"Helvetica-Bold"}
              onPress={() => {
                onShareClick()
              }}
            />
          </Container>
        </Container>
        
    </ScreenLayoutTab>
)}

export default BuySellReciptScreen
