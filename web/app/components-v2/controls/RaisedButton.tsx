import React from 'react'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { common, CommonProps } from '../CommonProps'
import { Trans } from '../texts/Trans'

export type RaisedButtonProps = CommonProps<{
  children: LocalizedString | string
  onClick?: () => void
  disabled?: boolean
  fullWidth?: boolean
}>

export const RaisedButton = (props: RaisedButtonProps) => (
  <button
    {...common(props, [
      'RaisedButton',
      props.fullWidth && 'RaisedButton__fullWidth'
    ])}
    onClick={props.onClick}
    disabled={props.disabled}
  >
    <Trans>{props.children}</Trans>
  </button>
)
