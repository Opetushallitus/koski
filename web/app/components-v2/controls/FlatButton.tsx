import React from 'react'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { common, CommonProps } from '../CommonProps'
import { Trans } from '../texts/Trans'

export type FlatButtonProps = CommonProps<{
  children: LocalizedString | string
  onClick: () => void
  fullWidth?: boolean
  disabled?: boolean
}>

export const FlatButton = (props: FlatButtonProps) => (
  <button
    {...common(props, [
      'FlatButton',
      props.fullWidth && 'FlatButton__fullWidth'
    ])}
    onClick={props.onClick}
    disabled={props.disabled}
  >
    <Trans>{props.children}</Trans>
  </button>
)
