import React, { useEffect, ClipboardEvent, useState, useContext } from "react";
import { useTranslation } from "react-i18next";
import { useForm } from "react-hook-form";
import { AiFillEyeInvisible, AiFillEye } from "react-icons/ai";
import { yupResolver } from "@hookform/resolvers/yup";
import { Link, useNavigate } from "react-router-dom";
import { RouteType } from "src/constants/routeTypes";

import { useSignIn, verifyToken } from "../api/user";
import { SignInForm } from "../constants/formTypes";
import { useValidationSchemas } from "../constants/validationSchemas";
import env from "../env";
import { UserContext } from "src/context/User";

const SignIn: React.FC = () => {
  const [showPwdIcon, setPwdIcon] = useState(false);
  const [pwdInputType, setPwdInputType] = useState("password");
  const {
    user,
    isLoading: loadingUser,
    error: userError,
    verified,
  } = useContext(UserContext);

  const navigate = useNavigate();
  const { t } = useTranslation();
  const { signInSchema } = useValidationSchemas();
  const {
    register,
    handleSubmit,
    setValue,
    formState: { errors },
  } = useForm<SignInForm>({
    resolver: yupResolver(signInSchema),
    defaultValues: {
      email: env.DEFAULT_USER_EMAIL,
      password: env.DEFAULT_USER_PASSWORD,
    },
  });

  const { mutate: signIn, isLoading, error, isSuccess } = useSignIn();
  useEffect(() => {
    if (isSuccess && verified) {
      navigate(RouteType.HOME);
    }
  }, [isSuccess, verified, userError]);

  useEffect(() => {
    if (error) {
      setValue("password", "");
      // setValue("email", "")
    }
  }, [error]);

  const onLogin = (data: SignInForm) => {
    const { email, password } = data;
    signIn({ email, password });
  };

  const handleClipboardEvent = (e: ClipboardEvent<HTMLInputElement>) => {
    e.preventDefault();
    return false;
  };

  const changePWDIconType = () => {
    setPwdIcon(!showPwdIcon);
    if (pwdInputType == "text") {
      setPwdInputType("password");
    } else {
      setPwdInputType("text");
    }
  };

  return (
    <div className="signIn">
      <img src={"/images/login_bg.svg"} className="loginBG" />
      <div className="userAuth">
        <div className="userAuthForm">
          <form
            autoComplete="off"
            onSubmit={handleSubmit(onLogin)}
            role="form"
            noValidate
          >
            {(error || userError) && (
              <div className="alert alert-danger" role="alert">
                {error && error.message}
                {userError && userError}
              </div>
            )}
            <div className="userAuthWTKLogo">
              <img src={"/images/black_logo.svg"} />
            </div>
            <div className="userAuthLoginText">
              <h5>{t("Acceptance Portal Login")}</h5>
              {/* <p>{t("Make every moment special with WadzPay.")}</p> */}
            </div>
            <div className="form-group mt-4">
              <input
                {...register("email")}
                placeholder={t("Email Address")}
                type="email"
                data-testid="email"
                className={`userInput ${
                  errors.email?.message ? "is-invalid" : ""
                }`}
                aria-describedby="emailError"
                aria-autocomplete="both"
                aria-haspopup="false"
                /*                 autoComplete="new-password"
                 */
              />
              {errors.email?.message && (
                <div id="emailError" className="invalid-feedback">
                  {errors.email?.message}
                </div>
              )}
              {/*               <input type="text" name="dummy" style={{display:"none"}}/><br/>
               */}{" "}
            </div>
            <div className="form-group mt-3">
              <div className="loginPassword">
                <input
                  {...register("password")}
                  placeholder={t("Password")}
                  type={pwdInputType}
                  data-testid="password"
                  className={`userInput ${
                    errors.password?.message ? "is-invalid" : ""
                  }`}
                  aria-describedby="passwordError"
                  onPaste={handleClipboardEvent}
                  onKeyDown={(event) => {
                    if (
                      (event.ctrlKey && event.key === "z") ||
                      (event.metaKey && event.key === "z")
                    ) {
                      event.preventDefault();
                      return false;
                    }
                    if (event.ctrlKey && event.shiftKey && event.key === "z") {
                      event.preventDefault();
                      return false;
                    }
                  }}
                />
                <div onClick={() => changePWDIconType()}>
                  {showPwdIcon ? <AiFillEye /> : <AiFillEyeInvisible />}
                </div>
              </div>
              {errors.password?.message && (
                <div id="emailError" className="signinForm-error">
                  {errors.password?.message}
                </div>
              )}
            </div>
            <div className="form-group mt-3">
              <button
                type="submit"
                className="btn btn-primary signInBtn"
                disabled={isLoading && verified}
              >
                {t("Login")}
              </button>
              {/* <div className="col mt-4">
                <button
                  type="button"
                  className="btn btn-link"
                  disabled={isLoading}
                  onClick={() => navigate(RouteType.RESET_PASSWORD)}
                >
                  {t("Reset Password")}
                </button>
              </div>
              <div className="col mt-4">
                <button
                  type="button"
                  className="btn btn-link"
                  disabled={isLoading}
                  onClick={() => navigate(RouteType.CREATE_ACCOUNT)}
                >
                  {t("Create Account")}
                </button>
              </div> */}
              <div className="mt-4">
                <a
                  onClick={() => navigate(RouteType.FORGOT_PASSWORD)}
                  className="forgotPassword"
                  style={{ color: " #1E4B83", textDecoration: "underline" }}
                >
                  {t("Forgot Password?")}
                </a>
              </div>
            </div>
            {/* <div className="form-group userAuthTerms">
              <p>
                <a href="">{t("Terms & Condition")}</a>
                {" | "}
                <a href="">{t("Privacy Policy")}</a>
              </p>
            </div> */}
          </form>
        </div>
        <div className="userCopyright">
          <p>{t("Copyright ©️ 2023 WadzPay Worldwide.")}</p>
        </div>
      </div>
    </div>
  );
};

export default SignIn;
