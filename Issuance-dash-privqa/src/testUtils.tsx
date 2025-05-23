import React from "react"
import { render } from "@testing-library/react"
import { QueryClient, QueryClientProvider } from "react-query"

import { OnboardingContext } from "./context"
import { UserContext } from "./context/User"
import { User } from "./auth/AuthManager"
import { IssuanceDetailsData } from "./api/models"
import { IssuanceContext } from "./context/Merchant"

const queryClient = new QueryClient()

export const queryClientWrapper = ({ children }: { children: JSX.Element }) => (
  <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
)

export const renderWithQueryClient = (element: JSX.Element) =>
  render(queryClientWrapper({ children: element }))

export const OnboardingContextWrapper = ({
  children,
  phoneNumber,
  accountDetails
}: {
  children: JSX.Element
  phoneNumber?: string
  accountDetails?: {
    email?: string
    newPassword?: string
    confirmPassword?: string
  }
}) => (
  <OnboardingContext.Provider
    value={{
      onboardingValues: {
        accpetInvitations: {
          email: accountDetails?.email || "",
          code: accountDetails?.email || "",
          newPassword: accountDetails?.newPassword || "",
          confirmPassword: accountDetails?.confirmPassword || ""
        }
      },
      setOnboardingValues: jest.fn(),
      resetOnboardingValues: jest.fn()
    }}
  >
    {children}
  </OnboardingContext.Provider>
)

export const UserContextWrapper = ({
  children,
  user
}: {
  children: JSX.Element
  user?: User
}) => (
  <UserContext.Provider
    value={{
      user,
      isLoading: false,
      fiatAsset: "USD",
      setUser: () => undefined,
      error: "",
      verified: false,
      setFiatAsset: () => undefined
    }}
  >
    {children}
  </UserContext.Provider>
)

export const IssuanceContextWrapper = ({
  children,
  issuanceDetails
}: {
  children: JSX.Element
  issuanceDetails?: IssuanceDetailsData
}) => (
  <IssuanceContext.Provider
    value={{
      issuanceDetails,
      isFetching: false
    }}
  >
    {children}
  </IssuanceContext.Provider>
)

export const mockCurrentAuthenticatedUser = () =>
  Promise.resolve({
    attributes: {
      email: "test@test.com",
      phone_number: "+123456768",
      sub: "xxx"
    },
    username: "test_user"
  })
