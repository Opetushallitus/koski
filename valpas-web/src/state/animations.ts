import { useCallback, useEffect, useMemo, useState } from "react"
import { animationsDisabled } from "../utils/environment"

export type UseComponentAppearDisappearProps = {
  hideDuration: number
  onHidden?: () => void
}

export type UseComponentAppearDisappearResult = {
  hidden: boolean
  hide: () => void
}

export const useComponentAppearDisappear = ({
  hideDuration,
  onHidden,
}: UseComponentAppearDisappearProps): UseComponentAppearDisappearResult => {
  const [hidden, setHidden] = useState(true)
  useEffect(() => setHidden(false), [])

  const hide = useCallback(() => {
    setHidden(true)
    onHidden && setTimeout(onHidden, hideDuration)
  }, [hideDuration, onHidden])

  const result: UseComponentAppearDisappearResult = useMemo(
    () =>
      animationsDisabled()
        ? {
            hidden: false,
            hide: () => onHidden?.(),
          }
        : {
            hidden,
            hide,
          },
    [hidden, hide, onHidden],
  )

  return result
}
