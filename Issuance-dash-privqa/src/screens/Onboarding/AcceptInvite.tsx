import React, { useContext, useEffect } from "react"
import { useNavigate, useSearchParams } from "react-router-dom"
import { RouteType } from "src/constants/routeTypes"
import { OnboardingContext } from "src/context"
import { defaultOnboardingValues } from "src/context/Onboarding"

const AccountDetails: React.FC = () => {
  const { setOnboardingValues } = useContext(OnboardingContext)
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()

  const email = searchParams.get("email")

  const onboardingValues = {
    ...defaultOnboardingValues,
    ...{
      ...{
        accpetInvitations: {
          ...defaultOnboardingValues.accpetInvitations,
          email: email || defaultOnboardingValues.accpetInvitations.email
        },
        isMerchantAdmin: false
      }
    }
  }

  useEffect(() => {
    if (email) {
      setOnboardingValues(onboardingValues)

      navigate(RouteType.ACCEPT_INVITATIONS)
    }
  }, [email])

  return <></>
}

export default AccountDetails
