import { constant } from 'fp-ts/lib/function'
import { isNumber } from 'fp-ts/lib/number'
import React, { useMemo } from 'react'
import { TestIdLayer } from '../../appstate/useTestId'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { mapTimes } from '../../util/fp/arrays'
import { sum } from '../../util/numbers'
import { CommonProps, CommonPropsWithChildren, common } from '../CommonProps'
import { Trans } from '../texts/Trans'
import {
  COLUMN_COUNT,
  Column,
  ColumnRow,
  ResponsiveValue,
  getResponsiveValueAt,
  mapResponsiveValue
} from './Columns'

export type KeyValueTableProps = CommonPropsWithChildren

export const KeyValueTable = (props: KeyValueTableProps) => (
  <ul {...common(props, ['KeyValueTable'])}>{props.children}</ul>
)

export type KeyValueRowProps = CommonPropsWithChildren<{
  localizableLabel?: string | LocalizedString
  labelContent?: React.ReactNode
  indent?: number
  innerKeyValueTable?: boolean
  largeLabel?: boolean
}>

export const KeyValueRow = (props: KeyValueRowProps) => {
  const indent = props.indent || 0
  const nameSpans = props.innerKeyValueTable
    ? { default: 8, small: 12, phone: 16 }
    : props.largeLabel
      ? { default: 8, small: 12, phone: 12 }
      : { default: 4, small: 8, phone: 12 }
  const valueSpans = {
    default: 24 - nameSpans.default - indent,
    small: 24 - nameSpans.small - indent,
    phone: 24 - nameSpans.phone - indent
  }

  return props.children ? (
    <ColumnRow component="li" {...common(props, ['KeyValueRow'])}>
      {indent > 0 && <Column span={indent} />}
      <Column
        className="KeyValueRow__name"
        span={nameSpans}
        valign="top"
        component="span"
      >
        {props.labelContent ?? <Trans>{props.localizableLabel}</Trans>}
      </Column>
      <Column
        className="KeyValueRow__value"
        span={valueSpans}
        valign="top"
        component="div"
      >
        {props.children}
      </Column>
    </ColumnRow>
  ) : null
}

export type KeyColumnedValuesRowProps = CommonProps<{
  localizableName?: string | LocalizedString
  children: React.ReactNode[]
  columnSpans?: ResponsiveValue<Array<number | '*'>>
  /** Nimisarakkeen leveys ruudukon sarakkeissa. Oletuksena 4. */
  nameWidth?: number
}>

const DEFAULT_NAME_WIDTH = 4

export const KeyColumnedValuesRow = (props: KeyColumnedValuesRowProps) => {
  const nameWidth = props.nameWidth ?? DEFAULT_NAME_WIDTH
  const valueAreaWidth = COLUMN_COUNT - nameWidth
  const spans = useMemo(() => {
    if (props.columnSpans) {
      return mapResponsiveValue(calculateAutomaticWidths(valueAreaWidth))(
        props.columnSpans
      )
    } else {
      const autoWidth = Math.min(valueAreaWidth / props.children.length)
      return mapTimes(props.children.length, constant(autoWidth))
    }
  }, [props.columnSpans, props.children.length, valueAreaWidth])

  return props.children ? (
    <ColumnRow component="li" {...common(props, ['KeyValueRow'])}>
      <Column
        className="KeyValueRow__name"
        span={{ default: nameWidth }}
        valign="top"
        component="span"
      >
        <Trans>{props.localizableName}</Trans>
      </Column>
      {props.children.map((child, index) => (
        <Column
          className="KeyValueRow__value"
          span={getResponsiveValueAt(index)(spans) || 4}
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

const calculateAutomaticWidths =
  (valueAreaWidth: number) =>
  (columnSpans: Array<number | '*'>): number[] => {
    const fixed = sum(columnSpans.filter(isNumber) || [])
    const autoWidths = columnSpans.filter((s) => s === '*').length || 1
    const autoWidth = Math.min((valueAreaWidth - fixed) / autoWidths)
    return columnSpans.map((s) => (s === '*' ? autoWidth : s))
  }
