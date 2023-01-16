import React from 'react'
import { common, CommonProps } from '../CommonProps'

export const COLUMN_COUNT = 24

export type ColumnRowProps = CommonProps<{
  component?: React.ComponentClass | string
  children?: React.ReactNode
  indent?: number
}>

export const ColumnRow = (props: ColumnRowProps) => {
  const Component = props.component || 'section'
  return (
    <Component
      {...common(props, [
        'ColumnRow',
        props.indent && `ColumnRow-indent-${props.indent}`
      ])}
    >
      {props.children}
    </Component>
  )
}

export type ColumnProps = CommonProps<{
  component?: React.ComponentClass | string
  children?: React.ReactNode
  span?: number
  spanPhone?: number
  spanSmall?: number
  spanLarge?: number
  start?: number
  startPhone?: number
  startSmall?: number
  startLarge?: number
  row?: number
  valign?: 'top' | 'center' | 'bottom'
  align?: 'left' | 'center' | 'right'
}>

export const Column = (props: ColumnProps) => {
  const Component = props.component || 'div'
  return (
    <Component
      {...common(props, [
        'Column',
        props.span && `Column-default-span-${props.span}`,
        props.spanPhone && `Column-phone-span-${props.spanPhone}`,
        props.spanSmall && `Column-small-span-${props.spanSmall}`,
        props.spanLarge && `Column-large-span-${props.spanLarge}`,
        props.start !== undefined && `Column-default-start-${props.start + 1}`,
        props.startPhone !== undefined &&
          `Column-phone-start-${props.startPhone + 1}`,
        props.startSmall !== undefined &&
          `Column-small-start-${props.startSmall + 1}`,
        props.startLarge !== undefined &&
          `Column-large-start-${props.startLarge + 1}`,
        props.row !== undefined && `Column-row-${props.row + 1}`,
        props.valign && `Column-valign-${props.valign}`,
        props.align && `Column-align-${props.align}`
      ])}
    >
      {props.children}
    </Component>
  )
}
