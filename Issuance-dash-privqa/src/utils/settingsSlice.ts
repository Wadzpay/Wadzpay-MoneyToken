import { createSlice } from "@reduxjs/toolkit"

import { SETTINGS } from "./modules"

const initialState: SETTINGS = {
  p2pTransfer: true
}

const settingsSlice = createSlice({
  name: "appConfig",
  initialState,
  reducers: {
    setSettingsConfig: (state, action) => {
      // Return a new state object instead of mutating the state directly
      return {
        ...state,
        p2pTransfer: action.payload.p2pTransfer,
        themeColor: action.payload
      }
    }
  }
})

export const { setSettingsConfig } = settingsSlice.actions

export default settingsSlice.reducer
