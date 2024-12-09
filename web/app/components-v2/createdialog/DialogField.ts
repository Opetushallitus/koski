import { useState, useEffect, useCallback } from 'react'

export type DialogField<T> = {
  value?: T
  set: (t?: T) => void
  visible: boolean
  setVisible: (visible: boolean) => void
}

export const useDialogField = <T>(
  isVisible: boolean,
  defaultValue?: () => T
): DialogField<T> => {
  const [value, set] = useState<T | undefined>(defaultValue)
  const [visible, setVisible] = useState<boolean>(false)

  const setVisibility = useCallback(
    (b: boolean) => {
      setVisible(b)
      if (!b) {
        set(defaultValue)
      }
    },
    [defaultValue]
  )

  useEffect(() => {
    setVisibility(isVisible)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isVisible])

  return { value, set, visible, setVisible: setVisibility }
}
