import React, { PropsWithChildren } from 'react'
import { nonNull } from '../../util/fp/arrays'
import { mapRecordValues } from '../../util/fp/objects'
import { common, CommonProps } from '../CommonProps'

export const COLUMN_COUNT = 24

export type ColumnRowProps = CommonProps<
  PropsWithChildren<{
    component?: React.ComponentClass<CommonProps> | string
    indent?: number
    valign?: 'top' | 'center' | 'bottom'
    align?: 'left' | 'center' | 'right'
  }>
>

export const ColumnRow = (props: ColumnRowProps) => {
  const Component = props.component || 'section'
  return (
    <Component
      {...common(props, [
        'ColumnRow',
        props.indent && `ColumnRow-indent-${props.indent}`,
        props.valign && `ColumnRow-valign-${props.valign}`,
        props.align && `ColumnRow-align-${props.align}`
      ])}
    >
      {props.children}
    </Component>
  )
}

export type ResponsiveValueObj<T> = {
  default: T
  phone?: T
  small?: T
  large?: T
}
export type ResponsiveValueTarget = keyof ResponsiveValueObj<any>
export type ResponsiveValue<T> = T | ResponsiveValueObj<T>

export type ColumnProps = CommonProps<
  PropsWithChildren<{
    component?: React.ComponentClass<CommonProps> | string
    span?: ResponsiveValue<number>
    start?: ResponsiveValue<number>
    row?: number
    valign?: ResponsiveValue<'top' | 'center' | 'bottom'>
    align?: ResponsiveValue<'left' | 'center' | 'right'>
  }>
>

export const Column = (props: ColumnProps) => {
  const Component = props.component || 'div'
  return (
    <Component
      {...common(props, [
        'Column',
        ...responsiveClassNames(
          props.span,
          (name, span) => `Column-${name}-span-${span}`
        ),
        ...responsiveClassNames(
          props.start,
          (name, start) => `Column-${name}-start-${start + 1}`
        ),
        ...responsiveClassNames(
          props.align,
          (name, align) => `Column-${name}-align-${align}`
        ),
        ...responsiveClassNames(
          props.valign,
          (name, align) => `Column-${name}-valign-${align}`
        ),
        props.row !== undefined && `Column-row-${props.row + 1}`
      ])}
    >
      {props.children}
    </Component>
  )
}

const responsiveClassNames = <T extends string | number>(
  value: ResponsiveValue<T> | undefined,
  build: (name: string, value: T) => string
): string[] =>
  value === undefined
    ? []
    : typeof value === 'object'
    ? Object.entries(value)
        .map(([name, t]) => (t ? build(name, t) : null))
        .filter(nonNull)
    : [build('default', value)]

export const isResponsiveValueObj = <T,>(
  a: ResponsiveValue<T>
): a is ResponsiveValueObj<T> =>
  typeof a === 'object' && a !== null && (a as any).default !== undefined

export const mapResponsiveValue =
  <T, S>(f: (t: T) => S) =>
  (v: ResponsiveValue<T>): ResponsiveValue<S> =>
    isResponsiveValueObj<T>(v)
      ? (mapRecordValues(f)(v) as ResponsiveValue<S>)
      : f(v)

export const getResponsiveValueAt =
  (index: number) =>
  <T,>(value: ResponsiveValue<T[]>): ResponsiveValue<T> =>
    mapResponsiveValue((as: T[]) => as[index])(value)
