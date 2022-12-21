import React from 'react'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { BaseProps, baseProps } from '../baseProps'
import { Trans } from '../texts/Trans'

export type FlatButtonProps = BaseProps & {
  children: LocalizedString | string
  onClick: () => void
  fullWidth?: boolean
}

export const FlatButton = (props: FlatButtonProps) => (
  <button
    {...baseProps(
      props,
      'FlatButton',
      props.fullWidth && 'FlatButton__fullWidth'
    )}
    onClick={props.onClick}
  >
    <Trans>{props.children}</Trans>
  </button>
)
