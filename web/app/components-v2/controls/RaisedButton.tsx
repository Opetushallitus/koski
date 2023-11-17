import React from 'react'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { common, CommonProps } from '../CommonProps'
import { Trans } from '../texts/Trans'
import { useTestId } from '../../appstate/useTestId'

export type RaisedButtonType = 'default' | 'dangerzone'

export type RaisedButtonProps = CommonProps<{
  children: LocalizedString | string
  onClick?: React.MouseEventHandler<HTMLButtonElement>
  disabled?: boolean
  fullWidth?: boolean
  type?: RaisedButtonType
  testId?: string
}>

export const RaisedButton = (props: RaisedButtonProps) => (
  <button
    {...common(props, [
      'RaisedButton',
      props.fullWidth && 'RaisedButton__fullWidth',
      props.type && `RaisedButton__${props.type}`
    ])}
    data-testid={useTestId(props.testId)}
    onClick={props.onClick}
    disabled={props.disabled}
  >
    <Trans>{props.children}</Trans>
  </button>
)
