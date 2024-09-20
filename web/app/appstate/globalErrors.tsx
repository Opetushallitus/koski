import React, { useCallback, useMemo, useState } from 'react'
import { PropsWithOnlyChildren } from '../util/react'

export type GlobalErrorContext = {
  // Lista virheistä
  readonly errors: GlobalError[]
  // Lisää uusia näytettäviä virheitä
  readonly push: (errors: GlobalError[]) => void
  // Kuittaa kaikki virheet
  readonly clearAll: () => void
}

/**
 * Palauttaa rajapinnan, jonka avulla pääsee käsiksi kaikkiin saman rajapinnan kautta julkaistuihin
 * virheilmoituksiin. Rajapinta tarjoaa uusien virheiden ilmoittamisen sekä niiden kuittaamisen.
 *
 * @returns GlobalErrorContext
 */
export const useGlobalErrors = (): GlobalErrorContext =>
  React.useContext(GlobalErrorContext)

// Context provider

export type GlobalError = {
  message: string
}

const GlobalErrorContext = React.createContext<GlobalErrorContext>({
  errors: [],
  push: () => {},
  clearAll: () => {}
})

export const GlobalErrorProvider: React.FC<PropsWithOnlyChildren> = (
  props
) => {
  const [errors, setErrors] = useState<GlobalError[]>([])
  const push = useCallback(
    (errs: GlobalError[]) => {
      setErrors([...errors, ...errs])
    },
    [errors]
  )
  const clearAll = useCallback(() => setErrors([]), [])

  const context = useMemo(
    () => ({
      errors,
      push,
      clearAll
    }),
    [errors, push, clearAll]
  )

  return (
    <GlobalErrorContext.Provider value={context}>
      {props.children}
    </GlobalErrorContext.Provider>
  )
}
