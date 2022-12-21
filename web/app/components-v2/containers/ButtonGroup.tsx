import React from 'react'
import { baseProps, BaseProps } from '../baseProps'

export type ButtonGroupProps = React.PropsWithChildren<BaseProps>

export const ButtonGroup: React.FC<ButtonGroupProps> = (props) => (
  <nav {...baseProps(props, 'ButtonGroup')}>{props.children}</nav>
)
