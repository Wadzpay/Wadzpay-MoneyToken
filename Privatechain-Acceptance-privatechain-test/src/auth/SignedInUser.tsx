import React, { ReactNode, useContext } from "react"
import { UserContext } from "src/context/User"

const SignedInUser: React.FC = ({ children }: { children?: ReactNode }) => {
  const { user, isLoading,setUser,error,verified } = useContext(UserContext)
  console.log(user,sessionStorage.getItem("verified")
  )
   if(user&&sessionStorage.getItem("verified")==null){
    setUser(null);
    sessionStorage.removeItem("currentUser");
    sessionStorage.removeItem("currentToken");

  }
   return <>{!isLoading &&sessionStorage.getItem("verified")!=null&&  user && children}</>
}

export default SignedInUser
