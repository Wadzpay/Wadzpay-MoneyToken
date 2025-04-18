import React, { useState, useEffect } from "react"
import { useRefundFormTokenDetails } from "src/api/user"
import { useParams } from "react-router-dom"

import RefundForm from "./RefundForm"
import ErrorPage from "./ErrorPage"

export default function CustomerRefundForm() {
  const { mutate: getRefundForm, data, error } = useRefundFormTokenDetails()

  const { id } = useParams() as { id: string }
  const [refundFormData, setRefundFormData] = useState<any | undefined>(
    undefined
  )

  useEffect(() => {
    getRefundForm({
      refundUUID: id
    })
  }, [])

  useEffect(() => {
    data && setRefundFormData(data)
  }, [data, setRefundFormData])

  return (
    <>
      {refundFormData && <RefundForm data={refundFormData} id={id} />}{" "}
      {/* {error && (
        <div className="alert alert-danger" role="alert">
          {error.message}
        </div>
      )} */}
      {error && error?.message === "TOKEN_EXPIRED" && (
        <ErrorPage errorMssg={error?.message} />
      )}
    </>
  )
}
