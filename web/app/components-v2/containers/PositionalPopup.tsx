import React from 'react'
import { common, CommonPropsWithChildren } from '../CommonProps'

export type PositionalPopupProps = CommonPropsWithChildren

export const PositionalPopup: React.FC<PositionalPopupProps> = (props) => (
  <div {...common(props, ['PositionalPopup'])}>
    <div className="PositionalPopup--overlay">{props.children}</div>
  </div>
)

export type PositionalPopupHolderProps = CommonPropsWithChildren

export const PositionalPopupHolder: React.FC<PositionalPopupHolderProps> = (
  props
) => <div {...common(props, ['PositionalPopupHolder'])}>{props.children}</div>
