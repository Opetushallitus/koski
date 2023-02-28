import React from 'react'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { common, CommonProps, testId } from '../CommonProps'
import { Trans } from '../texts/Trans'

export type FlatButtonProps = CommonProps<{
  children: LocalizedString | string
  onClick: React.MouseEventHandler<HTMLButtonElement>
  fullWidth?: boolean
  disabled?: boolean
  compact?: boolean
  buttonRef?: React.MutableRefObject<HTMLButtonElement | null>
}>

export const FlatButton = (props: FlatButtonProps) => (
  <button
    {...common(props, [
      'FlatButton',
      props.fullWidth && 'FlatButton__fullWidth',
      props.compact && 'FlatButton__compact'
    ])}
    {...testId(props)}
    onClick={props.onClick}
    disabled={props.disabled}
    ref={props.buttonRef}
  >
    <Trans>{props.children}</Trans>
  </button>
)
