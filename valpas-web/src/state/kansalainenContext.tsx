import React from "react"

const KansalainenContext = React.createContext<boolean>(false)

export type KansalainenContextProviderProps = {
  children: React.ReactNode
}
export const KansalainenContextProvider = (
  props: KansalainenContextProviderProps,
) => <KansalainenContext.Provider value={true} {...props} />

export const useIsKansalainenView = () => React.useContext(KansalainenContext)
