import React from 'react'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { common, CommonProps } from '../CommonProps'
import { Trans } from '../texts/Trans'
import { useTestId } from '../../appstate/useTestId'

export type FlatButtonProps = CommonProps<{
  children: LocalizedString | string
  onClick?: React.MouseEventHandler<HTMLButtonElement>
  fullWidth?: boolean
  disabled?: boolean
  compact?: boolean
  buttonRef?: React.MutableRefObject<HTMLButtonElement | null>
  testId?: string
}>

export const FlatButton = (props: FlatButtonProps) => (
  <button
    {...common(props, [
      'FlatButton',
      props.fullWidth && 'FlatButton__fullWidth',
      props.compact && 'FlatButton__compact'
    ])}
    onClick={props.onClick}
    disabled={props.disabled}
    ref={props.buttonRef}
    data-testid={useTestId(props.testId)}
  >
    <Trans>{props.children}</Trans>
  </button>
)
