import React, { useCallback, useMemo, useState } from 'react'

export type GlobalErrorContext = {
  readonly errors: GlobalError[]
  readonly push: (errors: GlobalError[]) => void
  readonly clearAll: () => void
}

export type GlobalError = {
  message: string
}

const GlobalErrorContext = React.createContext<GlobalErrorContext>({
  errors: [],
  push: () => {},
  clearAll: () => {}
})

export const GlobalErrorProvider: React.FC<React.PropsWithChildren> = (
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

export const useGlobalErrors = (): GlobalErrorContext =>
  React.useContext(GlobalErrorContext)
