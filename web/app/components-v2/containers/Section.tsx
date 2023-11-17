import React from 'react'
import { common, CommonPropsWithChildren } from '../CommonProps'

export type SectionProps = CommonPropsWithChildren

export const Section: React.FC<SectionProps> = (props) => (
  <section {...common(props)}>{props.children}</section>
)
