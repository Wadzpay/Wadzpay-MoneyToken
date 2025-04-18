import React, { useEffect, useState, useContext } from "react"
import { t } from "i18next"
import { Button } from "react-bootstrap"
import { useDisableUser, useEnableUser, useUserList } from "src/api/admin"
import { MerchantUserData } from "src/api/models"
import { getCurrentUserAsync, User } from "src/auth/AuthManager"
import { MerchantContext } from "src/context/Merchant"
import MerchantDetails from "src/screens/Onboarding/MerchantDetails"

function AdminUserList(): JSX.Element {
  const { merchantDetails } = useContext(MerchantContext)
  const [users, setUsers] = useState<MerchantUserData[]>()
  const [merchanType, setMerchantType] = useState<string>("")
  const [updatingUser, setUpdatingUser] =
    useState<{ isDisable: boolean; email: string }>()
  const { data: dataUsers, error: errorUsers } = useUserList()

  const [currentUser, setCurrentUser] = useState<User>()
  useEffect(() => {
    getCurrentUserAsync().then(setCurrentUser)
  }, [])

  const {
    mutate: enableUser,
    error: enableUserError,
    isSuccess: enableUserIsSuccess
  } = useEnableUser()

  const {
    mutate: disableUser,
    error: disableUserError,
    isSuccess: disableUserIsSuccess
  } = useDisableUser()

  useEffect(() => {
    setUsers(dataUsers)
  }, [dataUsers])

  useEffect(() => {
    if (merchantDetails && merchantDetails.role) {
      setMerchantType(merchantDetails?.role)
    }
  }, [merchantDetails])

  useEffect(() => {
    if (
      users &&
      updatingUser &&
      !updatingUser.isDisable &&
      enableUserIsSuccess
    ) {
      setUsers(
        users.map((user) =>
          user.userAccount.email === updatingUser.email
            ? { ...user, isActive: true }
            : user
        )
      )
      setUpdatingUser(undefined)
    }

    if (
      users &&
      updatingUser &&
      updatingUser.isDisable &&
      disableUserIsSuccess
    ) {
      setUsers(
        users.map((user) =>
          user.userAccount.email === updatingUser.email
            ? { ...user, isActive: false }
            : user
        )
      )
      setUpdatingUser(undefined)
    }
  }, [users, updatingUser, enableUserIsSuccess, disableUserIsSuccess])

  const setUserEnabled = (user: MerchantUserData) => {
    setUpdatingUser({ isDisable: false, email: user.userAccount.email })
    enableUser({ email: user.userAccount.email })
  }

  const setUserDisabled = (user: MerchantUserData) => {
    setUpdatingUser({ isDisable: true, email: user.userAccount.email })
    disableUser({ email: user.userAccount.email })
  }

  const error = errorUsers || enableUserError || disableUserError

  return (
    <div className="table-responsive">
      {error && (
        <div
          className="alert alert-danger"
          role="alert"
          data-testid="errorMessage"
        >
          {error.message}
        </div>
      )}
      <table className="table table-hover">
        <thead>
          <tr>
            <th scope="col">{t("User")}</th>
            <th scope="col">{t("Status")}</th>
          </tr>
        </thead>
        <tbody>
          {users &&
            users.map((user) => (
              <tr key={user.userAccount.email} className="align-middle">
                <td>{user.userAccount.email}</td>
                <td>
                  {user.isActive === false && t("Disabled")}
                  {user.isActive === true && t("Enabled")}
                </td>
                <td>
                  {merchanType == "MERCHANT_ADMIN" &&
                  currentUser?.attributes.email ==
                    user.userAccount.merchant.primaryContactEmail ? (
                    <>
                      {!user.isActive && (
                        <Button
                          variant="outline-success"
                          onClick={() => setUserEnabled(user)}
                          disabled={updatingUser !== undefined}
                        >
                          {t("Enable")}
                        </Button>
                      )}
                      {user.isActive &&
                        user.userAccount.email !==
                          currentUser?.attributes.email && (
                          <Button
                            variant="outline-danger"
                            onClick={() => setUserDisabled(user)}
                            disabled={updatingUser !== undefined}
                          >
                            {t("Disable")}
                          </Button>
                        )}
                    </>
                  ) : null}
                </td>
              </tr>
            ))}
        </tbody>
      </table>
    </div>
  )
}

export default AdminUserList
