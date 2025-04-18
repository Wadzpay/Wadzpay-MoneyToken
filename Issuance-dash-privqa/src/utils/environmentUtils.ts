const isProduction = (): boolean => {
  const baseUrl = window.location.origin.toLowerCase()

  return baseUrl === "https://issuance.privatechain.wadzpay.com"
}

export default isProduction
