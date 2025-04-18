import React from "react"
import { useTranslation } from "react-i18next"

import Container from "./Container"
import Modal from "./Modal"
import Typography from "./Typography"
import theme from "./theme"
import { StringFormat } from "expo-clipboard"
import useFormatCurrencyAmount from "~/helpers/formatCurrencyAmount"

type Props = {
    transactionType: any
    amountRequested: number
    feeConfigList: any
    netFeeKeyName: string
    totalAmount: number | any,
    netAmountCalulated : number | any
    totalAmountText : string
    transactionBlockHeader : string
}

export const TransactionSummaryComponent: React.FC<Props> = ({
transactionType,
amountRequested,
feeConfigList,
netFeeKeyName,
netAmountCalulated,
totalAmountText,
totalAmount,
transactionBlockHeader
}: Props) => {
    const { t } = useTranslation()
    const formatter = useFormatCurrencyAmount();
    
    console.log("feeConfigList ", feeConfigList)
  return (
    <Container justify={'center'}  alignItems={'center'}>
              <Container
                style={{flex:1, marginTop: 35, marginBottom:10, marginHorizontal:20}}
                >
                <Typography
                  variant="label"
                  color="darkBlackBold"
                  fontFamily="Rubik-Medium"
                  textAlign="left">
                  {transactionBlockHeader || t('TRANSACTION SUMMARY') }
                </Typography>
                <Container alignItems='center'justify='center'  style={{borderWidth:1, borderRadius: 10,borderColor:"#C4C4C4", marginTop:15}}>
                <Container alignItems='flex-start'justify='center'  style={{borderBottomWidth:1, backgroundColor:"#F6F6F6",borderTopEndRadius: 10, borderTopStartRadius:10,borderColor:"#C4C4C4", padding:20}}>
                    <Typography textAlign='left' color='darkBlackBold'  fontFamily='Rubik-Regular' variant='label' style={{marginVertical:6}}>Transaction Type</Typography>
                    <Typography textAlign='left' color='darkBlackBold' fontFamily='Rubik-Medium' variant='label'>{transactionType}</Typography>
                    {transactionType === "Service Fee"  &&  feeConfigList?.map((x:any , index:any) => {
                    return <Container alignItems='flex-start'justify='center'  style={{marginTop:20}}>
                        <Typography textAlign='left' color='darkBlackBold'  fontFamily='Rubik-Regular' variant='label' style={{marginVertical:6}}>Period</Typography>
                        <Typography textAlign='left' color='darkBlackBold' fontFamily='Rubik-Medium' variant='label'>{x.description}</Typography>
                    </Container> 
                    })
                  }
                   {transactionType === "Wallet Low Balance Fee"  &&  feeConfigList?.map((x:any , index:any) => {
                    return <Container alignItems='flex-start'justify='center'  style={{marginTop:20}}>
                        <Typography textAlign='left' color='darkBlackBold'  fontFamily='Rubik-Regular' variant='label' style={{marginVertical:6}}>Description</Typography>
                        <Typography textAlign='left' color='darkBlackBold' fontFamily='Rubik-Medium' variant='label'>{x.description}</Typography>
                    </Container> 
                    })
                  }

                </Container>
              
                
                  {transactionType !== "Service Fee" && transactionType !== "Wallet Low Balance Fee" && <Container alignItems='center'  style={{borderBottomWidth:1, backgroundColor:theme.colors.white, borderBottomColor: "#C4C4C4",padding:20}}>
                      <Container justify='space-between' direction='row' style={{marginVertical:6}}>
                      <Typography textAlign='left' color='darkBlackBold'  fontFamily='Rubik-Regular'>{transactionType == "Topup" ? "Amount Requested" : transactionType == "Redeem Unspent" || transactionType == "Refund" ? "Tokens Requested" : "Tokens"}</Typography>
                      <Typography textAlign='left' color='darkBlackBold'  fontFamily='Rubik-Regular'>{amountRequested}</Typography>
                      </Container>
                      {feeConfigList?.length > 0 && feeConfigList?.map((x:any , index:any) => {
                         return <Container key={index} justify='space-between' direction='row' style={{marginVertical:6}}>
                         <Typography textAlign='left' color='darkBlackBold' fontFamily='Rubik-Regular'>{x.feeName}</Typography>
                         <Typography textAlign='left' color='darkBlackBold'  fontFamily='Rubik-Regular'>{x.currencyType || x.currencyUnit} {x.feeCalculatedAmount || formatter(x.feeAmount, {asset: x.asset})}</Typography>
                         </Container>
                      })
                      }
                      
                      {feeConfigList?.length > 0 ? <Container justify='space-between' direction='row' style={{marginVertical:6}}>
                      <Typography textAlign='left' color='darkBlackBold' fontFamily='Rubik-Regular'>{netFeeKeyName}</Typography>
                      <Typography textAlign='left' color='darkBlackBold' fontFamily='Rubik-Regular'>{netAmountCalulated}</Typography>
                      </Container>
                      :
                     (<Container justify='space-between' direction='row' style={{marginVertical:6}}>
                      <Typography textAlign='left' color='darkBlackBold' fontFamily='Rubik-Regular'>Fee</Typography>
                      <Typography textAlign='left' color='darkBlackBold' fontFamily='Rubik-Regular'>0.00</Typography>
                      </Container>)
                      }
                </Container>}
                <Container alignItems='flex-start'justify='space-between' direction='row'  style={{backgroundColor:"#FFECC0",borderBottomEndRadius: 10, borderBottomStartRadius:10, padding:20}}>
                    
                    <Typography textAlign='left'color='darkBlackBold'  fontFamily='Rubik-Medium'>{totalAmountText}</Typography>
                    <Typography textAlign='left' color='darkBlackBold' fontFamily='Rubik-Medium'>{totalAmount}</Typography>
                    </Container>
                </Container>
              </Container>
            </Container>
  )
}
