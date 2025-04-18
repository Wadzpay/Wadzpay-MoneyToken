import {Dimensions, Platform, StatusBar, ToastAndroid} from 'react-native';
import EncryptedStorage from 'react-native-encrypted-storage';

import 'intl';
// TODO import localizations that are used for formatting numbers
import 'intl/locale-data/jsonp/en';

import env, {ENV} from './env';
import {AssetFractionDigits} from './api/constants';
import {Asset, TokenToAmount} from './constants/types';
import { Transaction } from './api/models';
import Clipboard from '@react-native-clipboard/clipboard';
import { useContext } from 'react';
import { UserContext } from './context';
import useFormatCurrencyAmount from './helpers/formatCurrencyAmount';

export const isIOS = Platform.OS === 'ios';

export const isDev = false;

export const isTesting = env.TYPE === ENV.TESTING;
export const isGeideaDev = env.TYPE === ENV.GEIDEADEV;
export const isGeideaTest = env.TYPE === ENV.GEIDEATEST;
export const isGeideaUat = env.TYPE === ENV.GEIDEAUAT;
export const isProd = env.TYPE === ENV.PROD;
export const isUat = env.TYPE === ENV.UAT;
export const sartTxt = 'SAR* ';
const X_WIDTH = 375;
const X_HEIGHT = 812;

export const {height, width} = Dimensions.get('window');
const {user} = useContext(UserContext);

export const isIPhoneX: () => boolean = () =>
  isIOS ? width >= X_WIDTH && height >= X_HEIGHT : false;

export const StatusBarHeight = Platform.select({
  ios: isIPhoneX() ? 44 : 20,
  android: StatusBar.currentHeight,
  default: 0,
});

export const formatPhoneNumber: (
  phoneNumber: string,
) => string = phoneNumber => {
  return phoneNumber.replace(/[^+\d]/g, '');
};

export const isNumeric: (str: string) => boolean = str => {
  return !isNaN(Number(str)) && !isNaN(parseFloat(str));
};

export const calculateTimeLeft: (targetDate: Date) => number = targetDate => {
  const now = new Date();
  const difference = +targetDate - +now;
  return difference > 0 ? Math.floor((difference / 1000) % 60) : 0;
};

let navigation: any = undefined;

export const setNavigation = (value: any) => {
  navigation = value;
};

export const getNavigation = () => {
  return navigation;
};

export const showToast = (msg: string) => {
  return ToastAndroid.showWithGravity(
    msg,
    ToastAndroid.SHORT,
    ToastAndroid.CENTER,
  );
};

export const calculateEstimatedFee = (
  amount: number,
  p2pFee: number,
  asset: Asset,
) => {
  return (amount * p2pFee).toFixed(AssetFractionDigits[asset]);
};

export function formatAMPM(date) {
  var hours = date.getHours();
  var minutes = date.getMinutes();
  var ampm = hours >= 12 ? 'pm' : 'am';
  hours = hours % 12;
  hours = hours ? hours : 12; // the hour '0' should be '12'
  minutes = minutes < 10 ? '0' + minutes : minutes;
  var strTime = hours + ':' + minutes + ' ' + ampm;
  return strTime;
}

export const CapitalizeFirstLetter = str => {
  return str.length ? str.charAt(0).toUpperCase() + str.slice(1) : str;
};
export const ConTwoDecDigit = digit => {
  return digit.includes('.')
    ? digit.split('.').length >= 2
      ? digit.split('.')[0] + '.' + digit.split('.')[1].substring(-1, 2)
      :  digit
    : digit;
};

const validateTransaction = (obj: any, amount: number) => {

  if (
    (obj?.availableCount !== null && obj?.availableCount < 0) ||
    (obj?.availableCount !== null && obj?.availableCount == 0) ||
    (obj?.remainingMaximumBalance !== null && amount > obj?.remainingMaximumBalance)
  ) {
    return {isPermitted: false, msgType: 'frequency error'};
  } else if (obj?.maximumBalance ==! null && amount > obj?.maximumBalance) {
    return {isPermitted: false, msgType: 'balance error'};
  } else {
    return {isPermitted: true, msgType: 'success'};
  }
};
export const getErrorMessage = (obj : any) => {
  let maxBalance =  obj?.maximumBalance
  let minBalance =  obj?.minimumBalance

  if(maxBalance > 0 &&   minBalance > 0 ) {
    return  `Please enter amount between ${obj?.minimumBalance} - ${obj?.maximumBalance}`

  } else if (maxBalance > 0   &&   minBalance == null) {
     return  `Please enter amount less than or equal to ${obj?.maximumBalance}`
   
   } else if(minBalance > 0 &&  maxBalance == null) {
    return  `Please enter amount greater than or equal to ${obj?.minimumBalance}`
   } 
   //return "nan"

  // if(obj && obj?.maximumBalance && obj?.minimumBalance) {
  //   return  `Please enter valid amount between ${obj?.minimumBalance} - ${obj?.maximumBalance}`
  // } else if(obj && obj.minimumBalance != null){
  //   return  `Please enter valid amount greater than ${obj?.minimumBalance}`
  // } else {
  //   return  `Please enter valid amount  between 1 - ${obj?.maximumBalance}`
  // }

  
}


const validatePerTransactionData = (obj: any, amount: number) => {
  let isAmountExistInMaxMin = true


  let maxBalance =  obj?.maximumBalance
  let minBalance =  obj?.minimumBalance


  if(maxBalance > 0 &&   minBalance > 0 ) {

   // do comparison between maximumBalance and minimumBalance with amount

   isAmountExistInMaxMin = (amount < maxBalance || amount == maxBalance) && (amount > minBalance || amount == minBalance)
   return isAmountExistInMaxMin

  } else if(maxBalance > 0   &&   minBalance == null) {

    // do comparison between maximumBalance with amount I.e amount <= maximumBalance

    isAmountExistInMaxMin = (amount < maxBalance || amount == maxBalance)
    return isAmountExistInMaxMin

  } else if(minBalance > 0 &&  maxBalance == null) {

    // do comparison between minimumBalance with amount I.e amount >= minimumBalance
    isAmountExistInMaxMin = amount > minBalance || amount == minBalance
    return isAmountExistInMaxMin

  } 

  return isAmountExistInMaxMin

  // if(obj && obj.maximumBalance && obj.minimumBalance) {
  //   isAmountExistInMaxMin = (amount < obj?.maximumBalance || amount == obj?.maximumBalance) && (amount > obj?.minimumBalance || amount == obj?.minimumBalance)
  //   return isAmountExistInMaxMin
  // } else if(obj && obj.minimumBalance != null){
  //   isAmountExistInMaxMin = amount > obj?.minimumBalance || amount == obj?.minimumBalance
  //   return isAmountExistInMaxMin
  // } else {
  //   isAmountExistInMaxMin = (amount < obj?.maximumBalance || amount == obj?.maximumBalance) && (amount > 0)
  //   return isAmountExistInMaxMin
  // }
}
  

export const isTransactionAllowed = (
  transactionConfigData: any,
  amount: number,
) => {
  // transactionConfigData =  {
  //   "daily":{
  //     "availableCount": 0,
  //     "count": 5,
  //     "maximumBalance": 5000,
  //     "minimumBalance": 99,
  //     "remainingMaximumBalance": 5000,
  //   },
  //   "half_YEARLY":{
  //     "availableCount": 19,
  //     "count": 20,
  //     "maximumBalance": 100000,
  //     "minimumBalance": 99,
  //     "remainingMaximumBalance": 99900,
  //   },
  //   "monthly":{
  //     "availableCount": 9,
  //     "count": 10,
  //     "maximumBalance": 50000,
  //     "minimumBalance": 99,
  //     "remainingMaximumBalance": 49900,
  //   },
  //   "one_TIME": null,
  //   "per_TRANSACTION":{
  //     "availableCount": null,
  //     "count": 1,
  //     "maximumBalance": 1000,
  //     "minimumBalance": 100,
  //     "remainingMaximumBalance": null,
  //   },
  //   "quarterly":{
  //     "availableCount": 14,
  //     "count": 15,
  //     "maximumBalance": 80000,
  //     "minimumBalance": 99,
  //     "remainingMaximumBalance": 79900,
  //   },
  //   "weekly":{
  //     "availableCount": 0,
  //     "count": 10,
  //     "maximumBalance": 10000,
  //     "minimumBalance": 99,
  //     "remainingMaximumBalance": 9900,
  //   },
  //   "yearly":{
  //     "availableCount": 25,
  //     "count": 30,
  //     "maximumBalance": 150000,
  //     "minimumBalance": 99,
  //     "remainingMaximumBalance": 20000000,
  //   },
  // }

  const isAmountExistInMaxMin = validatePerTransactionData(
    transactionConfigData.per_TRANSACTION,
    amount,
  );

  let yearlyValidateTransaction = validateTransaction(
    transactionConfigData.yearly,
    amount,
  );
  let halfYealyValidateTransaction = validateTransaction(
    transactionConfigData.half_YEARLY,
    amount,
  );
  let quaterlyValidateTransaction = validateTransaction(
    transactionConfigData.quarterly,
    amount,
  );
  let monthlyValidateTransaction = validateTransaction(
    transactionConfigData.monthly,
    amount,
  );
  let weeklyValidateTransaction = validateTransaction(
    transactionConfigData.weekly,
    amount,
  );
  let dailyValidateTransaction = validateTransaction(
    transactionConfigData.daily,
    amount
  );
  

  const isAllPermitted =
    isAmountExistInMaxMin &&
    yearlyValidateTransaction.isPermitted &&
    halfYealyValidateTransaction.isPermitted &&
    quaterlyValidateTransaction.isPermitted &&
    monthlyValidateTransaction.isPermitted &&
    weeklyValidateTransaction.isPermitted &&
    dailyValidateTransaction.isPermitted;

  if (isAllPermitted) {
    return 'allowed';
  } else {
    if(!isAmountExistInMaxMin ){
      return getErrorMessage(transactionConfigData?.per_TRANSACTION)
    } else if (yearlyValidateTransaction.msgType == 'frequency error') {
      return 'yearly limit exhausted ';
    } else if (yearlyValidateTransaction.msgType == 'balance error') {
      return 'yearly maximum limit exceeded';
    } else if (halfYealyValidateTransaction.msgType == 'frequency error') {
      return 'Half yearly limit exhausted ';
    } else if (halfYealyValidateTransaction.msgType == 'balance error') {
      return 'Half yearly maximum limit exceeded';
    } else if (quaterlyValidateTransaction.msgType == 'frequency error') {
      return 'Quaterly limit exhausted ';
    } else if (quaterlyValidateTransaction.msgType == 'balance error') {
      return 'Quaterly maximum limit exceeded';
    } else if (monthlyValidateTransaction.msgType == 'frequency error') {
      return 'Monthly limit exhausted';
    } else if (monthlyValidateTransaction.msgType == 'balance error') {
      return 'Monthly maximum limit exceeded';
    } else if (weeklyValidateTransaction.msgType == 'frequency error') {
      return 'Weekly limit exhausted';
    } else if (weeklyValidateTransaction.msgType == 'balance error') {
      return 'weekly Maximum limit exceeded';
    } else if (dailyValidateTransaction.msgType == 'frequency error') {
      return 'Daily limit exhausted';
    } else if (dailyValidateTransaction.msgType == 'balance error') {
      return 'daily Maximum limit exceeded';
    } else {
      return 'Maximum limit exceeded';
    }
  }
  //   ? halfYealyValidateTransaction.isPermitted
  //     ? quaterlyValidateTransaction.isPermitted
  //       ? monthlyValidateTransaction.isPermitted
  //         ? weeklyValidateTransaction.isPermitted
  //           ? dailyValidateTransaction.isPermitted
  //             ? "allowed"
  //             : dailyValidateTransaction.msgType == "frequency error"
  //             ? "daily limit exhausted "
  //             : "maximum limit exceeded"
  //           : weeklyValidateTransaction.msgType == "frequency error"
  //           ? "weekly limit exhausted "
  //           : "maximum limit exceeded"
  //         : monthlyValidateTransaction.msgType == "frequency error"
  //         ? "monthly limit exhausted "
  //         : "maximum limit exceeded"
  //       : quaterlyValidateTransaction.msgType == "frequency error"
  //       ? "quaterly limit exhausted "
  //       : "maximum limit exceeded"
  //     : halfYealyValidateTransaction.msgType == "frequency error"
  //     ? "half yearly limit exhausted "
  //     : "maximum limit exceeded"
  //   : yearlyValidateTransaction.msgType == "frequency error"
  //   ? "yearly limit exhausted "
  //   : "maximum limit exceeded"
};

export const validateInitialLoading = (
  transactionConfigData: any,
  amount: number,
) => {
  // transactionConfigData =  {
  //   "daily": null,
  //   "half_YEARLY": null,
  //   "initialLoading": true,
  //   "monthly": null,
  //   "one_TIME": null,
  //   "per_TRANSACTION": null,
  //   "quarterly": null,
  //   "weekly": null,
  //   "yearly": null,
  // }
  const isAmountExistInMaxMin = validatePerTransactionData(
    transactionConfigData.one_TIME,
    amount,
  );
  if (!isAmountExistInMaxMin) {
    return `Please enter valid amount between ${transactionConfigData?.one_TIME?.minimumBalance} - ${transactionConfigData?.one_TIME?.maximumBalance}`;
  } else {
    return 'allowed';
  }
};
export async function storeValue(key: string, value: any) {
  try {
    await EncryptedStorage.setItem(key, value);
  } catch (error) {
    console.error(error);
  }
}
export async function retrieveValue(key: string) {
  try {
    const session = await EncryptedStorage.getItem(key);
  if (session !== undefined) {
    // Congrats! You've just retrieved your first value!
    return session
}
  } catch (error) {
    console.error(error);
  }
}
export async function removeValue(key: string) {
  try {
    await EncryptedStorage.removeItem(key);
  } catch (error) {
    console.error(error);
  }
}
async function clearStorage() {
  try {
    await EncryptedStorage.clear();
  } catch (error) {
    console.error(error);
  }
}

export const getTransactionUrl = (assetName: string, blockchainTxId: string) => {
  switch (assetName) {
    case "BTC":
      return "www.btcscan.org/tx/" + blockchainTxId
    case "ETH" || "USDT":
      return "www.etherscan.io/txn/" + blockchainTxId
    case "WTK":
      return "assetName" + blockchainTxId
    case "THB":
      return "assetName" + blockchainTxId
    case "PHP":
      return "assetName" + blockchainTxId
    case "AED":
      return "assetName" + blockchainTxId
    case "SART":
      return "https://www.google.com/search?q=" + blockchainTxId
    default:
      return "www.google.com"
  }
}

export const getTransactionType = (transaction: Transaction , transactionType: string) => {
  switch (transactionType) {
    case "ON_RAMP":
      return "On Ramp"
    case "OFF_RAMP":
      return "Off Ramp"
    case "MERCHANT":
      return "POS"
    case "PEER_TO_PEER":
      return transaction?.asset == "SART" ? "Transfer" : "Wadzpay Wallet"
    case "OTHER":
    case "EXTERNAL_SEND":
    case "EXTERNAL_RECEIVE":
    case "POS":
      return transaction?.asset == "SART" ? "Payment" : "External Wallet"
    case "DEPOSIT":
      return transaction?.asset == "SART" ? "Topup" : "Deposit"
    case "WITHDRAW":
      return transaction?.asset == "SART" ? "Redeem Unspent" : "Withdraw"
    case "REFUND":
      return transaction?.asset == "SART" ? "Merchant Refund" : "External Wallet"
    case "SERVICE_FEE":
      return "Service Fee"
      case "WALLET_FEE":
        return "Wallet Low Balance Fee"
    default:
      return transactionType
  }
};
export const getActionName = (transaction: Transaction , transactionType: string) => {
  switch (transactionType) {
    case "ON_RAMP":
      return "Net On Ramp Amount"
    case "OFF_RAMP":
      return "Net Off Ramp Amount"
    case "MERCHANT":
      return "Amount Paid"
    case "PEER_TO_PEER":
      return transaction?.asset == "SART" ? "Net Transfer Amount" : "Wadzpay Wallet"
    case "OTHER":
    case "EXTERNAL_SEND":
    case "EXTERNAL_RECEIVE":
    case "POS":
      return transaction?.asset == "SART" ? "Amount Paid" : "External Wallet"
    case "DEPOSIT":
      return transaction?.asset == "SART" ? "Amount Debited" : "Deposit"
    case "WITHDRAW":
      return transaction?.asset == "SART" ? "Net Redeem Token" : "Withdraw"
    case "REFUND":
      return transaction?.asset == "SART" ? "Net Refund Amount" : "External Wallet"
    case "SERVICE_FEE":
      return "Service Fee"
    case "WALLET_FEE":
        return "Wallet Low Balance Fee"
    default:
      return transactionType
  }
};

export const getTotalName = (transaction: Transaction , transactionType: string) => {
  switch (transactionType) {
    case "ON_RAMP":
      return "Amount Received"
    case "OFF_RAMP":
      return "Amount Transferred"
    case "MERCHANT":
      return "Tokens Paid"
    case "PEER_TO_PEER":
      return transaction?.asset == "SART" ? "Tokens Transferred" : "Wadzpay Wallet"
    case "OTHER":
    case "EXTERNAL_SEND":
    case "EXTERNAL_RECEIVE":
    case "POS":
      return transaction?.asset == "SART" ? "Tokens Paid" : "External Wallet"
    case "DEPOSIT":
      return transaction?.asset == "SART" ? "Received Tokens" : "Deposit"
    case "WITHDRAW":
      return transaction?.asset == "SART" ? "Redeemed Amount" : "Withdraw"
    case "REFUND":
      return transaction?.asset == "SART" ? "Refund Tokens" : "External Wallet"
    case "SERVICE_FEE":
      return "Service Fee"
      case "WALLET_FEE":
        return "Wallet Low Balance Fee"
    default:
      return transactionType
  }
};

export const copyToClipboard = (value: string, type: string) => {

  if(type == "TRANSACTION_ID") {
    Clipboard.setString(value)
    showToast(`Transaction ID Copied`)  
  }
  else {
    Clipboard.setString(value)
    showToast(`Transaction Hash Copied`)  
  }
}
export const getFromToValues = (details: any) => {
  if (details) {
    if (details.length == 2) {
      return details[1].length > 6
        ? 'XXXX XXXX XXXX XXXX ' + details[1].slice(-4)
        : 'Wallet';
    }
    return 'Wallet';
  }
  return '';
};

export const showToFeildValue = (transaction: any) => {
  if (transaction.transactionType === 'WITHDRAW') {
    const details = transaction.description.split('To');
    return getFromToValues(details);
  } else if (transaction.transactionType === 'SERVICE_FEE' ||  transaction.transactionType === 'WALLET_FEE') {
    return "WadzPay"
  }
   else {
    return transaction?.receiverFirstName && transaction?.receiverLastName ? `${transaction?.receiverFirstName} ${transaction?.receiverLastName}` : transaction.receiverName;
  }
};


export const getOtherPartyName = (transaction: any , userEmailId: string) => {
  if (transaction.transactionType === 'WITHDRAW') {
    const details = transaction.description.split('To');
    return getFromToValues(details);
  } else if (transaction.transactionType === 'DEPOSIT') {
    const details = transaction.description.split('From');
    return getFromToValues(details);
  } else if(transaction.transactionType ===  "REFUND") {
    return  transaction?.senderName
  } else if(transaction?.transactionType === "PEER_TO_PEER") {
    return  transaction?.direction === "OUTGOING" ? transaction?.receiverFirstName && transaction?.receiverLastName ? `${transaction?.receiverFirstName} ${transaction?.receiverLastName}` : transaction.receiverName :  transaction?.senderFirstName && transaction?.senderLastName ? `${transaction?.senderFirstName} ${transaction?.senderLastName}` : transaction.senderName
  } 
  else if (transaction.transactionType === 'SERVICE_FEE' ||  transaction.transactionType === 'WALLET_FEE') {
    return "WadzPay"
  }
   else {
    return transaction?.receiverFirstName && transaction?.receiverLastName ? `${transaction?.receiverFirstName} ${transaction?.receiverLastName}` : transaction.receiverName;
  }
};
export const showFromFeildValue = (transaction: any , userEmailId: string) => {


  if (transaction.transactionType === 'DEPOSIT') {
    const details = transaction.description.split('From');
    return getFromToValues(details);
  } else if (transaction.transactionType === 'WITHDRAW') {
    return transaction?.senderFirstName ? `${transaction?.senderFirstName} ${transaction?.senderLastName}` : userEmailId; // as per https://wadzpay.atlassian.net/browse/GI-193 kunal sir direction
  } else {
    return transaction?.senderFirstName ? `${transaction?.senderFirstName} ${transaction?.senderLastName}` : transaction.senderName;
  }
};


export const getTransactionId = (transaction : Transaction) => {
  return  transaction.transactionType === "REFUND" ? transaction?.refundTransactionId || transaction.uuid : transaction.uuid
}

export const getCryptoBalance: (asset: Asset, balances?: TokenToAmount) => number = (
  asset,
  balances
) => Number(balances && balances[asset] ? balances[asset] : 0)

export const getDirection = (item: any, property: string) => {
  if (property === 'name') {
    return item.direction === 'INCOMING' ? 'CreditArrow': 'DebitArrow';
  }
  return item.direction === 'INCOMING'
    ? 'success'
    : item.direction === 'OUTGOING'
    ? 'error'
    : 'orange';
};
export const shownames = (item: any) => {
  if (item.direction === 'OUTGOING') {
    if (item.transactionType === 'WITHDRAW') {
      return item.asset == 'SART' ? 'Refund' : 'Withdraw';
    } else if (item.transactionType === 'DEPOSIT') {
      return item.asset == 'SART' ? 'Topup' : 'Deposit';
    } else if (item.transactionType === 'BUY') {
      return 'Buy';
    } else if (item.transactionType === 'SELL') {
      return 'Sell';
    } else if (item.transactionType === 'SERVICE_FEE') {
      return 'Service Fee';
    } else if (item.transactionType === 'WALLET_FEE') {
      return 'Wallet Low Balance Fee';
    }
    else {
      return item.receiverName;
    }
  } else if (item.direction === 'INCOMING') {
    if (item.transactionType === 'WITHDRAW') {
      return 'Withdraw';
    } else if (item.transactionType === 'DEPOSIT') {
      return item.asset == 'SART' ? 'Topup' : 'Deposit';
    } else if (item.transactionType === 'BUY') {
      return 'Buy';
    } else if (item.transactionType === 'SELL') {
      return 'Sell';
    } else {
      return item.senderName;
    }
  }
};


export const showAssetAmount = (item: any) => {
  const formatter = useFormatCurrencyAmount();
  if (item.direction === 'OUTGOING') {
    if (item.transactionType === 'WITHDRAW') {
      if (item.asset == 'SART') {
        return formatter(item.amount, {asset: item.asset});
      }
    } else if (item.transactionType === 'POS') {
      return formatter(item.totalDigitalCurrencyReceived, {
        asset: item.asset,
      });
    }
    return formatter(item.amount, {asset: item.asset});
  } else if (item.direction === 'INCOMING') {
    if (item.transactionType === 'DEPOSIT') {
      if (item.asset == 'SART') {
       //console.log("satya vasavi",item.amount , "item.asset ", item.asset, "formatter(item.amount, {asset: item.asset})", formatter(item.amount, {asset: item.asset}))
        return formatter(item.amount, {asset: item.asset});
      }
      return formatter(item.fiatAmount, {asset: item.fiatAsset});
    } else if (item.transactionType === 'POS') {
      return formatter(item.totalDigitalCurrencyReceived, {
        asset: item.asset,
      });
    }
  }
  return formatter(item.amount, {asset: item.asset});
};

export const getMaskedAccountNumber = (user : any) => {
  return user && user?.userBankAccount ?  user?.userBankAccount[0]?.bankAccountNumber.slice(-4) : "Please add bank account"
}

export const getAccountHolderName = (user : any) => {
  return user && user?.userBankAccount ?  user?.userBankAccount[0]?.accountHolderName  : "Please add account holder name"
}

// export const calculateDeductions = () => {
  
  
// }

export const findPercentage = (per : number, num: number) => {
  //(num/100)*per;
  return (num / 100)*per
}