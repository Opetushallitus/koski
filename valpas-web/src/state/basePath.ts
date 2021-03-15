import React from "react"

const BasePathContext = React.createContext("/")

export const BasePathProvider = BasePathContext.Provider
export const useBasePath = () => React.useContext(BasePathContext)
