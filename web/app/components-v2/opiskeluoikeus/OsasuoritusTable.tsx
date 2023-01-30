import React, { useState } from 'react'
import { t } from '../../i18n/i18n'
import { useLayout } from '../../util/useDepth'
import { CommonProps } from '../CommonProps'
import { Column, ColumnRow } from '../containers/Columns'
import { ExpandButton } from '../controls/ExpandButton'
import { IconButton } from '../controls/IconButton'
import { CHARCODE_REMOVE } from '../texts/Icon'

export const OSASUORITUSTABLE_DEPTH_KEY = 'OsasuoritusTable'

export type OsasuoritusTableProps<DATA_KEYS extends string> = CommonProps<{
  editMode: boolean
  rows: Array<OsasuoritusRowData<DATA_KEYS>>
  onRemove?: (index: number) => void
}>

export type OsasuoritusRowData<DATA_KEYS extends string> = {
  columns: Record<DATA_KEYS, React.ReactNode>
  content?: React.ReactElement
}

export const OsasuoritusTable = <DATA_KEYS extends string>(
  props: OsasuoritusTableProps<DATA_KEYS>
) => {
  return (
    <>
      {props.rows[0] && (
        <OsasuoritusHeader row={props.rows[0]} editMode={props.editMode} />
      )}
      {props.rows.map((row, index) => (
        <OsasuoritusRow
          key={index}
          editMode={props.editMode}
          row={row}
          onRemove={props.onRemove ? () => props.onRemove?.(index) : undefined}
        />
      ))}
    </>
  )
}

export type OsasuoritusRowProps<DATA_KEYS extends string> = CommonProps<{
  editMode: boolean
  row: OsasuoritusRowData<DATA_KEYS>
  onRemove?: () => void
}>

export const OsasuoritusHeader = <DATA_KEYS extends string>(
  props: OsasuoritusRowProps<DATA_KEYS>
) => {
  const [indentation] = useLayout(OSASUORITUSTABLE_DEPTH_KEY)
  const spans = getSpans(props.row.columns, indentation)
  return (
    <>
      <ColumnRow className="OsasuoritusHeader">
        {spans.indent > 0 && (
          <Column span={spans.indent} className="OsasuoritusHeader__indent" />
        )}
        {Object.keys(props.row.columns).map((key, index) => (
          <Column
            key={index}
            span={index === 0 ? spans.name + spans.leftIcons : spans.data}
          >
            {t(key)}
          </Column>
        ))}
      </ColumnRow>
    </>
  )
}

export const OsasuoritusRow = <DATA_KEYS extends string>(
  props: OsasuoritusRowProps<DATA_KEYS>
) => {
  const [indentation, LayoutProvider] = useLayout(OSASUORITUSTABLE_DEPTH_KEY)
  const [isOpen, setOpen] = useState(false)
  const spans = getSpans(
    props.row.columns,
    indentation,
    Boolean(props.editMode && props.onRemove)
  )

  return (
    <>
      <ColumnRow className="OsasuoritusRow">
        {spans.indent > 0 && (
          <Column span={spans.indent} className="OsasuoritusHeader__indent" />
        )}
        <Column span={spans.leftIcons} align="right">
          {props.row.content && (
            <ExpandButton expanded={isOpen} onChange={setOpen} label="TODO" />
          )}
        </Column>
        {Object.values<React.ReactNode>(props.row.columns).map(
          (value, index) => (
            <Column key={index} span={index === 0 ? spans.name : spans.data}>
              {value}
            </Column>
          )
        )}
        {props.editMode && props.onRemove && (
          <Column span={spans.rightIcons}>
            {props.row.content && (
              <IconButton
                charCode={CHARCODE_REMOVE}
                label={t('Poista')}
                size="input"
                onClick={props.onRemove}
              />
            )}
          </Column>
        )}
      </ColumnRow>
      {isOpen && props.row.content && (
        <LayoutProvider indent={1}>{props.row.content}</LayoutProvider>
      )}
    </>
  )
}

const getSpans = (dataObj: object, depth?: number, canRemove?: boolean) => {
  const DATA_SPAN = 4

  const indent = depth || 0
  const leftIcons = 1
  const rightIcons = canRemove ? 1 : 0
  const dataCount = Object.values(dataObj).length
  const data = DATA_SPAN * Math.max(0, dataCount - 1)
  const name = 24 - indent - leftIcons - data - rightIcons

  return {
    indent,
    leftIcons,
    rightIcons,
    data: DATA_SPAN,
    name
  }
}
