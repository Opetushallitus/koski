import React from 'react'
import { uncapitalize } from '../../util/strings'

export type UncapitalizedProps = {
  children?: string
}

export const Uncapitalized = (props: UncapitalizedProps) => (
  <>{props.children ? uncapitalize(props.children) : null}</>
)
