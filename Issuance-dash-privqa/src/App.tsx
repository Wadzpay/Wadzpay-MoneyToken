import React from "react"
import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom"
import { QueryClient, QueryClientProvider } from "react-query"
import { Provider } from "react-redux"

import "./App.scss"
import "./i18n"
import { OnboardingContextProvider } from "./context"
import { UserContextProvider } from "./context/User"
import PrivateRoute from "./auth/PrivateRoute"
import PublicRoute from "./auth/PublicRoute"
import AcceptInvite from "./screens/Onboarding/AcceptInvite"
import AcceptInvitations from "./screens/Onboarding/AcceptInvitations"
import Success from "./screens/Onboarding/Success"
import ResetSuccess from "./screens/ResetPassword/ResetSuccess"
import UpdateSuccess from "./screens/Settings/UpdatePassword/UpdateSuccess"
import RequestResetPasswordCode from "./screens/ResetPassword/RequestResetPasswordCode"
import SubmitResetPasswordCode from "./screens/ResetPassword/SubmitResetPasswordCode"
import { RouteType } from "./constants/routeTypes"
import Dashboard from "./screens/Dashboard/Dashboard"
import Page404 from "./screens/Error/Page404"
import SignIn from "./screens/SignIn"
import Settings from "./screens/Settings/Settings"
import AdminUsers from "./screens/Admin/Users"
import ChangePassword from "./screens/Settings/UpdatePassword/ChangePassword"
import FIAT_CURRENT_LIMIT from "./screens/Settings/fiatCurrentLimits/FiatCurrentLimits"
import AdminInviteUser from "./screens/Admin/InviteUser"
import AdminAPIKeys from "./screens/Admin/APIKeys"
import AdminCreateAPIKey from "./screens/Admin/CreateAPIKey"
import { IssuanceContextProvider } from "./context/Merchant"
import Navigation from "./components/navigation/Navigation"
import Topbar from "./components/navigation/Topbar"
import WalletsList from "./screens/WalletsList/WalletsList"
import WalletParameter from "./screens/Configurations/WalletParameter"
import TransactionLimits from "./screens/Configurations/TransactionLimits"
import ConversionRates from "./screens/Configurations/ConversionRates"
import ConversionRatesAdjustment from "./screens/Configurations/ConversionRatesAdjustment"
import InstitutionManagement from "./screens/institutionManagement/InstitutionManagement"
import InstitutionRegister from "./screens/institutionManagement/InstitutionRegister"
import RoleManagement from "./screens/roleManagement/RoleManagement"
import CreateRole from "./screens/roleManagement/CreateRole"
import UserManagement from "./screens/userManagement/UserManagement"
import ComingSoon from "./components/ui/ComingSoon"
import appStore from "./utils/appStore"
import Languages from "./screens/Configurations/Languages"
import AddLanguage from "./screens/Configurations/AddLanguage"
import Logo from "./screens/Configurations/Logo"

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false
    }
  }
})

function App(): JSX.Element {
  if (window !== window.top) {
    return <div>This Page Refused To Connect.</div>
  }
  return (
    <Provider store={appStore}>
      <div className="App">
        <UserContextProvider>
          <QueryClientProvider client={queryClient}>
            <BrowserRouter>
              <IssuanceContextProvider>
                <div className="container-fluid mb-4">
                  <div className="row">
                    <div
                      className="col-sm-auto bg-light sticky-top bg-white navigation-menu"
                      id="navigation-container"
                    >
                      <Navigation />
                    </div>
                    <div className="col-sm p-3 min-vh-100 mt-20 col-lg-11 col-sm-12 container-full navigation-container-mobile">
                      <div className="header fixed-top bg-white" id="topHeader">
                        <div className="mainLogo"></div>
                        <Topbar />
                      </div>
                      <div className="main-container" id="main-container">
                        <div className="container-body">
                          <OnboardingContextProvider>
                            <Routes>
                              {/* Public route */}
                              <Route element={<PublicRoute />}>
                                <Route
                                  path={RouteType.SIGN_IN}
                                  element={<SignIn />}
                                />
                                <Route
                                  path={RouteType.ACCEPT_INVITE}
                                  element={<AcceptInvite />}
                                ></Route>
                                <Route
                                  path={RouteType.ACCEPT_INVITATIONS}
                                  element={<AcceptInvitations />}
                                ></Route>
                              </Route>

                              {/* Private route */}
                              <Route element={<PrivateRoute />}>
                                <Route
                                  path={RouteType.HOME}
                                  element={<Dashboard />}
                                />
                                <Route
                                  path={`${RouteType.WALLETSLIST}`}
                                  element={<WalletsList />}
                                />
                                <Route
                                  path={RouteType.SETTINGS}
                                  element={<Settings />}
                                />
                                <Route
                                  path={RouteType.WALLET_PARAMETER}
                                  element={<WalletParameter />}
                                />
                                <Route
                                  path={RouteType.TRANSACTION_LIMITS}
                                  element={<TransactionLimits />}
                                />
                                <Route
                                  path={RouteType.CONVERSION_RATES}
                                  element={<ConversionRates />}
                                />
                                <Route
                                  path={RouteType.CONVERSION_RATES_ADJUSTMENT}
                                  element={<ConversionRatesAdjustment />}
                                />
                                <Route
                                  path={RouteType.LANGUAGES}
                                  element={<Languages />}
                                />
                                <Route
                                  path={RouteType.ADD_LANGUAGE}
                                  element={<AddLanguage />}
                                />
                                <Route
                                  path={RouteType.EDIT_LANGUAGE}
                                  element={<AddLanguage />}
                                />
                                <Route
                                  path={RouteType.INSTITUTION_LANGUAGES}
                                  element={<Languages />}
                                />
                                <Route
                                  path={RouteType.LOGO}
                                  element={<Logo />}
                                />
                                <Route
                                  path={RouteType.INSTITUTION_MANAGEMENT}
                                  element={
                                    <Navigate to={RouteType.HOME} replace />
                                  }
                                />

                                <Route
                                  path={RouteType.INSTITUTION_REGISTER}
                                  element={<InstitutionRegister />}
                                />
                                <Route
                                  path={RouteType.ROLE_MANAGEMENT}
                                  element={
                                    <Navigate to={RouteType.HOME} replace />
                                  }
                                />
                                <Route
                                  path={RouteType.ROLE_CREATE}
                                  element={<CreateRole />}
                                />
                                <Route
                                  path={RouteType.USER_MANAGEMENT}
                                  element={
                                    <Navigate to={RouteType.HOME} replace />
                                  }
                                />
                                <Route
                                  path={RouteType.ADMIN_USERS}
                                  element={<AdminUsers />}
                                />
                                <Route
                                  path={RouteType.ADMIN_USERS_ROLES}
                                  element={<ComingSoon />}
                                />
                                <Route
                                  path={RouteType.ADMIN_USERS_INVITE}
                                  element={<ComingSoon />}
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
                                  path={RouteType.CHANGE_PASSWORD}
                                  element={<ChangePassword />}
                                />
                                <Route
                                  path={RouteType.FIAT_CURRENT_LIMIT}
                                  element={<FIAT_CURRENT_LIMIT />}
                                />
                              </Route>

                              {/* Private Route or public route */}
                              <Route
                                path={RouteType.RESET_PASSWORD}
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
                              {!RouteType.REFUND_FORM_TOKEN && (
                                <Route path="*" element={<Page404 />} />
                              )}
                            </Routes>
                          </OnboardingContextProvider>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </IssuanceContextProvider>
            </BrowserRouter>
          </QueryClientProvider>
        </UserContextProvider>
      </div>
    </Provider>
  )
}

export default App
