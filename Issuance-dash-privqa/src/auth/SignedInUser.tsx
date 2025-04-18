import React, { ReactNode, useContext, useEffect } from "react"
import { UserContext } from "src/context/User"

const SignedInUser: React.FC<{ children?: ReactNode }> = ({ children }) => {
  const { user, isLoading, setUser, error, verified } = useContext(UserContext)

  useEffect(() => {
    if (user && sessionStorage.getItem("verified") == null) {
      setUser(null)
      sessionStorage.removeItem("currentUser")
      sessionStorage.removeItem("currentToken")
    }
  }, [user, setUser])

  return (
    <>
      {!isLoading &&
        sessionStorage.getItem("verified") != null &&
        user &&
        children}
    </>
  )
}

export default SignedInUser
