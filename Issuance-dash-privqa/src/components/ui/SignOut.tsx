import React from "react"
import { signOutAsync } from "src/auth/AuthManager"
import SignedInUser from "src/auth/SignedInUser"

const SignOut: React.FC = () => {
  return (
    <SignedInUser>
      <span className="signout-fixed"></span>
      <span
        style={{
          cursor: "pointer",
          zIndex: 1,
          position: "absolute",
          top: "0",
          left: "3"
        }}
        onClick={() => signOutAsync()}
      >
        <span className="expand-title logout-icon">Sign Out</span>
        <img
          src={"/images/navigation/logout.svg"}
          alt="Logout Icon"
          title="Logout"
          style={{ cursor: "pointer" }}
        />
      </span>
    </SignedInUser>
  )
}

export default SignOut
