import { constant } from 'fp-ts/lib/function'
import { isNumber } from 'fp-ts/lib/number'
import React, { useMemo } from 'react'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { mapTimes, nonNull } from '../../util/fp/arrays'
import { sum } from '../../util/numbers'
import {
  common,
  CommonProps,
  CommonPropsWithChildren,
  subTestId
} from '../CommonProps'
import { Trans } from '../texts/Trans'
import {
  Column,
  ColumnRow,
  COLUMN_COUNT,
  getResponsiveValueAt,
  mapResponsiveValue,
  ResponsiveValue
} from './Columns'

export type KeyValueTableProps = CommonPropsWithChildren

export const KeyValueTable = (props: KeyValueTableProps) => (
  <ul {...common(props, ['KeyValueTable'])}>{props.children}</ul>
)

export type KeyValueRowProps = CommonPropsWithChildren<{
  label?: string | LocalizedString
  indent?: number
}>

export const KeyValueRow = (props: KeyValueRowProps) => {
  const indent = props.indent || 0
  return props.children ? (
    <ColumnRow component="li" {...common(props, ['KeyValueRow'])}>
      {props.indent && <Column span={props.indent} />}
      <Column
        className="KeyValueRow__name"
        span={{ default: 4, small: 8, phone: 12 }}
        valign="top"
        component="span"
      >
        <Trans>{props.label}</Trans>
      </Column>
      <Column
        className="KeyValueRow__value"
        span={{ default: 20 - indent, small: 16 - indent, phone: 12 - indent }}
        valign="top"
        component="span"
        testId={subTestId(props, 'value')}
      >
        {props.children}
      </Column>
    </ColumnRow>
  ) : null
}

export type KeyColumnedValuesRowProps = CommonProps<{
  name?: string | LocalizedString
  children: React.ReactNode[]
  columnSpans?: ResponsiveValue<Array<number | '*'>>
  testIds?: string[]
}>

const NAME_WIDTH: ResponsiveValue<number> = { default: 4 }
const VALUE_AREA_WIDTH = COLUMN_COUNT - NAME_WIDTH.default

export const KeyColumnedValuesRow = (props: KeyColumnedValuesRowProps) => {
  const spans = useMemo(() => {
    if (props.columnSpans) {
      return mapResponsiveValue(calculateAutomaticWidths)(props.columnSpans)
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
          span={getResponsiveValueAt(index)(spans) || 4}
          valign="top"
          component="span"
          key={index}
          testId={[props.testId, props.testIds?.[index]]
            .filter(nonNull)
            .join('.')}
        >
          {child}
        </Column>
      ))}
    </ColumnRow>
  ) : null
}

const calculateAutomaticWidths = (
  columnSpans: Array<number | '*'>
): number[] => {
  const fixed = sum(columnSpans.filter(isNumber) || [])
  const autoWidths = columnSpans.filter((s) => s === '*').length || 1
  const autoWidth = Math.min((VALUE_AREA_WIDTH - fixed) / autoWidths)
  return columnSpans.map((s) => (s === '*' ? autoWidth : s))
}
