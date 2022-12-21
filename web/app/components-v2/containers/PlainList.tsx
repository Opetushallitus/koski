import React from 'react'
import { baseProps, BaseProps } from '../baseProps'

export type PlainListProps = BaseProps & {
  children: React.ReactNode[]
}

export const PlainList = (props: PlainListProps) => (
  <ul {...baseProps(props, 'PlainList')}>
    {props.children.map((child, index) => (
      <li key={index}>{child}</li>
    ))}
  </ul>
)
