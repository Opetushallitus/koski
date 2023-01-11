import React from 'react'
import { baseProps, BaseProps } from '../baseProps'

export type ColumnGridProps = BaseProps & {
  component?: React.ComponentClass | string
  children?: React.ReactNode
}

export const ColumnGrid = (props: ColumnGridProps) => {
  const Component = props.component || 'section'
  return (
    <Component {...baseProps(props, 'ColumnGrid')}>{props.children}</Component>
  )
}

export type ColumnProps = BaseProps & {
  component?: React.ComponentClass | string
  children?: React.ReactNode
  span: number
  spanPhone?: number
  spanSmall?: number
  spanLarge?: number
  valign?: 'top' | 'center' | 'bottom'
  align?: 'left' | 'center' | 'right'
}

export const Column = (props: ColumnProps) => {
  const Component = props.component || 'div'
  return (
    <Component
      {...baseProps(
        props,
        'Column',
        `Column-span-${props.span}`,
        props.spanPhone && `Column-phone-${props.spanPhone}`,
        props.spanSmall && `Column-small-${props.spanSmall}`,
        props.spanLarge && `Column-large-${props.spanLarge}`,
        props.valign && `Column-valign-${props.valign}`,
        props.align && `Column-align-${props.align}`
      )}
    >
      {props.children}
    </Component>
  )
}
