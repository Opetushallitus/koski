import React from 'react'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { common, CommonProps, CommonPropsWithChildren } from '../CommonProps'
import { Trans } from '../texts/Trans'
import { Column, ColumnRow } from './Columns'

export type KeyValueTableProps = CommonPropsWithChildren

export const KeyValueTable = (props: KeyValueTableProps) => (
  <ul {...common(props, ['KeyValueTable'])}>{props.children}</ul>
)

export type KeyValueRowProps = CommonPropsWithChildren<{
  name: string | LocalizedString
  indent?: number
}>

export const KeyValueRow = (props: KeyValueRowProps) =>
  props.children ? (
    <ColumnRow component="li" {...common(props, ['KeyValueRow'])}>
      {props.indent && <Column span={props.indent} />}
      <Column
        className="KeyValueRow__name"
        span={4}
        valign="top"
        component="span"
      >
        <Trans>{props.name}</Trans>
      </Column>
      <Column
        className="KeyValueRow__value"
        span={20 - (props.indent || 0)}
        valign="top"
        component="span"
      >
        {props.children}
      </Column>
    </ColumnRow>
  ) : null

export type KeyMultiValueRowProps = CommonProps<{
  name: string | LocalizedString
  children: React.ReactNode[]
  columnSpans?: number[]
}>

export const KeyMultiValueRow = (props: KeyMultiValueRowProps) =>
  props.children ? (
    <ColumnRow component="li" {...common(props, ['KeyValueRow'])}>
      <Column
        className="KeyValueRow__name"
        span={4}
        valign="top"
        component="span"
      >
        <Trans>{props.name}</Trans>
      </Column>
      {props.children.map((child, index) => (
        <Column
          className="KeyValueRow__value"
          span={props.columnSpans?.[index] || 4}
          valign="top"
          component="span"
          key={index}
        >
          {child}
        </Column>
      ))}
    </ColumnRow>
  ) : null
