import * as yup from "yup"
import { useState } from "react"

const useIsEmailValid = (address?: string) => {
  const [emailIsValid, setEmailIsValid] = useState(false)

  const schema = yup.object().shape({
    email: yup.string().required().email()
  })

  schema
    .isValid({
      email: address
    })
    .then(function (valid) {
      if (valid) {
        setEmailIsValid(valid)
      }
    })

  return emailIsValid
}

export default useIsEmailValid
