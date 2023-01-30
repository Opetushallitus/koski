import React from 'react'
import { CommonProps, cx, common } from '../CommonProps'

export type PlainListProps = CommonProps<{
  children: React.ReactNode[]
}>

export const PlainList = (props: PlainListProps) => (
  <ul {...common(props, ['PlainList'])}>
    {props.children.map((child, index) => (
      <li key={index}>{child}</li>
    ))}
  </ul>
)
