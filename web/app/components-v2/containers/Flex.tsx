import { common, CommonPropsWithChildren } from '../CommonProps'
import React from 'react'

export const Flex: React.FC<CommonPropsWithChildren> = (props) => {
  return <div {...common(props, ['Flex'])}> {props.children}</div>
}
