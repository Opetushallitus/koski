import React, { useMemo } from 'react'
import { mapTimes } from '../../util/fp/arrays'
import { common, CommonProps } from '../CommonProps'
import { Column, ColumnRow, COLUMN_COUNT } from './Columns'

export type MultiFieldProps = CommonProps<{
  children: React.ReactNode[]
}>

export const MultiField: React.FC<MultiFieldProps> = (props) => {
  const spans = useMemo(() => {
    const fieldCount = props.children.length
    const span = Math.min(COLUMN_COUNT / fieldCount)
    const remainder = COLUMN_COUNT - span * fieldCount
    return mapTimes(fieldCount, (i) =>
      i < fieldCount - 1 ? span : span + remainder
    )
  }, [props.children.length])

  return (
    <ColumnRow {...common(props, ['MultiField'])} valign="top">
      {props.children.map((child, i) => (
        <Column key={i} span={spans[i]}>
          {child}
        </Column>
      ))}
    </ColumnRow>
  )
}
