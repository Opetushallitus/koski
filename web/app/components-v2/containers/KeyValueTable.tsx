import { constant } from 'fp-ts/lib/function'
import { isNumber } from 'fp-ts/lib/number'
import React, { useMemo } from 'react'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { mapTimes } from '../../util/fp/arrays'
import { sum } from '../../util/numbers'
import { common, CommonProps, CommonPropsWithChildren } from '../CommonProps'
import { Trans } from '../texts/Trans'
import { Column, ColumnRow, COLUMN_COUNT } from './Columns'

export type KeyValueTableProps = CommonPropsWithChildren

export const KeyValueTable = (props: KeyValueTableProps) => (
  <ul {...common(props, ['KeyValueTable'])}>{props.children}</ul>
)

export type KeyValueRowProps = CommonPropsWithChildren<{
  name?: string | LocalizedString
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

export type KeyColumnedValuesRowProps = CommonProps<{
  name?: string | LocalizedString
  children: React.ReactNode[]
  columnSpans?: Array<number | '*'>
}>

export const KeyColumnedValuesRow = (props: KeyColumnedValuesRowProps) => {
  const NAME_WIDTH = 4

  const spans = useMemo(() => {
    const VALUE_AREA_WIDTH = COLUMN_COUNT - NAME_WIDTH
    if (props.columnSpans) {
      const fixed = sum(props.columnSpans.filter(isNumber) || [])
      const autoWidths = props.columnSpans.filter((s) => s === '*').length || 1
      const autoWidth = Math.min((VALUE_AREA_WIDTH - fixed) / autoWidths)
      return props.columnSpans.map((s) => (s === '*' ? autoWidth : s))
    } else {
      const autoWidth = Math.min(VALUE_AREA_WIDTH / props.children.length)
      return mapTimes(props.children.length, constant(autoWidth))
    }
  }, [props.columnSpans, props.children.length])

  return props.children ? (
    <ColumnRow component="li" {...common(props, ['KeyValueRow'])}>
      <Column
        className="KeyValueRow__name"
        span={NAME_WIDTH}
        valign="top"
        component="span"
      >
        <Trans>{props.name}</Trans>
      </Column>
      {props.children.map((child, index) => (
        <Column
          className="KeyValueRow__value"
          span={spans[index] || 4}
          valign="top"
          component="span"
          key={index}
        >
          {child}
        </Column>
      ))}
    </ColumnRow>
  ) : null
}
