import { createSlice } from "@reduxjs/toolkit"

import { INSTITUTION_CURRENCIES } from "./modules"

const initialState: INSTITUTION_CURRENCIES = {
  defaultCurrency: "MYR",
  destinationCurrency: "SART"
}

const institutionCurrenciesSlice = createSlice({
  name: "institutionCurrencies",
  initialState,
  reducers: {
    setInstitutionCurrencies: (state, action) => {
      // Mutating the state or Mofiying the state here
      return {
        ...state,
        defaultCurrency: action.payload,
        destinationCurrency: action.payload
      }
    }
  }
})

export const { setInstitutionCurrencies } = institutionCurrenciesSlice.actions

export default institutionCurrenciesSlice.reducer
