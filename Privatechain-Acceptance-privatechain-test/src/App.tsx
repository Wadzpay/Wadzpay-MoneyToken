import React, { useContext, useEffect, useState } from "react";
import { BrowserRouter, Link, Navigate, Route, Routes } from "react-router-dom";
import { QueryClient, QueryClientProvider } from "react-query";

import "./App.scss";
import "./i18n";
import { OnboardingContextProvider } from "./context";
import { UserContextProvider } from "./context/User";
import PrivateRoute from "./auth/PrivateRoute";
import PublicRoute from "./auth/PublicRoute";
import SignOut from "./components/ui/SignOut";
import ResetSuccess from "./screens/ResetPassword/ResetSuccess";
import UpdateSuccess from "./screens/Settings/UpdatePassword/UpdateSuccess";
import RequestResetPasswordCode from "./screens/ResetPassword/RequestResetPasswordCode";
import SubmitResetPasswordCode from "./screens/ResetPassword/SubmitResetPasswordCode";
import VerifyPhoneCode from "./screens/Onboarding/VerifyPhoneCode";
import VerifyEmailCode from "./screens/Onboarding/VerifyEmailCode";
import { RouteType } from "./constants/routeTypes";
import Success from "./screens/Onboarding/Success";
import AccountDetails from "./screens/Onboarding/AccountDetails";
import Dashboard from "./screens/Dashboard/Dashboard";
import Page404 from "./screens/Error/Page404";
import CreateAccount from "./screens/Onboarding/CreateAccount";
import SignIn from "./screens/SignIn";
import Transactions from "./screens/Transactions/Transactions";
import TransactionDetail from "./screens/Dashboard/TransactionDetails";
import Invoice from "./screens/Invoice/Invoice";
import Settings from "./screens/Settings/Settings";
import MerchantDetails from "./screens/Onboarding/MerchantDetails";
import P2PTransaction from "./screens/Dashboard/P2PTransaction";
import Admin from "./screens/Admin";
import Refund from "./screens/Refund/Refund";
import RefundReqAcceptance from "./screens/RefundAcceptance/RefundReqAcceptance";
import RefundReqApproval from "./screens/RefundApproval/RefundReqApproval";
import AdminUsers from "./screens/Admin/Users";
import ChangePassword from "./screens/Settings/UpdatePassword/ChangePassword";
import AdminInviteUser from "./screens/Admin/InviteUser";
import AdminAPIKeys from "./screens/Admin/APIKeys";
import AdminCreateAPIKey from "./screens/Admin/CreateAPIKey";
import AcceptInvite from "./screens/Onboarding/AcceptInvite";
import RefundFormManual from "./screens/RefundForm/RefundFormManual";
import { MerchantContext, MerchantContextProvider } from "./context/Merchant";
import { SaveContextProvider } from "./context/SaveContext";
import MerchantName from "./components/ui/MerchantName";
import Navigation from "./components/navigation/Navigation";
import RefundForm from "./screens/RefundForm/RefundForm";
import CustomerRefundForm from "./screens/RefundForm/CustomerRefundForm";
import RequestTransaction from "./screens/RequestTransaction/requestTransaction";
import ATM from "./screens/ATM/ATM";
import ATMEntry from "./screens/ATM/ATMEntry";
import ATMQrCode from "./screens/ATM/ATMQrCode";
import ATMDisbursement from "./screens/ATM/ATMDisbursement";
import { WADZPAY_LOGO } from "./constants/Defaults";
import AggregatorManagement from "./screens/AggregatorManagement/AggregatorManagement";
import AggregatorRegister from "./screens/AggregatorManagement/AggregatorRegister";
import RoleManagement from "./screens/roleManagement/RoleManagement";
import UserManagement from "./screens/userManagement/UserManagement";
import InstitutionRegister from "./screens/AggregatorManagement/InstitutionManagement/InstitutionRegister";
import InstitutionList from "./screens/AggregatorManagement/InstitutionManagement/InstitutionList";
import MerchantGroupRegister from "./screens/AggregatorManagement/MerchantGroupManagement/MerchantGroupRegister";
import MerchantGroupList from "./screens/AggregatorManagement/MerchantGroupManagement/MerchantGroupList";
import MerchantRegister from "./screens/AggregatorManagement/MerchantManagement/MerchantRegister";
import MerchantList from "./screens/AggregatorManagement/MerchantManagement/MerchantList";
import SubMerchantRegister from "./screens/AggregatorManagement/SubMerchantManagement/SubMerchantRegister";
import SubMerchantList from "./screens/AggregatorManagement/SubMerchantManagement/SubMerchantList";
import OutletList from "./screens/AggregatorManagement/OutletManagement/OutletList";
import OutletRegister from "./screens/AggregatorManagement/OutletManagement/OutletRegister";
import EditOutlet from "./screens/AggregatorManagement/OutletManagement/EditOutlet";
import { OutletContextProvider } from "./screens/AggregatorManagement/OutletManagement/context/OutletContext";
import AddPos from "./screens/AggregatorManagement/PosManagement/Pos";
import PosList1 from "./screens/AggregatorManagement/PosManagement/PosList";
import PosList from "./screens/AggregatorManagement/PosManagement/PosList";
import LevelManagement from "./screens/levelManagement/LevelManagement";
import ModuleManagement from "./screens/moduleManagement/ModuleManagement";
import { LevelContext, LevelContextProvider } from "./context/Level";
import env from "src/env";
import { getIdTokenAsync } from "./auth/AuthManager";

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
    },
  },
});

function App(): JSX.Element {
  console.log(window.location.pathname);
  const { levelNumber } = useContext(LevelContext);
  const [merchDetails, setMerchantDetails] = useState({
    merchantName: "",
    merchantLogo:
      "",
    merchantCurrency: "",
  });

  const { merchantDetails, institutionDetails } = useContext(MerchantContext);

  // merchantDashboard/merchantDetails
  // console.log("mercg", merchantDetails, institutionDetails);

  const fetchDetails = async () => {
    const response = await fetch(
      `${env.PUBLIC_API_URL}merchantDashboard/merchantDetails`,
      {
        method: "GET", // Assuming it's a GET request
        headers: {
          Authorization: `Bearer ${await getIdTokenAsync()}`, // Use the token from localStorage
          "Content-Type": "application/json", // Optional, based on your API needs
        },
      }
    );

    if (!response.ok) {
      throw new Error("Failed to fetch merchant details");
    }
    return response.json();
  };

  const fetchMerchantDetails = async (name: any) => {
    console.log("gg", name);
    const response = await fetch(
      `${env.PUBLIC_API_URL}merchant/getDetails?merchantName=${name}&lang=English`,
      {
        method: "GET", // Assuming it's a GET request
        headers: {
          Authorization: `Bearer ${await getIdTokenAsync()}`, // Use the token from localStorage
          "Content-Type": "application/json", // Optional, based on your API needs
        },
      }
    );

    if (!response.ok) {
      throw new Error("Failed to fetch merchant details");
    }
    return response.json();
  };
  const getMerchantDetails = async (name: any) => {
    try {
      const data = await fetchMerchantDetails(name);
      console.log("Merchant Details:", data);
      setMerchantDetails(data);
      // You can also update state here if you want to store the result in the component's state
    } catch (error) {
      console.error("Error fetching merchant details:", error);
    }
  };

  const getMerchDetails = async () => {
    try {
      const data = await fetchDetails();
      console.log(" Details:", data?.merchant);
      // setDetails(data?.merchant);
      getMerchantDetails(data?.merchant?.name);
      // You can also update state here if you want to store the result in the component's state
    } catch (error) {
      console.error("Error fetching merchant details:", error);
    }
  };

  useEffect(() => {
    getMerchDetails();

    // getMerchantDetails(); // Call the async function
  }, []);

  // console.log("data", instDetails);
  if (window !== window.top) {
    return (
      <div
        style={{
          height: "600px",
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          color: "gray",
        }}
      >
        <h4>This Page Refused To Connect....</h4>
      </div>
    );
  }
  return (
    <div className="App">
      <UserContextProvider>
        <LevelContextProvider>
          <QueryClientProvider client={queryClient}>
            <BrowserRouter>
              <MerchantContextProvider>
                <SaveContextProvider>
                  {/* <OnboardingContextProvider>
                <Routes>
                  <Route
                    path={RouteType.REFUND_FORM_TOKEN}
                    element={<CustomerRefundForm />}
                  ></Route>
                </Routes>
              </OnboardingContextProvider> */}
                  <div className="container-fluid mb-4">
                    {/* <div
                  className={
                    window.location.pathname !== RouteType.REFUND_FORM_TOKEN
                      ? `row dashBoardBg`
                      : `refundBg`
                  }
                > */}
                    <div className="row">
                      <div
                        className="col-sm-auto bg-light sticky-top bg-white navigation-menu"
                        id="navigation-container"
                      >
                        <Navigation />
                      </div>
                      <div
                        className="col-sm p-3 min-vh-100 mt-20  col-sm-12 container-full navigation-container-mobile"
                        style={{ background: "white" }}
                      >
                        <div className="header fixed-top" id="topHeader">
                          <div className="container-fluid d-flex justify-content-between header-row align-items-center mt-1">
                            <div className="d-flex align-items-center">
                              <div className="logo-container">
                                {WADZPAY_LOGO ? (
                                  <picture>
                                    <source
                                      // srcSet="/images/dubai.png"
                                      srcSet="/images/black_logo.svg"
                                      type="image/webp"
                                    />
                                    <img
                                      // src="/images/white-logo.png"
                                      src="/images/black_logo.svg"
                                      alt="WadzPay Logo"
                                      title="WadzPay Logo"
                                    />
                                  </picture>
                                ) : (
                                  <picture>
                                    <source
                                      // srcSet="/images/dubai.png"
                                      srcSet="/images/black_logo.svg"
                                      type="image/webp"
                                    />
                                    <img
                                      // src="/images/white-logo.png"
                                      src="/images/black_logo.svg"
                                      alt="DDF Logo"
                                      title="DDF Logo"
                                      width="65px"
                                    />
                                  </picture>
                                )}
                              </div>
                              <div className="mx-3" data-testid="header">
                                Acceptance Portal
                                <img
                                  src={merchDetails?.merchantLogo}
                                  alt="Logo"
                                  className="me-2" // Add margin on the right side of the logo to separate it from the text
                                  style={{ height: "40px" }} // Adjust the height of the logo if necessary
                                />
                              </div>
                              <div
                                className="mx-3 text-nowrap"
                                data-testid="merchantName"
                              >
                                {/* <MerchantName /> */}
                              </div>
                            </div>
                            <div className="d-flex justify-content-end me-1 text-nowrap align-items-right flex-column mt-3">
                              <SignOut />
                            </div>
                          </div>
                        </div>
                        <div
                          className={
                            window.location.pathname ===
                            "/admin/role-management"
                              ? "main-container-role"
                              : "main-container"
                          }
                          id="main-container"
                        >
                          <div
                            className={
                              window.location.pathname ===
                              "/admin/role-management"
                                ? "container-body-role"
                                : "container-body"
                            }
                          >
                            <OnboardingContextProvider>
                              {/*                             <OutletContextProvider>
                               */}{" "}
                              <Routes>
                                {/* Public route */}
                                <Route element={<PublicRoute />}>
                                  <Route
                                    path={RouteType.VERIFY_PHONE_CODE}
                                    element={<VerifyPhoneCode />}
                                  />

                                  {/* <Route
                              path={RouteType.REFUND_FORM_TOKEN}
                              element={<CustomerRefundForm />}
                            ></Route> */}

                                  <Route
                                    path={RouteType.ONBOARDING_SUCCESS}
                                    element={<Success />}
                                  />
                                  <Route
                                    path={RouteType.ACCOUNT_DETAILS}
                                    element={<AccountDetails />}
                                  />
                                  <Route
                                    path={RouteType.VERIFY_EMAIL_CODE}
                                    element={<VerifyEmailCode />}
                                  />
                                  <Route
                                    path={RouteType.CREATE_ACCOUNT}
                                    element={<CreateAccount />}
                                  />

                                  <Route
                                    path={RouteType.SIGN_IN}
                                    element={<SignIn />}
                                  />

                                  <Route
                                    path={RouteType.ACCEPT_INVITE}
                                    element={<AcceptInvite />}
                                  ></Route>
                                </Route>

                                {/* Private route */}
                                <Route element={<PrivateRoute />}>
                                  <Route
                                    path={RouteType.HOME}
                                    element={<Dashboard />}
                                  />
                                  <Route
                                    path={`${RouteType.TRANSACTIONS}`}
                                    element={<Transactions />}
                                  />
                                  <Route
                                    path={`${RouteType.TRANSACTION_DETAIL}/:transactionId`}
                                    element={<TransactionDetail />}
                                  />
                                  <Route
                                    path={RouteType.INVOICE}
                                    element={<Invoice />}
                                  ></Route>
                                  {/* <Route
                              path={RouteType.REFUND_DISPUTE}
                              element={<P2PTransaction />}
                            >
                              <Route
                                path={`${RouteType.REFUND_DISPUTE}/:transactionId`}
                                element={<P2PTransaction />}
                              />
                            </Route> */}
                                  <Route
                                    path={RouteType.REFUND_DISPUTE}
                                    element={<Refund />}
                                  />
                                  <Route
                                    path={RouteType.REFUND_REQUEST_ACCEPTANCE}
                                    element={<RefundReqAcceptance />}
                                  />
                                  <Route
                                    path={RouteType.REFUND_REQUEST_APPROVAL}
                                    element={<RefundReqApproval />}
                                  />
                                  <Route
                                    path={RouteType.SETTINGS}
                                    element={<Settings />}
                                  />
                                  <Route
                                    path={RouteType.LEVEL_MANAGEMENT}
                                    element={<LevelManagement />}
                                  />
                                  <Route
                                    path={RouteType.MODULE_MANAGEMENT}
                                    element={<ModuleManagement />}
                                  />
                                  <Route
                                    path={RouteType.ADMIN_USERS}
                                    element={<AdminUsers />}
                                  />
                                  <Route
                                    path={RouteType.ADMIN_USERS_INVITE}
                                    element={<AdminInviteUser />}
                                  />
                                  <Route
                                    path={RouteType.ADMIN_API_KEYS}
                                    element={<AdminAPIKeys />}
                                  />
                                  <Route
                                    path={RouteType.ADMIN_API_KEYS_CREATE}
                                    element={<AdminCreateAPIKey />}
                                  />

                                  <Route
                                    path={RouteType.MERCHANT_DETAILS}
                                    element={<MerchantDetails />}
                                  />

                                  <Route
                                    path={RouteType.CHANGE_PASSWORD}
                                    element={<ChangePassword />}
                                  />

                                  <Route
                                    path={
                                      RouteType.CUSTOMER_MANUAL_VERIFICATION_FORM
                                    }
                                    element={<RefundFormManual />}
                                  />

                                  <Route
                                    path={RouteType.AGGREGATOR_MANAGEMENT}
                                    element={
                                      <Navigate to={RouteType.HOME} replace />
                                    }
                                  />
                                  <Route
                                    path={RouteType.AGGREGATOR_REGISTER}
                                    element={<AggregatorRegister />}
                                  />
                                  <Route
                                    path={RouteType.AGGREGATOR_UPDATE}
                                    element={<AggregatorRegister />}
                                  />

                                  <Route
                                    path={`${RouteType.INSTITUTION_REGISTER}/:aggregatorID/:refId`}
                                    element={<InstitutionRegister />}
                                  />
                                  <Route
                                    path={`${RouteType.INSTITUTION_LIST}/:aggregatorID/:refId`}
                                    element={<InstitutionList />}
                                  />

                                  <Route
                                    path={`${RouteType.MERCHANT_GROUP_REGISTER}/:aggregatorID/:aggregatorName/:instituteID/:instituteName`}
                                    element={<MerchantGroupRegister />}
                                  />

                                  <Route
                                    path={`${RouteType.MERCHANT_GROUP_LIST}/:aggregatorID/:aggregatorName/:instituteID/:instituteName`}
                                    element={<MerchantGroupList />}
                                  />

                                  <Route
                                    path={`${RouteType.MERCHANT_REGISTER}/:aggregatorID/:aggregatorName/:instituteID/:instituteName/:merchantGroupID/:merchantGroupName`}
                                    element={<MerchantRegister />}
                                  />
                                  <Route
                                    path={`${RouteType.MERCHANT_LIST}/:aggregatorID/:aggregatorName/:instituteID/:instituteName/:merchantGroupID/:merchantGroupName`}
                                    element={<MerchantList />}
                                  />
                                  <Route
                                    path={`${RouteType.SUB_MERCHANT_REGISTER}/:aggregatorID/:aggregatorName/:instituteID/:instituteName/:merchantGroupID/:merchantGroupName/:merchantAcquirerId/:merchantAcquirerName`}
                                    element={<SubMerchantRegister />}
                                  />
                                  <Route
                                    path={`${RouteType.SUB_MERCHANT_LIST}/:aggregatorID/:aggregatorName/:instituteID/:instituteName/:merchantGroupID/:merchantGroupName/:merchantAcquirerId/:merchantAcquirerName`}
                                    element={<SubMerchantList />}
                                  />
                                  <Route
                                    path={`${RouteType.OUTLET_LIST}/:aggregatorID/:aggregatorName/:instituteID/:instituteName/:merchantGroupID/:merchantGroupName/:merchantAcquirerId/:merchantAcquirerName/:subMerchantId/:subMerchantName`}
                                    element={<OutletList />}
                                  />
                                  <Route
                                    path={`${RouteType.POS_LIST}/:aggregatorID/:aggregatorName/:instituteID/:instituteName/:merchantGroupID/:merchantGroupName/:merchantAcquirerId/:merchantAcquirerName/:subMerchantId/:subMerchantName/:outletId/:outletName`}
                                    element={<PosList />}
                                  />

                                  <Route
                                    path={`${RouteType.OUTLET_REGISTER}/:aggregatorID/:aggregatorName/:instituteID/:instituteName/:merchantGroupID/:merchantGroupName/:merchantAcquirerId/:merchantAcquirerName/:subMerchantId/:subMerchantName`}
                                    element={<OutletRegister />}
                                  />
                                  <Route
                                    path={`${RouteType.POS_REGISTER}/:aggregatorID/:aggregatorName/:instituteID/:instituteName/:merchantGroupID/:merchantGroupName/:merchantAcquirerId/:merchantAcquirerName/:subMerchantId/:subMerchantName/:outletId/:outletName`}
                                    element={<AddPos />}
                                  />

                                  {/* <Route
                                  path={`${RouteType.OUTLET_EDIT}/:aggregatorID/:aggregatorName/:instituteID/:instituteName/:merchantGroupID/:merchantGroupName/:merchantAcquirerId/:merchantAcquirerName/:subMerchantId/:subMerchantName`}
                                  element={<EditOutlet/>}
                                /> */}

                                  <Route
                                    path={
                                      RouteType.ROLE_MANAGEMENT +
                                      `/:levelNumber`
                                    }
                                    element={<RoleManagement />}
                                  />
                                  <Route
                                    path={
                                      RouteType.USER_MANAGEMENT +
                                      `/:levelNumber`
                                    }
                                    element={<UserManagement />}
                                  />
                                </Route>

                                {/* Private Route or public route */}
                                <Route
                                  path={RouteType.FORGOT_PASSWORD}
                                  element={<RequestResetPasswordCode />}
                                />
                                <Route
                                  path={RouteType.SUBMIT_RESET_PASSWORD}
                                  element={<SubmitResetPasswordCode />}
                                />
                                <Route
                                  path={RouteType.RESET_PASSWORD_SUCCESS}
                                  element={<ResetSuccess />}
                                />
                                <Route
                                  path={RouteType.CHANGE_PASSWORD_SUCCESS}
                                  element={<UpdateSuccess />}
                                />
                                <Route
                                  path={RouteType.REFUND_FORM_TOKEN}
                                  element={<CustomerRefundForm />}
                                ></Route>
                                <Route
                                  path={RouteType.ATM}
                                  element={<ATM />}
                                ></Route>
                                <Route
                                  path={RouteType.ATM_ENTRY}
                                  element={<ATMEntry />}
                                ></Route>
                                <Route
                                  path={RouteType.ATM_ENTRY_QR_CODE}
                                  element={<ATMQrCode />}
                                ></Route>
                                <Route
                                  path={RouteType.ATM_DISBURSEMENT}
                                  element={<ATMDisbursement />}
                                ></Route>
                                {/* <Route
                            path={RouteType.RequestTransaction}
                            element={<RequestTransaction />}
                          ></Route>*/}
                                {!RouteType.REFUND_FORM_TOKEN && (
                                  <Route path="*" element={<Page404 />} />
                                )}
                              </Routes>
                              {/*                             </OutletContextProvider>
                               */}{" "}
                            </OnboardingContextProvider>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </SaveContextProvider>
              </MerchantContextProvider>
            </BrowserRouter>
          </QueryClientProvider>
        </LevelContextProvider>
      </UserContextProvider>
    </div>
  );
}

export default App;
