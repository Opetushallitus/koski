import React from 'react'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { baseProps, BaseProps } from '../baseProps'
import { Trans } from '../texts/Trans'
import { Column, ColumnGrid } from './ColumnGrid'

export type KeyValueTableProps = BaseProps & {
  children: React.ReactNode
}

export const KeyValueTable = (props: KeyValueTableProps) => (
  <ul {...baseProps(props, 'KeyValueTable')}>{props.children}</ul>
)

export type KeyValueRowProps = BaseProps & {
  name: string | LocalizedString
  children?: React.ReactNode
  indent?: number
}

export const KeyValueRow = (props: KeyValueRowProps) =>
  props.children ? (
    <ColumnGrid component="li" {...baseProps(props, 'KeyValueRow')}>
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
    </ColumnGrid>
  ) : null

export type KeyMultiValueRowProps = BaseProps & {
  name: string | LocalizedString
  children: React.ReactNode[]
  columnSpans?: number[]
}

export const KeyMultiValueRow = (props: KeyMultiValueRowProps) =>
  props.children ? (
    <ColumnGrid component="li" {...baseProps(props, 'KeyValueRow')}>
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
    </ColumnGrid>
  ) : null
