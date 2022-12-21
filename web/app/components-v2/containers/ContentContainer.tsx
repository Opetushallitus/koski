import React from 'react'
import { baseProps, BaseProps } from '../baseProps'

export type ContentContainerProps = React.PropsWithChildren<BaseProps>

export const ContentContainer: React.FC<ContentContainerProps> = (props) => (
  <div {...baseProps(props, 'ContentContainer')}>{props.children}</div>
)
