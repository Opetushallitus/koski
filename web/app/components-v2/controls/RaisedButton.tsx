import React from 'react'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { BaseProps, baseProps } from '../baseProps'
import { Trans } from '../texts/Trans'

export type RaisedButtonProps = BaseProps & {
  children: LocalizedString | string
  onClick?: () => void
  disabled?: boolean
  fullWidth?: boolean
}

export const RaisedButton = (props: RaisedButtonProps) => (
  <button
    {...baseProps(
      props,
      'RaisedButton',
      props.fullWidth && 'RaisedButton__fullWidth'
    )}
    onClick={props.onClick}
    disabled={props.disabled}
  >
    <Trans>{props.children}</Trans>
  </button>
)
