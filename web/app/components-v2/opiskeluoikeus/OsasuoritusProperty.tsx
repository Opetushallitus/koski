import React, { useMemo } from 'react'
import { useLayout } from '../../util/useDepth'
import { common, CommonPropsWithChildren } from '../CommonProps'
import { Column, ColumnRow, COLUMN_COUNT } from '../containers/Columns'
import { OSASUORITUSTABLE_DEPTH_KEY } from './OsasuoritusTable'

const LABEL_WIDTH_COLUMNS = 4

export type OsasuoritusPropertyProps = CommonPropsWithChildren<{
  label: string
}>

export const OsasuoritusProperty: React.FC<OsasuoritusPropertyProps> = (
  props
) => {
  const [layout, LayoutProvider] = useLayout(OSASUORITUSTABLE_DEPTH_KEY)
  return (
    <ColumnRow {...common(props, ['OsasuoritusProperty'])} indent={layout.col}>
      <OsasuoritusPropertyLabel>{props.label}</OsasuoritusPropertyLabel>
      <LayoutProvider indent={LABEL_WIDTH_COLUMNS}>
        {props.children}
      </LayoutProvider>
    </ColumnRow>
  )
}

export type OsasuoritusSubpropertyProps = CommonPropsWithChildren<{
  label: string
  rowNumber?: number
}>

export const OsasuoritusSubproperty: React.FC<OsasuoritusSubpropertyProps> = (
  props
) => (
  <>
    <OsasuoritusPropertyLabel row={props.rowNumber}>
      {props.label}
    </OsasuoritusPropertyLabel>
    <OsasuoritusPropertyValue row={props.rowNumber}>
      {props.children}
    </OsasuoritusPropertyValue>
  </>
)

export type OsasuoritusPropertyLabel = CommonPropsWithChildren<{
  row?: number
}>

export const OsasuoritusPropertyLabel: React.FC<OsasuoritusPropertyLabel> = (
  props
) => {
  const [layout] = useLayout(OSASUORITUSTABLE_DEPTH_KEY)
  return (
    <Column
      row={layout.row + (props.row || 0)}
      start={layout.col}
      span={LABEL_WIDTH_COLUMNS}
      {...common(props, ['OsasuoritusPropertyLabel'])}
    >
      {props.children}
    </Column>
  )
}

export type OsasuoritusPropertyValueProps = CommonPropsWithChildren<{
  row?: number
}>

export const OsasuoritusPropertyValue: React.FC<
  OsasuoritusPropertyValueProps
> = (props) => {
  const [layout] = useLayout(OSASUORITUSTABLE_DEPTH_KEY)
  const span = COLUMN_COUNT - layout.col - LABEL_WIDTH_COLUMNS
  return (
    <Column
      row={layout.row + (props.row || 0)}
      start={layout.col + LABEL_WIDTH_COLUMNS}
      span={span}
      {...common(props, ['OsasuoritusPropertyValue'])}
    >
      {props.children}
    </Column>
  )
}
