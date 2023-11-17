import React, { useEffect, useState } from 'react'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { Trans } from '../texts/Trans'
import { CommonProps } from '../CommonProps'
import { useTestId } from '../../appstate/useTestId'

export type SnackbarProps = CommonProps<{
  timeout?: number
  children: string | LocalizedString
}>

export const Snackbar: React.FC<SnackbarProps> = (props) => {
  const [visible, setVisible] = useState(true)
  useEffect(() => {
    const timeout = setTimeout(() => setVisible(false), props.timeout || 3000)
    return () => clearTimeout(timeout)
  }, [props.timeout])
  const testId = useTestId('snackbar')

  return visible ? (
    <aside
      className="Snackbar"
      onClick={() => setVisible(false)}
      data-testid={testId}
    >
      <Trans>{props.children}</Trans>
    </aside>
  ) : null
}
