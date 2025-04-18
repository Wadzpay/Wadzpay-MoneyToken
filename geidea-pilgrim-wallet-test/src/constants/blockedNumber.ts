export type Countries = "SINGAPORE"

export type PHONE = "+65"

export const BlockedCountries: { [key in Countries]: PHONE } = {
  SINGAPORE: "+65"
}
