import React from 'react'
import { common, CommonPropsWithChildren } from '../CommonProps'

export type ContentContainerProps = CommonPropsWithChildren

export const ContentContainer: React.FC<ContentContainerProps> = (props) => (
  <div {...common(props, ['ContentContainer'])}>{props.children}</div>
)
