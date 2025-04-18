import React, { PropsWithChildren, useEffect, useState } from "react";
import { Navigate, useLocation } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { MerchantDetailsData } from "src/api/models";
import { useMerchantDetails } from "src/api/user";
import { RouteType } from "src/constants/routeTypes";
import SignedInUser from "src/auth/SignedInUser";
import SignedOutUser from "src/auth/SignedOutUser";
import env from "src/env";
import { getIdTokenAsync } from "src/auth/AuthManager";
type MerchantContextType = {
  merchantDetails?: MerchantDetailsData;
  institutionDetails?: any;
  isFetching: boolean;
};

export const MerchantContext = React.createContext<MerchantContextType>({
  merchantDetails: undefined,
  isFetching: true,
});

type Props = PropsWithChildren<{}>;

export const SignedInMerchantContextProvider: React.FC<Props> = ({
  children,
}: Props) => {
  const { t } = useTranslation();
  const { pathname } = useLocation();
  const { data, isFetching, error, refetch } = useMerchantDetails();
  // const {
  //   data: institutionData,
  //   error: institutionError,
  //   refetch: institutionRefetch,
  // } = useInstitutionDetails("");

  const [merchantDetailsData, setMerchantData] =
    useState<MerchantDetailsData>();

  const [merchDetails, setMerchantDetails] = useState({
    merchantName: "",
    merchantLogo: "https://wadzpay-app-bucket-privatechain-dev.s3-me-south-1.amazonaws.com/logo/1743082320729_logo.png",
    merchantCurrency: "SAR",
  });

  const fetchMerchantDetails = async (name: any) => {
    console.log("gg", name);
    const response = await fetch(
      `${env.PUBLIC_API_URL}merchant/getDetails?merchantName=${name}&lang=English`,
      {
        method: "GET", // Assuming it's a GET request
        headers: {
          Authorization: `Bearer ${await getIdTokenAsync()}`, // Use the token from localStorage
          "Content-Type": "application/json", // Optional, based on your API needs
        },
      }
    );

    if (!response.ok) {
      throw new Error("Failed to fetch merchant details");
    }
    return response.json();
  };
  const getMerchantDetails = async (name: any) => {
    try {
      const data = await fetchMerchantDetails(name);
      console.log("Merchant Details: app", data);
      setMerchantDetails(data);
      // You can also update state here if you want to store the result in the component's state
    } catch (error) {
      console.error("Error fetching merchant details:", error);
    }
  };


  useEffect(() => {
    if (data) {
      setMerchantData(data);
      getMerchantDetails(data?.merchant?.name)
    }
  }, [data]);

  useEffect(() => {
    if (!data) {
      refetch();
    }
  }, [pathname]);

  const getAction = () => {
    // Display a blank screen while fetching the merchant details
    if (isFetching) {
      return <></>;
    }

    // Display the error message if not MERCHANT_NOT_FOUND
    if (error && error.message !== "ERROR_MESSAGE.MERCHANT_NOT_FOUND") {
      return (
        <div className="alert alert-danger" role="alert">
          <div>{error.message}</div>
          <div>{t("Unable to retrieve merchant details from the server")}</div>
        </div>
      );
    }

    // Send to home page if merchant details exist and requesting the merchant details page
    if (merchantDetailsData && pathname === RouteType.MERCHANT_DETAILS) {
      return <Navigate to={RouteType.HOME} />;
    }

    // Send to merchant details page if merchant is not found
    if (
      pathname !== RouteType.MERCHANT_DETAILS &&
      error &&
      error.message === "ERROR_MESSAGE.MERCHANT_NOT_FOUND"
    ) {
      return <Navigate to={RouteType.MERCHANT_DETAILS} />;
    }

    // Otherwise, display the content
    return (
      <MerchantContext.Provider
        value={{
          merchantDetails: merchantDetailsData,
          institutionDetails: merchDetails,
          isFetching,
        }}
      >
        {children}
      </MerchantContext.Provider>
    );
  };

  return getAction();
};

/**
 * This will load the merchant data if the current user is logged in
 * otherwise it just renders the children
 */
export const MerchantContextProvider: React.FC<Props> = ({
  children,
}: Props) => {
  return (
    <>
      <SignedInUser>
        <SignedInMerchantContextProvider>
          {children}
        </SignedInMerchantContextProvider>
      </SignedInUser>
      <SignedOutUser>{children}</SignedOutUser>
    </>
  );
};
