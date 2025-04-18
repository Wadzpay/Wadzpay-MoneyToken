/* eslint-disable @typescript-eslint/no-empty-function */
import React, { PropsWithChildren, useState } from "react"
import env from "~/env"

import { isDev } from "~/utils"

type EnvironmentContextType = {
  isLocal: boolean
  setIsLocal: (value: boolean) => void
  isFakeOnRamp: boolean
  setIsFakeOnRamp: (value: boolean) => void
  onRampCountry: string
  setOnRampCountry: (value: string) => void
}

export const EnvironmentContext = React.createContext<EnvironmentContextType>({
  isLocal: isDev,
  setIsLocal: () => {},
  isFakeOnRamp: isDev,
  setIsFakeOnRamp: () => {},
  onRampCountry: env.DEFAULT_COUNTRY,
  setOnRampCountry: () => {}
})

type Props = PropsWithChildren<{}>

export const EnvironmentContextProvider: React.FC<Props> = ({
  children
}: Props) => {
  const [isLocal, setIsLocal] = useState(false)
  const [isFakeOnRamp, setIsFakeOnRamp] = useState(isDev)
  const [onRampCountry, setOnRampCountry] = useState(env.DEFAULT_COUNTRY)

  return (
    <EnvironmentContext.Provider
      value={{
        isLocal,
        setIsLocal,
        isFakeOnRamp,
        setIsFakeOnRamp,
        onRampCountry,
        setOnRampCountry
      }}
    >
      {children}
    </EnvironmentContext.Provider>
  )
}
