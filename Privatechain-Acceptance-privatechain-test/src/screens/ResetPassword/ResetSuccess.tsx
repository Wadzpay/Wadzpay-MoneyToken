import React from "react";
import { useTranslation } from "react-i18next";
import { RouteType } from "src/constants/routeTypes";
import { useNavigate } from "react-router-dom";

const ResetSuccess: React.FC = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  return (
    // <div className="wp-form">
    //   <h2>{t("Password reset successful")}</h2>
    //   <p>{t("You can now try signing in")}</p>
    //   <button
    //     className="btn btn-primary"
    //     onClick={() => navigate(RouteType.SIGN_IN)}
    //   >
    //     {t("Sign In")}
    //   </button>
    // </div>
    <div className="signIn">
      <img src={"/images/login_bg.svg"} className="loginBG" />
      <div className="userAuth">
        <div className="userAuthForm">
          <h5>{t("Password reset successful")}</h5>
          <p>{t("You can now try signing in")}</p>
          <button
            className="btn btn-primary signInBtn"
            onClick={() => navigate(RouteType.SIGN_IN)}
          >
            {t("Sign In")}
          </button>
        </div>
        <div className="userCopyright">
          <p>{t("Copyright ©️ 2023 WadzPay Worldwide.")}</p>
        </div>
      </div>
    </div>
  );
};

export default ResetSuccess;
