/* eslint-disable @typescript-eslint/no-empty-function */
import React, {PropsWithChildren, useState} from 'react';
import {subYears} from 'date-fns';
import {
  AccountDetailsForm,
  CreateAccountForm,
  PersonalDetailsForm,
} from '../constants';
import env from '../env';
import {isDev} from '../utils';

type OnboardingContextValues = {
  createAccount: CreateAccountForm;
  accountDetails: AccountDetailsForm;
  personalDetails: PersonalDetailsForm;
};

type OnboardingContextType = {
  onboardingValues: OnboardingContextValues;
  setOnboardingValues: (values: OnboardingContextValues) => void;
};

const initialOnboardingContextValues = {
  createAccount: {
    country: env.DEFAULT_COUNTRY || '',
    phoneNumber: env.DEFAULT_USER_PHONE_NUMBER || '',
    isNewsletter: false,
    isAgreement: isDev,
  },
  accountDetails: {
    email: env.DEFAULT_USER_EMAIL || '',
    newPassword: env.DEFAULT_USER_PASSWORD || '',
    confirmPassword: env.DEFAULT_USER_PASSWORD || '',
  },
  personalDetails: {
    firstName: '',
    lastName: '',
    dateOfBirth: subYears(new Date(), 18),
    profession: '',
    sourceOfFund: '',
  },
};

export const OnboardingContext = React.createContext<OnboardingContextType>({
  onboardingValues: initialOnboardingContextValues,
  setOnboardingValues: () => {},
});

type Props = PropsWithChildren<{}>;

export const OnboardingContextProvider: React.FC<Props> = ({
  children,
}: Props) => {
  const [state, setState] = useState<OnboardingContextValues>(
    initialOnboardingContextValues,
  );

  return (
    <OnboardingContext.Provider
      value={{
        onboardingValues: state,
        setOnboardingValues: setState,
      }}>
      {children}
    </OnboardingContext.Provider>
  );
};
