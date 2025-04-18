import React, { useContext, useEffect } from "react"
import { IssuanceContext } from "src/context/Merchant"

const MerchantName: React.FC = () => {
  const { issuanceDetails } = useContext(IssuanceContext)

  return (
    <>
      <img
        src="/images/Dubai_Duty_logo.png"
        style={{ maxHeight: "55px", marginRight: "6px" }}
        className="pe-1"
      />
      {/* {<h2>{merchantDetails?.merchant?.primaryContactFullName}</h2>} */}
    </>
  )
}

export default MerchantName
