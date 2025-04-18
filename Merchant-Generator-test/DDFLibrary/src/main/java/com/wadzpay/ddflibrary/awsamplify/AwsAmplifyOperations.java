package com.wadzpay.ddflibrary.awsamplify;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.regions.Regions;
import com.amplifyframework.AmplifyException;
import com.amplifyframework.auth.AuthException;
import com.amplifyframework.auth.AuthSession;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession;
import com.amplifyframework.auth.result.AuthSessionResult;
import com.amplifyframework.core.Action;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.Consumer;
import com.wadzpay.ddflibrary.activities.EmailVerification;
import com.wadzpay.ddflibrary.activities.LoginActivity;
import com.wadzpay.ddflibrary.api.ConstantsApi;
import com.wadzpay.ddflibrary.callbacks.AmplifySendResult;
import com.wadzpay.ddflibrary.dialogs.ConstantsDialog;
import com.wadzpay.ddflibrary.dialogs.DialogCustomize;
import com.wadzpay.ddflibrary.dialogs.ToastCustomize;
import com.wadzpay.ddflibrary.logs.LoggerDDF;
import com.wadzpay.ddflibrary.utils.ConstantsActivity;

public class AwsAmplifyOperations {
    public String TAG = getClass().getSimpleName();
    public static AwsAmplifyOperations instance = null;
    static Context mContext;
    AmplifySendResult amplifySendResult;

    public AwsAmplifyOperations() {
    }

    public static AwsAmplifyOperations getInstance(Context context) {
        mContext = context;
        mActivity = (Activity) mContext;
        if (instance == null) {
            instance = new AwsAmplifyOperations();
        }
        return instance;
    }


    public void passContext(Context context) {
        mContext = context;
        amplifySendResult = (AmplifySendResult) mContext;
    }

    //
    public void amplifyInit() {
        try {
//            mContext = context;
//            amplifySendResult = (AmplifySendResult) mContext;
            dialogCustomize = DialogCustomize.getInstance();
            Amplify.addPlugin(new AWSCognitoAuthPlugin());
            Amplify.configure(mContext);
            LoggerDDF.e(TAG, "Initialized Amplify");
            getAwsSession();
        } catch (AmplifyException e) {
            LoggerDDF.e(TAG, "Could not initialize Amplify " + e);
            getAwsSession();
        }
    }

    //

    DialogCustomize dialogCustomize;

    public void amplifySignIn(String userId, String password) {
//        ConstantsActivity.STR_USER_ID = userId;
        dialogCustomize.displayProgressDialog(mContext, ConstantsDialog.FLAG_DIALOG_DISPLAY);
        LoggerDDF.e(TAG, "amplifySignIn");
        Amplify.Auth.signIn(
                userId,
                password,
//                result -> LoggerDDF.e("AuthQuickstart", result.isSignInComplete() ? "Sign in succeeded" : "Sign in not complete"),
                result -> getSignInStatus(),
//                error -> LoggerDDF.e("AuthQuickstart", error.toString())
                error -> {
                    LoggerDDF.e("error ", error.toString());
                    signInFailed();
                }
        );


    }

    public boolean getSignInStatus() {
        LoggerDDF.e(TAG, "getSignInStatus");
        amplifyToken();
        return true;
    }

    public boolean signInFailed() {

        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                dialogCustomize.displayProgressDialog(mContext, ConstantsDialog.FLAG_DIALOG_DISMISS);
                ToastCustomize.displayToast(mContext, "Invalid Login Details");
                dialogCustomize.displayDialog(mContext, ConstantsDialog.FLAG_DIALOG_DISPLAY, "Invalid Login", "Please Enter Valid Credentials");
            }
        });
        return true;
    }

    //
    CognitoUserPool cognitoUserPool;

    public void amplifyToken() {
        LoggerDDF.e(TAG, "amplifyToken@118");
        if (ConstantsActivity.FLAG_ENVIRONMENT.equalsIgnoreCase(ConstantsActivity.FLAG_DEV)) {
            cognitoUserPool = new CognitoUserPool(mContext, "eu-central-1_YHGC0AdSw", "2pge6nhui8edjckr5fts5o0a1u", "XS9WbHlnjVP7LR6QI4Z5QrPHynzBisa4cWcNYLDm", Regions.EU_CENTRAL_1);
        } else if (ConstantsActivity.FLAG_ENVIRONMENT.equalsIgnoreCase(ConstantsActivity.FLAG_TEST)) {
            cognitoUserPool = new CognitoUserPool(mContext, "ap-southeast-1_jityQJ9ye", "35okds2us7ei7876fjf4g2jd4p", "IZqWYL9XSda72yGPjhxe8revrICuvz54FBtJ6/lF", Regions.AP_SOUTHEAST_1);
        } else if (ConstantsActivity.FLAG_ENVIRONMENT.equalsIgnoreCase(ConstantsActivity.FLAG_UAT)) {
            cognitoUserPool = new CognitoUserPool(mContext, "ap-southeast-1_EE1zE8GMT", "2f7amn9bg5877rkvou8c0b24m0", "2zczxLUoKfzMW68i36KUNTWOpaHkL1FbAdaPvGy9", Regions.AP_SOUTHEAST_1);
        } else if (ConstantsActivity.FLAG_ENVIRONMENT.equalsIgnoreCase(ConstantsActivity.FLAG_PROD)) {
            cognitoUserPool = new CognitoUserPool(mContext, "ap-southeast-1_qnF8ucjP1", "3sk23u48ub5ctr35u47m2o4dos", "1723ou3je0elj94ho60566sf4m3qjgnsch6opslkhmh4n8kbaagp", Regions.AP_SOUTHEAST_1);
        } else if (ConstantsActivity.FLAG_ENVIRONMENT.equalsIgnoreCase(ConstantsActivity.FLAG_POC)) {
            cognitoUserPool = new CognitoUserPool(mContext, "ap-southeast-1_9QyRkkZlp", "3icfcrqubcueiv2t1i8h56sb11", "IZqWYL9XSda72yGPjhxe8revrICuvz54FBtJ6/lF", Regions.AP_SOUTHEAST_1);
        } else if (ConstantsActivity.FLAG_ENVIRONMENT.equalsIgnoreCase(ConstantsActivity.FLAG_DDF_UAT)) {
            cognitoUserPool = new CognitoUserPool(mContext, "me-south-1_rvfULdYNo", "69nabm3pk9rkro74haiaso342f", "IZqWYL9XSda72yGPjhxe8revrICuvz54FBtJ6/lF", Regions.ME_SOUTH_1);
        } else if (ConstantsActivity.FLAG_ENVIRONMENT.equalsIgnoreCase(ConstantsActivity.FLAG_DDF_PROD)) {
            cognitoUserPool = new CognitoUserPool(mContext, "me-south-1_oqqjnTDTZ", "3qg0396almtpalkv4k6gm0nta3", "IZqWYL9XSda72yGPjhxe8revrICuvz54FBtJ6/lF", Regions.ME_SOUTH_1);
        } else if (ConstantsActivity.FLAG_ENVIRONMENT.equalsIgnoreCase(ConstantsActivity.FLAG_GEIDEA_DEV)) {
            cognitoUserPool = new CognitoUserPool(mContext, "me-south-1_QObKwnwY6", "743tba3tmdqtpn3ns8tnmhl1ok", "IZqWYL9XSda72yGPjhxe8revrICuvz54FBtJ6/lF", Regions.ME_SOUTH_1);
        } else if (ConstantsActivity.FLAG_ENVIRONMENT.equalsIgnoreCase(ConstantsActivity.FLAG_GEIDEA_TEST)) {
            cognitoUserPool = new CognitoUserPool(mContext, "me-south-1_9wTdxNUlV", "6qk31hio355uipf9qkm8ct5960", "IZqWYL9XSda72yGPjhxe8revrICuvz54FBtJ6/lF", Regions.ME_SOUTH_1);
        } else if (ConstantsActivity.FLAG_ENVIRONMENT.equalsIgnoreCase(ConstantsActivity.FLAG_GEIDEA_UAT)) {
            cognitoUserPool = new CognitoUserPool(mContext, "me-south-1_HEMoQQxG4", "3it8kaftu3e9nbkrhmhquqi1c5", "IZqWYL9XSda72yGPjhxe8revrICuvz54FBtJ6/lF", Regions.ME_SOUTH_1);
        }
        cognitoUserPool.getCurrentUser().getSession(new AuthenticationHandler() {
            @Override
            public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
                LoggerDDF.e("Data@114", userSession.getUsername());
//                LoggerDDF.e("auth Token", userSession.getIdToken().getJWTToken());
//                LoggerDDF.e("getRefreshToken@114", userSession.getRefreshToken()+"");
//                ConstantsApi.API_TEMP_JWT = "";
                ConstantsApi.alToken.clear();
                ConstantsApi.alToken.add(ConstantsApi.API_BEARER + userSession.getIdToken().getJWTToken() + ConstantsApi.API_TEMP_JWT);
                LoggerDDF.e("auth Token@143",  ConstantsApi.alToken.get(0) +"");
                dialogCustomize.displayProgressDialog(mContext, ConstantsDialog.FLAG_DIALOG_DISMISS);
//                openNextActivity();
                LoggerDDF.e(TAG, ConstantsActivity.FLAG_AMPLIFY_NAME);
                amplifySendResult.amplifySend(ConstantsActivity.FLAG_AMPLIFY_NAME);
                LoggerDDF.e("callback completed", "callback completed");
            }

            @Override
            public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String userId) {
                LoggerDDF.e(TAG, authenticationContinuation + " @137");
                LoggerDDF.e(TAG, userId + " @138");
                if (userId == null) {
                    openNextActivity(LoginActivity.class);
                }
            }

            @Override
            public void getMFACode(MultiFactorAuthenticationContinuation continuation) {
                LoggerDDF.e(TAG, continuation + " @142");
            }

            @Override
            public void authenticationChallenge(ChallengeContinuation continuation) {
                LoggerDDF.e(TAG, continuation + " @147");
            }

            @Override
            public void onFailure(Exception exception) {
                LoggerDDF.e(TAG, exception + "@151");
            }
        });
    }

    //
    public void amplifySignOut() {
        LoggerDDF.e(TAG, "signOut");
        Amplify.Auth.signOut(new Action() {
            @Override
            public void call() {
                LoggerDDF.e(TAG, "call");
                openNextActivity(LoginActivity.class);
            }
        }, new Consumer<AuthException>() {
            @Override
            public void accept(@NonNull AuthException value) {
                LoggerDDF.e(TAG, "accept");
            }
        });
    }

    public void getAwsSession() {
        try {
            LoggerDDF.e(TAG, "getAwsSession");
//            LoggerDDF.e(TAG, Amplify.Auth.getCurrentUser().toString()+"");

            Amplify.Auth.fetchAuthSession(
//                result -> Log.e(TAG, result.toString()),
                    result -> getAwsSessionStatus(result),
                    error -> Log.e(TAG, error.toString())
            );
            ConstantsActivity.STR_USER_ID = Amplify.Auth.getCurrentUser().getUsername();
            LoggerDDF.e(TAG, Amplify.Auth.getCurrentUser().getUserId() + "");
            LoggerDDF.e(TAG, Amplify.Auth.getCurrentUser().getUsername() + "");
//            LoggerDDF.e(TAG, Amplify.Auth.fetchAuthSession() + "");
//            getSignInStatus();

        } catch (Exception e) {
            LoggerDDF.e(TAG, e + "");
        }

    }

    public boolean getAwsSessionStatus(AuthSession result) {
        try {
            LoggerDDF.e(TAG, "getAwsSessionStatus");
            LoggerDDF.e(TAG, result.isSignedIn() + " @245");
            if (result.isSignedIn()) {
//                getJWTSessionToken();
                ConstantsActivity.FLAG_AMPLIFY_NAME = ConstantsActivity.FLAG_AMPLIFY_PAY;
                amplifyToken();
            } else {
                ConstantsActivity.FLAG_AMPLIFY_NAME = ConstantsActivity.FLAG_AMPLIFY_LOGIN;
                amplifySendResult.amplifySend(ConstantsActivity.FLAG_AMPLIFY_NAME);
            }

//            String strAuthSession = result.toString() + "";
//            LoggerDDF.e(TAG, strAuthSession + "");
//            String strAuthSessionArray[] = strAuthSession.split("accessToken");
//            LoggerDDF.e(TAG, strAuthSessionArray[1] + "");
//            JSONObject jsonResponseDATA = new JSONObject(strAuthSession);
//            LoggerDDF.e(TAG, jsonResponseDATA.getString("accessToken") + "");
        } catch (Exception e) {
            Log.e(TAG, e + "");
        }

        return true;
    }

    public void getJWTSessionToken() {
        Amplify.Auth.fetchAuthSession(
                result -> {
                    AWSCognitoAuthSession cognitoAuthSession = (AWSCognitoAuthSession) result;
                    if (cognitoAuthSession.getUserPoolTokens().getType() == AuthSessionResult.Type.FAILURE) {
                        // Handling no session here.
                    }
                    String jwtToken = cognitoAuthSession.getUserPoolTokens().getValue().getIdToken();
                    Log.e(TAG + " @248", jwtToken);

//                        String token = cognitoAuthSession.getUserPoolTokens().getValue().getAccessToken();
//                        String newToken = cognitoAuthSession.getAWSCredentials().toString();
//                        Log.e(TAG+" @248",token);
//                        Log.e(TAG+" @248",newToken);
                },
                error -> Log.e("AuthQuickStart", error.toString())
        );
    }

    //
    public void resetPassword(String userName) {
        Amplify.Auth.resetPassword(
                userName,
                result -> {
                    Log.e("result ", result.toString());
                    ToastCustomize.displayToast(mContext, "Please Check Your Email For Verification Code");
                    openVerifyActivity(EmailVerification.class);
                },
                error -> {
                    Log.e("error ", error.toString());
                    ToastCustomize.displayToast(mContext, "Email Sending Failed Please Enter Valid Email");
                }
        );
    }
    //
    public void resendPassword(String userName) {
        Amplify.Auth.resetPassword(
                userName,
                result -> {
                    Log.e("result ", result.toString());
                    ToastCustomize.displayToast(mContext, "Please Check Your Email For Verification Code");
//                    openNextActivity(EmailVerification.class);
                },
                error -> {
                    Log.e("error ", error.toString());
                    ToastCustomize.displayToast(mContext, "Email Sending Failed Please Enter Valid Email");
                }
        );
    }

    public void confirmPassword(String newPassword, String emailCode) {
        Amplify.Auth.confirmResetPassword(
                newPassword,
                emailCode,
                () -> {
                    Log.e("result ", "New password confirmed");
                    ToastCustomize.displayToast(mContext, "Please Login Again Because Your Password Changed");
                    openClearActivity(LoginActivity.class);
                },
                error -> {
                    Log.e("error ", error.toString());
                    ToastCustomize.displayToast(mContext, "Invalid OTP");
                }
        );
    }

    //
    public void deletePassword(String userName) throws Exception {
        /*Amplify.Auth.deleteUser(
                () -> Log.e("AuthQuickStart", "Delete user succeeded"),
                error -> Log.e("AuthQuickStart", "Delete user failed with error " + error.toString())
        );*/
        AWSMobileClient.getInstance().deleteUser();
    }

    public void amplifyReSignIn(String userId, String password) {
        ConstantsActivity.FLAG_AMPLIFY_RESIGN ="AMPLIFY_RESIGN";
        amplifySignOutV2(userId, password);
    }

    public void amplifySignInV2(String userId, String password) {
//        ConstantsActivity.STR_USER_ID = userId;
//        dialogCustomize.displayProgressDialog(mContext, ConstantsDialog.FLAG_DIALOG_DISPLAY);
        LoggerDDF.e(TAG, "amplifySignInV2");
        Amplify.Auth.signIn(
                userId,
                password,
//                result -> LoggerDDF.e("AuthQuickstart", result.isSignInComplete() ? "Sign in succeeded" : "Sign in not complete"),
                result -> amplifyTokenV2(),
//                error -> LoggerDDF.e("AuthQuickstart", error.toString())
                error -> {
                    LoggerDDF.e("error ", error.toString());
                    signInFailed();
                }
        );
    }
    public void amplifySignOutV2(String userId, String password) {
        LoggerDDF.e(TAG, "signOut");
        Amplify.Auth.signOut(new Action() {
            @Override
            public void call() {
                LoggerDDF.e(TAG, "call@343");
                amplifySignInV2(userId,password);
            }
        }, new Consumer<AuthException>() {
            @Override
            public void accept(@NonNull AuthException value) {
                LoggerDDF.e(TAG, "accept@350");
                amplifySignInV2(userId,password);
            }
        });
    }

    public void amplifyTokenV2() {
        LoggerDDF.e(TAG, "amplifyTokenV2@118");
        if (ConstantsActivity.FLAG_ENVIRONMENT.equalsIgnoreCase(ConstantsActivity.FLAG_DEV)) {
            cognitoUserPool = new CognitoUserPool(mContext, "eu-central-1_YHGC0AdSw", "2pge6nhui8edjckr5fts5o0a1u", "XS9WbHlnjVP7LR6QI4Z5QrPHynzBisa4cWcNYLDm", Regions.EU_CENTRAL_1);
        } else if (ConstantsActivity.FLAG_ENVIRONMENT.equalsIgnoreCase(ConstantsActivity.FLAG_TEST)) {
            cognitoUserPool = new CognitoUserPool(mContext, "ap-southeast-1_jityQJ9ye", "35okds2us7ei7876fjf4g2jd4p", "IZqWYL9XSda72yGPjhxe8revrICuvz54FBtJ6/lF", Regions.AP_SOUTHEAST_1);
        } else if (ConstantsActivity.FLAG_ENVIRONMENT.equalsIgnoreCase(ConstantsActivity.FLAG_UAT)) {
            cognitoUserPool = new CognitoUserPool(mContext, "ap-southeast-1_EE1zE8GMT", "2f7amn9bg5877rkvou8c0b24m0", "2zczxLUoKfzMW68i36KUNTWOpaHkL1FbAdaPvGy9", Regions.AP_SOUTHEAST_1);
        } else if (ConstantsActivity.FLAG_ENVIRONMENT.equalsIgnoreCase(ConstantsActivity.FLAG_PROD)) {
            cognitoUserPool = new CognitoUserPool(mContext, "ap-southeast-1_qnF8ucjP1", "3sk23u48ub5ctr35u47m2o4dos", "1723ou3je0elj94ho60566sf4m3qjgnsch6opslkhmh4n8kbaagp", Regions.AP_SOUTHEAST_1);
        } else if (ConstantsActivity.FLAG_ENVIRONMENT.equalsIgnoreCase(ConstantsActivity.FLAG_POC)) {
            cognitoUserPool = new CognitoUserPool(mContext, "ap-southeast-1_9QyRkkZlp", "3icfcrqubcueiv2t1i8h56sb11", "IZqWYL9XSda72yGPjhxe8revrICuvz54FBtJ6/lF", Regions.AP_SOUTHEAST_1);
        }
        cognitoUserPool.getCurrentUser().getSession(new AuthenticationHandler() {
            @Override
            public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
                LoggerDDF.e("Data@114", userSession.getUsername());
//                LoggerDDF.e("auth Token", userSession.getIdToken().getJWTToken());
//                LoggerDDF.e("getRefreshToken@114", userSession.getRefreshToken()+"");

                ConstantsApi.alToken.clear();
                ConstantsApi.alToken.add(ConstantsApi.API_BEARER + userSession.getIdToken().getJWTToken()+ConstantsApi.API_TEMP_JWT);
                LoggerDDF.e("auth Token@143",  ConstantsApi.alToken.get(0) +"");
//                dialogCustomize.displayProgressDialog(mContext, ConstantsDialog.FLAG_DIALOG_DISMISS);
//                openNextActivity();
                LoggerDDF.e(TAG, ConstantsActivity.FLAG_AMPLIFY_NAME);
                LoggerDDF.e("callback completed", "callback completed");
            }

            @Override
            public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String userId) {
                LoggerDDF.e(TAG, authenticationContinuation + " @137");
                LoggerDDF.e(TAG, userId + " @138");
                if (userId == null) {
                    openNextActivity(LoginActivity.class);
                }
            }

            @Override
            public void getMFACode(MultiFactorAuthenticationContinuation continuation) {
                LoggerDDF.e(TAG, continuation + " @142");
            }

            @Override
            public void authenticationChallenge(ChallengeContinuation continuation) {
                LoggerDDF.e(TAG, continuation + " @147");
            }

            @Override
            public void onFailure(Exception exception) {
                LoggerDDF.e(TAG, exception + "@151");
            }
        });
    }

    //
    static Activity mActivity;

    private void openNextActivity(Class intentScreen) {
        mActivity.finishAffinity();
        Intent i = new Intent(mContext, intentScreen);
//        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        mActivity.startActivity(i);
        mActivity.finish();
    }
    private void openVerifyActivity(Class intentScreen) {
//        mActivity.finishAffinity();
        Intent i = new Intent(mContext, intentScreen);
//        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        mActivity.startActivity(i);
//        mActivity.finish();
    }
    private void openClearActivity(Class intentScreen) {
        Intent i = new Intent(mContext, intentScreen);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mActivity.startActivity(i);
        mActivity.finish();
    }
}
