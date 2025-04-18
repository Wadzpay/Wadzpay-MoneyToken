import React, { useContext } from "react"
import { UserContext } from "src/context/User"
import env, { ENV } from "src/env.template"

const About: React.FC = () => {
  const { user } = useContext(UserContext)

  return (
    <>
      <div className="table-responsive" style={{ overflowX: "visible" }}>
        {user && (
          <>
            <div className="">
              <p>
                <b>About</b>
              </p>
            </div>
            <div className="ml-4 mt-3" data-testid="merchantPhoneNumber">
              Version : {env.VERSION}
            </div>
            <div className="ml-4 mt-4" data-testid="merchantName">
              Released Date : {"01-02-2023"}
            </div>
          </>
        )}
      </div>
    </>
  )
}

export default About
