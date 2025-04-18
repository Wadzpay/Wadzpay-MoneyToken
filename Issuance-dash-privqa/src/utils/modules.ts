export interface ROOTSTATE extends APP_CONFIGURATION {
  appConfig: APP_CONFIGURATION
  institutionCurrencies: INSTITUTION_CURRENCIES
  settingsConfig: SETTINGS
}

export interface APP_CONFIGURATION {
  activeNavigation: string
  issuanceType: string
  themeColor: string
  buttonColor: string
}

export interface INSTITUTION_CURRENCIES {
  defaultCurrency: string
  destinationCurrency: string
}

export interface SETTINGS {
  p2pTransfer: boolean
}
