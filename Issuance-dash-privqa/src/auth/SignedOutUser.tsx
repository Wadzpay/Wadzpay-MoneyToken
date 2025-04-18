import React, { ReactNode, useContext, useEffect } from "react"
import { UserContext } from "src/context/User"

import SignIn from "../screens/SignIn"

const SignedOutUser: React.FC = ({ children }: { children?: ReactNode }) => {
  const { user, isLoading } = useContext(UserContext)

  useEffect(() => {
    if (!isLoading && !user && children) {
      const sidebar = document.getElementById("navigation-container")
      const header = document.getElementById("topHeader")
      const mainContainer = document.getElementById("main-container")
      if (sidebar && header && mainContainer) {
        sidebar.style.display = "none"
        header.style.display = "none"
        mainContainer.style.height = "0"
      } else {
        sidebar!.style.display = "block"
        header!.style.display = "block"
        mainContainer!.style.height = "160vh"
      }
    }
  }, [isLoading, user, children])
  // return <>{!isLoading && !user && <SignIn />}</>
  return <>{!isLoading && !user && children}</>
}

export default SignedOutUser
