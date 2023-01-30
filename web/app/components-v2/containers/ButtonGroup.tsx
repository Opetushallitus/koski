import React from 'react'
import { common, CommonPropsWithChildren } from '../CommonProps'

export type ButtonGroupProps = CommonPropsWithChildren

export const ButtonGroup: React.FC<ButtonGroupProps> = (props) => (
  <nav {...common(props, ['ButtonGroup'])}>{props.children}</nav>
)
