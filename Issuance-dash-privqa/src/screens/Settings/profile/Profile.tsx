import React, { useContext } from "react"
import { Link } from "react-router-dom"
import { UserContext } from "src/context/User"
import { RouteType } from "src/constants/routeTypes"
import { t } from "i18next"

const Profile: React.FC = () => {
  const { user } = useContext(UserContext)

  return (
    <>
      <div className="table-responsive" style={{ overflowX: "visible" }}>
        {user && (
          <>
            <div>
              <p>
                <b>Profile</b>
              </p>
            </div>
            <div className="row">
              <div className="col-2" style={{ width: "12%" }}>
                <label className="form-label">Email</label>
              </div>
              <div className="col">
                :
                <span style={{ marginLeft: "20px" }}>
                  {user.attributes.email}
                </span>
              </div>
            </div>
            <div className="row mb-4">
              <div className="col-2" style={{ width: "12%" }}>
                <label className="form-label">Phone Number</label>
              </div>
              <div className="col">
                :
                <span className="ml-20" style={{ marginLeft: "20px" }}>
                  {user.attributes.phone_number}
                </span>
              </div>
            </div>
            <div className="ml-4 mt-4 mb-3" data-testid="merchantName">
              <Link
                to={RouteType.CHANGE_PASSWORD}
                role="button"
                style={{ background: "#1E4B83" }}
                className="btn btn-secondary signInBtn"
              >
                {t("Change Password")}
              </Link>
            </div>
          </>
        )}
      </div>
    </>
  )
}

export default Profile
