import React from 'react'

export type LowercaseProps = {
  children?: string
}

export const Lowercase = (props: LowercaseProps) => (
  <>{props.children ? props.children.toLowerCase() : null}</>
)
