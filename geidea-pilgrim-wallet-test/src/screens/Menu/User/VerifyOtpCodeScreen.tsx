import React, { useEffect, useState } from "react"
import { StyleSheet, TouchableOpacity } from "react-native"
import { useTranslation } from "react-i18next"
import { useForm } from "react-hook-form"
import { yupResolver } from "@hookform/resolvers/yup"
import { addMinutes } from "date-fns"

import {
  CodeField,
  Container,
  ErrorModal,
  LoadingSpinner,
  NumericKeyboard,
  ScreenLayoutOnboarding,
  theme,
  Typography
} from "~/components/ui"
import { fieldProps } from "~/constants/fieldProps"
import {
  TIMER_MINUTES,
  useValidationSchemas,
  VerifyCodeForm
} from "~/constants"
import { calculateTimeLeft } from "~/utils"
import { VerifyPhoneOTPCodeForm } from "~/constants/formTypes"

const CODE_LENGTH = 6

const styles = StyleSheet.create({
  contentContainer: { flex: 1 , backgroundColor:theme.colors.white },
  resend: { height: theme.iconSize.lg } // Should be the same as loading spinner
})

export type UseCustomMutation<T> = {
  /* eslint-disable-next-line @typescript-eslint/no-explicit-any */
  mutate: (body: T) => void | Promise<any>
  isSuccess: boolean
  isLoading: boolean
  error: Error | null
}

type Props<T> = {
  // useRequestCode: () => UseMutationResult<ResponseType, Error, T>
  useRequestCode: UseCustomMutation<T>
  useVerifyCode: UseCustomMutation<T & { phoneNumberCode: string}>
  onVerifySuccess: () => void
  onBack: () => void
  extraParams: T
  title: string
  target: string
}

// Can't specify React.FC<Props> return type because of generics
// eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
const VerifyOtpCodeScreen = <T extends Record<string, unknown>>({
  useRequestCode,
  useVerifyCode,
  onVerifySuccess,
  extraParams,
  title,
  target,
  onBack
}: Props<T>) => {
  const { t } = useTranslation()

  const { verifyCodeSchema } = useValidationSchemas()
  const {
    control,
    handleSubmit,
    formState: { errors },
    getValues,
    setValue,
    watch
  } = useForm<VerifyPhoneOTPCodeForm>({
    resolver: yupResolver(verifyCodeSchema)
  })

  const {
    mutate: verifyCode,
    isLoading: isVerifyCodeLoading,
    error: verifyCodeError,
    isSuccess: isVerifyCodeSuccess
  } = useVerifyCode
  const {
    mutate: requestCode,
    isLoading: isRequestCodeLoading,
    error: requestCodeError,
    isSuccess: isRequestCodeSuccess
  } = useRequestCode

  const [minutes, setMinutes] = useState(1);
  const [seconds, setSeconds] = useState(30);
  const phoneNumberCode = watch("phoneNumberCode")

  useEffect(() => {
    const interval = setInterval(() => {
      if (seconds > 0) {
        setSeconds(seconds - 1);
      }
  
      if (seconds === 0) {
        if (minutes === 0) {
          clearInterval(interval);
        } else {
          setSeconds(59);
          setMinutes(minutes - 1);
        }
      }
    }, 1000);
  
    return () => {
      clearInterval(interval);
    };
  }, [seconds]);

  useEffect(() => {
    if (phoneNumberCode?.length === CODE_LENGTH) {
      handleSubmit(onNext)()
    }
  }, [phoneNumberCode])

  useEffect(() => {
    if (isVerifyCodeSuccess) {
      onVerifySuccess()
    }
  }, [isVerifyCodeSuccess])

  useEffect(() => {
    if (isRequestCodeSuccess) {
      // Reset timer
      // setTargetDate(addMinutes(new Date(), TIMER_MINUTES))
      // setTimeLeft(TIMER_MINUTES * 60)
      setMinutes(1);
      setSeconds(30);
    }
  }, [isRequestCodeSuccess])
  useEffect(() => {
    if (verifyCodeError) {
      setValue('phoneNumberCode', "")
    }
  }, [verifyCodeError])
  
  const onNext = () => {
    verifyCode({
        phoneNumberCode,
        ...extraParams,
    })
  }

  return (
    <ScreenLayoutOnboarding
      title={title}
      content={
        <Container justify="space-between" style={styles.contentContainer}>
          <Container>
            {target && 
            <>
              <Typography style={{marginTop: 50}}  fontFamily="Rubik-Regular" textAlign="center">
                {t('A code has been sent to your phone ')}
              </Typography>
              <Typography

                  variant="heading"
                  fontFamily="Helvetica-Bold">
                  {target}
                </Typography>
            </>
            }
            <Container style={{marginTop: 20}} spacing={2}>
              <CodeField
                name="phoneNumberCode"
                control={control}
                codeLength={CODE_LENGTH}
                error={errors.phoneNumberCode}
                placeholder={t('Verification Code')}
                {...fieldProps.verificationCode}
              />
              <Container
                alignItems="center"
                noItemsStretch
                style={styles.resend}>
                {/* {timeLeft > 0 ? (
                  <Typography color="orange" textAlign="left">
                    {t("No code? Wait", {
                      timeLeft
                    })}
                  </Typography>
                ) : isRequestCodeLoading ? (
                  <LoadingSpinner color="orange" />
                ) : (
                  <TouchableOpacity  onPress={() => {
                    setValue("code","")
                    requestCode(extraParams)
                  }}>
                   <Typography variant="button" color="orange">
                      {t("Resend code")}
                    </Typography>
                  </TouchableOpacity>
                )} */}

                {seconds > 0 || minutes > 0 ? (
                  <Typography fontFamily="Rubik-Regular" textAlign="left">
                    {t('No code? Please Wait')}{' '}
                    {minutes < 10 ? `0${minutes}` : minutes}:
                    {seconds < 10 ? `0${seconds}` : seconds}{' seconds'}
                  </Typography>
                ) : isRequestCodeLoading ? (
                  <LoadingSpinner color="orange" />
                ) : (
                  <TouchableOpacity onPress={() => requestCode(extraParams)}>
                    <Typography variant="button" color="orange">
                      {t('Resend code')}
                    </Typography>
                  </TouchableOpacity>
                )}
              </Container>
              <ErrorModal error={requestCodeError} />
            </Container>
          </Container>
          <NumericKeyboard
            value={getValues()?.phoneNumberCode || ''}
            onChange={(value: string) => setValue('phoneNumberCode', value)}
            maxValueLength={CODE_LENGTH}
            hideDecimalPoint
          />
        </Container>
      }
      onBack={onBack}
      onNext={handleSubmit(onNext)}
      nextDisabled={isVerifyCodeLoading}
      nextLoading={isVerifyCodeLoading}
      error={verifyCodeError}
    />
  );
}

export default VerifyOtpCodeScreen
