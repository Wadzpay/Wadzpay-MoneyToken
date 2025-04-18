import React from "react"
import { t } from "i18next"
import { Link } from "react-router-dom"
import { RouteType } from "src/constants/routeTypes"
import { useForm } from "react-hook-form"
import { yupResolver } from "@hookform/resolvers/yup"
import { InviteUserForm } from "src/constants/formTypes"
import { useValidationSchemas } from "src/constants/validationSchemas"
import { useInviteUser } from "src/api/admin"
import Row from "react-bootstrap/Row"
import Col from "react-bootstrap/Col"
import Card from "src/helpers/Card"

function InviteUser(): JSX.Element {
  const { inviteUserSchema } = useValidationSchemas()

  const {
    register,
    handleSubmit,
    formState: { isSubmitted, errors },
    getValues,
    reset
  } = useForm<InviteUserForm>({
    resolver: yupResolver(inviteUserSchema)
  })

  const { mutate: inviteUser, isLoading, error, isSuccess } = useInviteUser()

  const { email } = getValues()

  const onNext = () => {
    const { email } = getValues()
    inviteUser({
      email,
      role: "MERCHANT_READER"
    })
  }

  return (
    <Card>
      <Row>
        <Col md={{ span: 5, offset: 0 }}>
          <div>
            <h2>{t("Invite User")}</h2>
            {(!isSuccess || !isSubmitted) && (
              <form  autoComplete="off"  onSubmit={handleSubmit(onNext)} role="form" noValidate>
                {error && (
                  <div className="alert alert-danger" role="alert">
                    {error.message}
                  </div>
                )}
                <div className="form-group mt-4">
                  <label htmlFor="email">{t("Email Address")}</label>
                  <input aria-autocomplete='both' aria-haspopup="false"
                    {...register("email")}
                    placeholder={t("Email Address")}
                    data-testid="emailInput"
                    type="email"
                    /* autoComplete="true" */
                    className={`form-control ${
                      errors.email?.message ? "is-invalid" : ""
                    }`}
                    aria-describedby="emailError"
                  />
                  {errors.email?.message && (
                    <div id="emailError" className="invalid-feedback">
                      {errors.email?.message}
                    </div>
                  )}
                </div>

                <div className="form-group row row-cols-auto btn-items-center">
                  <div className="col mt-4">
                    <button
                      type="submit"
                      className="btn btn-primary wdz-btn-primary wdz-btn-md"
                      disabled={isLoading}
                    >
                      Invite
                    </button>
                  </div>
                  <div className="col mt-4">
                    <Link
                      to={RouteType.ADMIN_USERS}
                      data-testid="backButton"
                      className="btn btn-secondary wdz-btn-grey wdz-btn-md"
                      role="button"
                    >
                      {t("Cancel")}
                    </Link>
                  </div>
                </div>
              </form>
            )}
            {isSuccess && isSubmitted && (
              <>
                <div className={`alert alert-success`}>
                  Invitation submitted
                </div>
                {/* <div>Please send this link</div>
                <div>
                  <a
                    href={`${window.location.origin}/accept-invite?email=${email}`}
                  >
                    {`${window.location.origin}/accept-invite?email=${email}`}
                  </a>
                </div>
                <div>to</div>
                <div>
                  <a
                    href={`mailto:${email}?subject=WadzPay Invite&body=${window.location.origin}/accept-invite?email=${email}`}
                  >
                    {email}
                  </a>
                </div>
                <div>to notify them that they have been invited to WadzPay</div> */}
                <div className="mt-2">
                  <Link
                    to={RouteType.ADMIN_USERS}
                    data-testid="backButton"
                    className="btn btn-primary"
                    role="button"
                  >
                    {t("Done")}
                  </Link>
                  <button
                    data-testid="addAnotherButton"
                    className="btn btn-secondary ms-2"
                    role="button"
                    onClick={() => reset()}
                  >
                    {t("Invite another user")}
                  </button>
                </div>
              </>
            )}
          </div>
        </Col>
      </Row>
    </Card>
  )
}

export default InviteUser
