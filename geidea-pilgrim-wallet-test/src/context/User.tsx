/* eslint-disable no-console */
/* eslint-disable @typescript-eslint/no-empty-function */
import React, {PropsWithChildren, useContext, useEffect, useState} from 'react';
import {Hub, HubCapsule} from '@aws-amplify/core';
import {FiatAsset} from '../constants/types';
import {User, getCurrentUserAsync, getIdTokenAsync} from '../auth/AuthManager';
import env from '~/env';

type UserContextType = {
  user: User;
  isLoading: boolean;
  fiatAsset: FiatAsset;
  setFiatAsset: (value: FiatAsset) => void;
  instDetails: {
    institutionName: string;
    institutionFullName: string;
    institutionLogo: string;
    issuingCurrency: string;
    acquiringCurrency: string;
  };
  setInstDetails: (value: any) => void;
  isLoadingSignupPage: boolean;
  reLoadSignUpPage: () => void;
  isUserKycApproved: boolean;
  setUserKycApproved: (value: boolean) => void;
  saveStaticQrCodeToGallery: boolean;
  setSaveStaticQrCodeToGallery: (value: boolean) => void;
  isActivationFeeCharged: boolean;
  setActivationFeeCharged: (value: boolean) => void;
  bankAccountNumber: string;
  countryCode: string;
  setBankAccNumber: (value: string) => void;
  setConCode: (value: string) => void;
  isIbanSkipped: boolean;
  setSkipIban: (value: boolean) => void;
  userFiatBalance: string;
  setUserFiatBalance: (value: string) => void;
  isBuySelltransactionExist: boolean;
  setBuySelltransactionExist: (value: boolean) => void;
  sarTokens: number | any;
  updateSarTokens: (value: number) => void;
};

export const UserContext = React.createContext<UserContextType>({
  user: null,
  isLoading: true,
  fiatAsset: 'SGD',
  setFiatAsset: () => {},
  isLoadingSignupPage: false,
  reLoadSignUpPage: () => {},
  isUserKycApproved: false,
  setUserKycApproved: () => {},
  isActivationFeeCharged: false,
  setActivationFeeCharged: () => {},
  bankAccountNumber: '',
  countryCode: '',
  setBankAccNumber: () => {}, // set IBAN
  setConCode: () => {}, // set Country code
  isIbanSkipped: false,
  setSkipIban: () => {}, // skip butn clicked or not
  userFiatBalance: '',
  setUserFiatBalance: () => {},
  isBuySelltransactionExist: false,
  setBuySelltransactionExist: () => {},
  sarTokens: 0,
  updateSarTokens: () => {},
  saveStaticQrCodeToGallery: false,
  setSaveStaticQrCodeToGallery: () => {},
  instDetails: {
    institutionName: '',
    institutionFullName: '',
    institutionLogo: '',
    issuingCurrency: '',
    acquiringCurrency: '',
  },
  setInstDetails: () => {}, // Placeholder function for setting instDetails
});

type Props = PropsWithChildren<{}>;

export type IssuanceDetailsData = {
  id: number;
  bankLogo: string;
  bankName: string;
  countryCode: string;
  timeZone: string;
  defaultCurrency: string;
  fiatCurrency: string;
  destinationFiatCurrency: string;
  phoneNumber: string;
  email: string;
  cognitoUsername: string;
  updatedAt: Date;
  createdAt: Date;
  isActive: boolean;
  p2pTransfer: boolean | null;
};

export const UserContextProvider: React.FC<Props> = ({children}: Props) => {
  const [user, setUser] = useState<User>();
  const [isLoading, setIsLoading] = useState(true);
  const [fiatAsset, setFiatAsset] = useState<FiatAsset>('SGD');
  const [isLoadingSignupPage, setIsLoadingSignupPage] = useState(false);
  const [isUserKycApproved, setIsUserKycApproved] = useState(false);
  const [saveStaticQrCodeToGallery, setIsSaveStaticQrCodeToGallery] =
    useState(false);
  const [isActivationFeeCharged, setIsActivationFeeCharged] = useState(false);
  const [bankAccountNumber, setBankAccountNumber] = useState('');
  const [countryCode, setCountryCode] = useState('');
  const [isIbanSkipped, setIsIbanSkipped] = useState(false);
  const [isBuySelltransactionExist, setisBuySelltransaction] = useState(false);
  const [userFiatBalance, setOldFiatBalance] = useState('');
  const [sarTokens, setSarTokens] = useState(0);
  const [userEmail, setUserEmail] = useState<string>('');
  

  const updateSarTokens = (value: number) => {
    setSarTokens(value);
  };

  const setBuySelltransactionExist = (value: boolean) => {
    setisBuySelltransaction(value);
  };

  const setUserFiatBalance = (value: string) => {
    setOldFiatBalance(value);
  };

  const setSkipIban = (value: boolean) => {
    setIsIbanSkipped(value);
  };
  const setUserKycApproved = (value: boolean) => {
    setIsUserKycApproved(value);
  };
  const setSaveStaticQrCodeToGallery = (value: boolean) => {
    setIsSaveStaticQrCodeToGallery(value);
  };

  const setActivationFeeCharged = (value: boolean) => {
    setIsActivationFeeCharged(value);
  };

  const setBankAccNumber = (value: string) => {
    setBankAccountNumber(value);
  };
  const setConCode = (value: string) => {
    setCountryCode(value);
  };
  const reLoadSignUpPage = () => {
    setUser(null);
    setIsLoading(false);
    setUserKycApproved(false);
    setActivationFeeCharged(false);
  };
  async function setCurrency() {
    //TODO: Add Curr On New Country Added
    await getCurrentUserAsync().then(value => {
      const number = value?.attributes?.phone_number;
      setFiatAsset('AED');
      // if (number?.startsWith("+91")) {
      //   setFiatAsset("INR")
      // } else if (number?.startsWith("+62")) {
      //   setFiatAsset("IDR")
      // } else if (number?.startsWith("+63")) {
      //   setFiatAsset("PHP")
      // } else if (number?.startsWith("+65")) {
      //   setFiatAsset("SGD")
      // } else if (number?.startsWith("+421")) {
      //   setFiatAsset("EUR")
      // } else if (number?.startsWith("+92")) {
      //   setFiatAsset("PKR")
      // } else if (number?.startsWith("+971")) {
      //   setFiatAsset("AED")
      // } else if (number?.startsWith("+44")) {
      //   setFiatAsset("GBP")
      // } else {
      //   setFiatAsset("SGD")
      // }
    });
  }

  const listener = async (data: HubCapsule) => {
    switch (data.payload.event) {
      case 'signIn':
        setUser(await getCurrentUserAsync());
        setCurrency();
        break;
      case 'signUp':
        setUser(await getCurrentUserAsync());
        break;
      case 'signOut':
        setUser(null);
        break;
      case 'signIn_failure':
        break;
      case 'tokenRefresh':
        break;
      case 'tokenRefresh_failure':
        setUser(null);
        break;
      case 'configured':
    }
  };

  // useEffect(() => {
  //   // if (user !== undefined) return; // Don't run if user is already set
  //   const bootstrapUser = async () => {
  //     let userDetails = await getCurrentUserAsync()
  //     setUser(userDetails);
  //     console.log("bootstrap userDetails", userDetails?.attributes?.email);
  //     let email = userDetails?.attributes?.email;
  //     getIssuanceDetails(email);
  //     // setUserEmail(userDetails ?userDetails?.attributes?.email: "");
  //     setCurrency();
  //     setIsLoading(false);
  //   };

  //   Hub.listen('auth', listener);
  //   bootstrapUser();
  //   return () => {
  //     Hub.remove('auth', listener);
  //   };
  // }, []);


  // useEffect(() => {
  //   const fetchUser = async () => {
  //     if (user) return; // Stop if user already has value
  
  //     const userDetails = await getCurrentUserAsync();
  //     if (userDetails) {
  //       setUser(userDetails);
  
  //       const email = userDetails?.attributes?.email;
  //       console.log("bootstrap userDetails", email);
  
  //       getIssuanceDetails(email);
  //       setCurrency();
  //       setIsLoading(false);
  //     }
  //   };
  
  //   const intervalId = setInterval(() => {
  //     if (!user) {
  //       fetchUser();
  //     } else {
  //       clearInterval(intervalId); // Stop trying once user is set
  //     }
  //   }, 1000); // Try every 1 second
  
  //   Hub.listen('auth', listener);
  
  //   return () => {
  //     clearInterval(intervalId);
  //     Hub.remove('auth', listener);
  //   };
  // }, []); // Only once on mount

  useEffect(() => {
    let isMounted = true;
  
    const bootstrapUser = async () => {
      if (user) return; // If user is already fetched, stop
  
      const userDetails = await getCurrentUserAsync();
  
      if (userDetails && isMounted) {
        setUser(userDetails);
  
        const email = userDetails?.attributes?.email;
        console.log("bootstrap userDetails", email);
  
        getIssuanceDetails(email);
        setCurrency();
        setIsLoading(false);
      } else {
        // Retry after delay
        setTimeout(() => {
          if (isMounted && !user) {
            bootstrapUser(); // retry
          }
        }, 1000);
      }
    };
  
    bootstrapUser(); // Start the loop
    Hub.listen('auth', listener);
  
    return () => {
      isMounted = false; // Cleanup
      Hub.remove('auth', listener);
    };
  }, []);
  
  const [IssuanceDetailsData, setIssuanceData] =
    useState<IssuanceDetailsData>();
    const [instDetails, setInstDetails] = useState(null);

  // get settings configuration
  // const settingsConfig = useSelector((store: ROOTSTATE) => store.settingsConfig)
  // console.log("dashboard", data)

  const fetchInstDetails = async (name: any) => {
    console.log('fetchInstDetails', name);
    const response = await fetch(
      `${env.PUBLIC_API_URL}issuance/getDetails?institutionName=${name}&lang=English`,
      {
        method: 'GET', // Assuming it's a GET request
        headers: {
          Authorization: `Bearer ${await getIdTokenAsync()}`, // Use the token from localStorage
          'Content-Type': 'application/json', // Optional, based on your API needs
        },
      },
    );

    if (!response.ok) {
      throw new Error('Failed to fetch merchant details');
    }
    return response.json();
  };
  const getInstDetails = async (name: any) => {

    try {
      const data = await fetchInstDetails(name);
      console.log('getInstDetails:', data);
      setInstDetails(data);
      // You can also update state here if you want to store the result in the component's state
    } catch (error) {
      console.error('Error fetching issuance details:', error);
    }
  };

  const fetchIssuanceDetails = async (email: string | undefined) => {
    console.log('fetchIssuanceDetails');

    const response = await fetch(
      `${env.PUBLIC_API_URL}issuanceWallet/walletName?email=${email}`,
      {
        method: 'GET',
        headers: {
          Authorization: `Bearer ${await getIdTokenAsync()}`,
          'Content-Type': 'application/json',
        },
      },
    );

    if (!response.ok) {
      throw new Error('Failed to fetch issuance details');
    }
    return response.json();
  };

  const getIssuanceDetails = async (email: string | undefined) => {
    console.log(
      'URL testDetails---',
      `${env.PUBLIC_API_URL}issuanceWallet/walletName?email=${email}`,
    );
    try {
      const data = await fetchIssuanceDetails(email);
      console.log('Bank Details:', data);
      
      // setInstDetails(data);
      getInstDetails(data?.bankName);
    } catch (error) {
      console.error('Error fetching issuance1 details:', error);
    }
  };

  // console.log("userEmail", userEmail)

  useEffect(() => {
    // console.log("user details check", user)
    // getIssuanceDetails();
    // getInstDetails("icici");

  }, []);
  return (
    <UserContext.Provider
      value={{
        user,
        isLoading,
        fiatAsset,
        setFiatAsset,
        reLoadSignUpPage,
        isLoadingSignupPage,
        isUserKycApproved,
        isActivationFeeCharged,
        bankAccountNumber,
        countryCode,
        setUserKycApproved,
        setActivationFeeCharged,
        setBankAccNumber,
        setConCode,
        isIbanSkipped,
        setSkipIban,
        userFiatBalance,
        setUserFiatBalance,
        isBuySelltransactionExist,
        setBuySelltransactionExist,
        updateSarTokens,
        sarTokens,
        setSaveStaticQrCodeToGallery,
        saveStaticQrCodeToGallery,
        instDetails, // <-- Add instDetails here
        setInstDetails, // <-- Add setInstDetails here
      }}>
      {children}
    </UserContext.Provider>
  );
};
