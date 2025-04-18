import { configureStore } from "@reduxjs/toolkit"

import appConfigReducer from "./appConfigSlice"
import institutionCurrenciesReducer from "./institutionCurrenciesSlice"
import settingsSliceReducer from "./settingsSlice"
import { APP_CONFIGURATION, INSTITUTION_CURRENCIES, SETTINGS } from "./modules"

// Define the initial state interfaces
interface InitialAppState {
  appConfig: APP_CONFIGURATION
  institutionCurrencies: INSTITUTION_CURRENCIES
  settingsConfig: SETTINGS
}

// Define the initial state
const initialAppState: InitialAppState = {
  appConfig: {
    activeNavigation: "/",
    issuanceType: "bullion",
    themeColor: "#ffffff",
    buttonColor: "#FFC235"
  },
  institutionCurrencies: {
    defaultCurrency: "MYR",
    destinationCurrency: "SART"
  },
  settingsConfig: {
    p2pTransfer: true
  }
}

// Configure the Redux store with the initial state
const appStore = configureStore({
  reducer: {
    appConfig: appConfigReducer,
    institutionCurrencies: institutionCurrenciesReducer,
    settingsConfig: settingsSliceReducer
  },
  preloadedState: initialAppState
})

export default appStore
