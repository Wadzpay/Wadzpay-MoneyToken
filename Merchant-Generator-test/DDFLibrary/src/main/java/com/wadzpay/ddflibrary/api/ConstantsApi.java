package com.wadzpay.ddflibrary.api;

import android.app.Activity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

public class ConstantsApi {
    public static Activity mActivity;
    public static int API_CONNECTION_TIME_OUT = 10000;
    public static String API_CALL_URL = "";
    public static String API_GET_POST = "GET";
    public static String BASE_URL_DEV = "https://api.dev.wadzpay.com";
    public static String BASE_URL_TEST = "https://api.test.wadzpay.com";
    public static String BASE_URL_PROD = "https://api.wadzpay.com";
    public static String BASE_URL_UAT = "https://api.uat.wadzpay.com";
    public static String BASE_URL = "https://api.dev.wadzpay.com";
    public static String BASE_URL_POC = "https://api.poc.wadzpay.com";
    public static String BASE_URL_DDF_UAT = "https://api.ddf1.wadzpay.com";
    public static String BASE_URL_DDF_PROD = "https://api.ddf.wadzpay.com";
    public static String BASE_URL_GEIDEA_DEV = "https://api.geidea-dev.wadzpay.com";
    public static String BASE_URL_GEIDEA_TEST = "https://api.geidea-test.wadzpay.com";
    public static String BASE_URL_GEIDEA_UAT = "https://api.geidea-uat.wadzpay.com";
//    public static String BASE_URL = "https://api.test.wadzpay.com";
    public static String SUB_URL = "";
    public static final String API_BEARER = "Bearer ";
//    public static String API_JWT = "";
//    public static String API_TOKEN = API_BEARER + API_JWT;
    public static ArrayList<String> alToken = new ArrayList<>();
    public static String API_CURRENCY_CODE = "BTC";

    public static String TEST_ONE_URL = "https://reqres.in/api/unknown";
    public static String TEST_BB_URL = "https://reqres.in/api/unknown";

//    public static final String CONVERSION_URL = "/pos/merchantDashboard/getConversionRateList?fiatType=AED";
    public static final String CONVERSION_URL = "/pos/merchantDashboard/getConversionRateList?";
    public static final String CONVERSION_URL_TWO = "/pos/merchantDashboard/getConversionRateListAlgo?";
//    public static final String PAYMENT_INFO_URL = "/pos/merchantDashboard/getPaymentInfo?cryptoType=BTC&fiatType=AED";
    public static final String PAYMENT_INFO_URL = "/pos/merchantDashboard/getPaymentInfo?digitalCurrencyType=BTC&fiatType=AED";
//    public static final String  PAYMENT_INFO_URL_TWO = "/pos/merchantDashboard/getPaymentInfo?";
    public static final String  PAYMENT_INFO_URL_TWO = "/pos/merchant/payment?";
    public static final String  PAYMENT_INFO_URL_THREE = "/pos/merchant/paymentAlgo?";
    public static final String  REFRESH_PAYMENT_INFO_URL = "/pos/merchantDashboard/refreshPaymentInfo?";
    public static final String CRYPTO_LIST_URL = "/v1/config/cryptos";
    public static final String FIAT_LIST_URL = "/v1/config";
    public static final String FIAT_LIST_URL_TWO = "/v1/config/algo";
    public static final String TRANSACTION_STATUS_URL = "/pos/merchantDashboard/getTransactionStatus?";
    public static final String TRANSACTION_STATUS_URL_TWO = "/pos/merchantDashboard/getTransactionStatusAlgo?";
    public static final String MERCHANT_DETAILS_URL = "/merchantDashboard/merchantDetails";
    public static final String MERCHANT_DETAILS_URL_TWO = "/merchantDashboard/merchantDetailsStatic";
    public static final String ADD_POS_MERCHANT_URL = "/pos/merchantDashboard/addPosToMerchant";
// fiat list
//    https://api.dev.wadzpay.com/v1/config

    public static String apiResponseData = "";
    public static String FLAG_API_NAME = "";
    public static String FLAG_API_TEST = "TEST_API";
    public static String FLAG_API_MEDIA = "MEDIA_API";

    public static String FLAG_API_CONVERSION = "CONVERSION_API";
    public static String FLAG_API_PAYMENT_INFO = "PAYMENT_INFO_API";
//    public static String FLAG_API_CRYPTO_LIST = "CRYPTO_LIST";
    public static String FLAG_API_FIAT_LIST = "FIAT_LIST";
    public static String FLAG_API_CURRENCY_LIST = "CURRENCY_LIST";
    public static String FLAG_API_MESSAGE = "FLAG_API_MESSAGE";
    public static String FLAG_API_ACTIVITY_FAILED = "ACTIVITY_FAILED";
    public static String FLAG_API_MERCHANT_DETAILS = "MERCHANT_DETAILS";
    public static String FLAG_API_ADD_POS_MERCHANT = "ADD_MERCHANT_POS";

    public static String[] KEYS_TEST_PARSE = {"id","name","year","color","pantone_value"};
    public static String[] KEYS_FIAT_PARSE = {"code","sign","fullName"};
    public static String[] KEYS_MERCHANT_DETAILS_PARSE = {"posId","posName"};
    public static String[] KEYS_CURRENCY_PARSE = {"code","inwardAddress","outwardAddress"};
    public static String[] KEYS_CONVERSION_PARSE = {"asset","amountFiat","amountCrypto","fees","totalAmount"};
    public static String[] KEYS_COMMON = {"code","MSG","DATA"};
//    public static String KEY_TRANSACTION_STATUS = "TRANSACTION_STATUS";

//
    public static ArrayList<HashMap<String, String>> alTestOne = new ArrayList<>();
    public static ArrayList<HashMap<String, String>> alConversions = new ArrayList<>();
    public static ArrayList<HashMap<String, String>> alConversionsNew = new ArrayList<>();
    public static ArrayList<HashMap<String, String>> alFiats = new ArrayList<>();
    public static ArrayList<HashMap<String, String>> alMerchantDetails = new ArrayList<>();
    public static ArrayList<HashMap<String, String>> alDigitalCurrencies = new ArrayList<>();
    public static ArrayList<HashMap<String, String>> alDigitalCurrenciesNew = new ArrayList<>();

    public static String strFeeAmount = "";
    public static String strFeeOnWadzPay = "";
    public static String strFeeOnExternal = "";
    public static ArrayList<String> alImageNameExtension = new ArrayList<>();

    public static String strQRString = "";
    public static String strQREncryptedString = "";
    public static String strQRAddress = "";
    public static String strQRAddressFirst = "";
    public static String strTSStatus = "";
    public static String strTSFiat = "";
    public static String strTSCrypto = "";
    public static String strTSDigitalCurrencyType = "";
    public static BigDecimal bigDecimalTSCrypto;
    public static String strTSReceived = "";
    public static BigDecimal bigDecimalTSReceived ;
    public static String strTSReceivedFinal = "";
    public static String strTSCreated = "";
    public static String strUUID = "";
    public static String strTransactionID = "";
    public static String strPosName = "POS-New1";
    public static String strPosID = "0";
    public static String strMDEmail = "";
    public static String strMDPhone = "";
    public static String strMDQRString = "";
    public static int API_STATUS_CODE = 0;
    public static String API_TEMP_JWT = "";

}
