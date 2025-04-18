/* eslint-disable @typescript-eslint/no-empty-function */
import React, { PropsWithChildren, useState } from "react"
import { AccpetInvitationsForm } from "src/constants/formTypes"
import env from "src/env"

type OnboardingContextValues = {
  accpetInvitations: AccpetInvitationsForm
}

type OnboardingContextType = {
  onboardingValues: OnboardingContextValues
  setOnboardingValues: (values: OnboardingContextValues) => void
  resetOnboardingValues: () => void
}

export const defaultOnboardingValues: OnboardingContextValues = {
  accpetInvitations: {
    email: env.DEFAULT_USER_EMAIL || "",
    code: env.DEFAULT_USER_EMAIL || "",
    newPassword: env.DEFAULT_USER_PASSWORD || "",
    confirmPassword: env.DEFAULT_USER_PASSWORD || ""
  }
}

// Get the defaults from local storage if they exist
const localStorageOnboardingContextString = localStorage.getItem(
  "onboardingContextValues"
)
const localStorageOnboardingContextValues: OnboardingContextValues =
  localStorageOnboardingContextString
    ? JSON.parse(localStorageOnboardingContextString)
    : null

// Else get the defaults from the environment
const initialOnboardingContextValues: OnboardingContextValues =
  defaultOnboardingValues

export const OnboardingContext = React.createContext<OnboardingContextType>({
  onboardingValues:
    localStorageOnboardingContextValues || initialOnboardingContextValues,
  setOnboardingValues: () => {},
  resetOnboardingValues: () => {}
})

type Props = PropsWithChildren<{}>

export const OnboardingContextProvider: React.FC<Props> = ({
  children
}: Props) => {
  const [state, setState] = useState<OnboardingContextValues>(
    localStorageOnboardingContextValues || initialOnboardingContextValues
  )

  const setOnboardingValues = (values: OnboardingContextValues) => {
    // Save to local storage incase the user reloads the page
    localStorage.setItem("onboardingContextValues", JSON.stringify(values))
    setState(values)
  }

  const resetOnboardingValues = () => {
    localStorage.removeItem("onboardingContextValues")
    setState(initialOnboardingContextValues)
  }

  return (
    <OnboardingContext.Provider
      value={{
        onboardingValues: state,
        setOnboardingValues,
        resetOnboardingValues
      }}
    >
      {children}
    </OnboardingContext.Provider>
  )
}
