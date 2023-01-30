import React, { useContext, useMemo } from 'react'

export type LayoutProvider = React.FC<
  React.PropsWithChildren<{
    indent?: number
  }>
>

export type LayoutPosition = number

const initialLayoutContext: LayoutPosition = 0

const contexts: Record<string, React.Context<LayoutPosition>> = {}

export const useLayout = (key: string): [LayoutPosition, LayoutProvider] => {
  if (!contexts[key]) {
    contexts[key] = React.createContext<LayoutPosition>(initialLayoutContext)
  }
  const indentation = useContext(contexts[key])

  const Provider = contexts[key].Provider
  const LayoutProvider: LayoutProvider = useMemo(
    () => (props) => {
      return (
        <Provider value={indentation + (props.indent || 0)}>
          {props.children}
        </Provider>
      )
    },
    [indentation]
  )

  return [indentation, LayoutProvider]
}
