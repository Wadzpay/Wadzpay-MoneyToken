import { createSlice } from "@reduxjs/toolkit"

import { APP_CONFIGURATION } from "./modules"

const initialState: APP_CONFIGURATION = {
  activeNavigation: "/",
  issuanceType: "bullion",
  themeColor: "#ffffff",
  buttonColor: "#FFC235"
}

const appConfigSlice = createSlice({
  name: "appConfig",
  initialState,
  reducers: {
    setAppConfig: (state, action) => {
      // Return a new state object instead of mutating the state directly
      return {
        ...state,
        activeNavigation: action.payload.activeNavigation,
        themeColor: action.payload
      }
    }
  }
})

export const { setAppConfig } = appConfigSlice.actions

export default appConfigSlice.reducer
