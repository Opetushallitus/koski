import React, { useState } from 'react'
import { t } from '../../i18n/i18n'
import { useDepth } from '../../util/useDepth'
import { BaseProps } from '../baseProps'
import { Column, ColumnGrid } from '../containers/ColumnGrid'
import { ExpandButton } from '../controls/ExpandButton'

export const OSASUORITUSTABLE_DEPTH_KEY = 'OsasuoritusTable'

export type OsasuoritusTableProps<DATA_KEYS extends string> = BaseProps & {
  rows: Array<OsasuoritusRowData<DATA_KEYS>>
}

export type OsasuoritusRowData<DATA_KEYS extends string> = {
  columns: Record<DATA_KEYS, React.ReactNode>
  getContent?: () => React.ReactNode
}

export const OsasuoritusTable = <DATA_KEYS extends string>(
  props: OsasuoritusTableProps<DATA_KEYS>
) => (
  <>
    {props.rows[0] && <OsasuoritusHeader row={props.rows[0]} />}
    {props.rows.map((row, index) => (
      <OsasuoritusRow key={index} row={row} />
    ))}
  </>
)

export type OsasuoritusRowProps<DATA_KEYS extends string> = BaseProps & {
  row: OsasuoritusRowData<DATA_KEYS>
}

export const OsasuoritusHeader = <DATA_KEYS extends string>(
  props: OsasuoritusRowProps<DATA_KEYS>
) => {
  const [depth] = useDepth(OSASUORITUSTABLE_DEPTH_KEY)
  const spans = getSpans(props.row.columns, depth)
  return (
    <>
      <ColumnGrid className="OsasuoritusHeader">
        {spans.indent > 0 && (
          <Column
            span={spans.indent}
            className="OsasuoritusHeader__indent"
          ></Column>
        )}
        <Column span={spans.icons}></Column>
        {Object.keys(props.row.columns).map((key, index) => (
          <Column key={index} span={index === 0 ? spans.name : spans.data}>
            {t(key)}
          </Column>
        ))}
      </ColumnGrid>
    </>
  )
}

export const OsasuoritusRow = <DATA_KEYS extends string>(
  props: OsasuoritusRowProps<DATA_KEYS>
) => {
  const [depth, DeeperLevel] = useDepth(OSASUORITUSTABLE_DEPTH_KEY)
  const [isOpen, setOpen] = useState(false)
  const spans = getSpans(props.row.columns, depth)

  return (
    <>
      <ColumnGrid className="OsasuoritusRow">
        {spans.indent > 0 && (
          <Column span={spans.indent} className="OsasuoritusHeader__indent" />
        )}
        <Column span={spans.icons} align="right">
          {props.row.getContent && (
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
      </ColumnGrid>
      {isOpen && <DeeperLevel>{props.row.getContent?.()}</DeeperLevel>}
    </>
  )
}

const getSpans = (dataObj: object, depth?: number) => {
  const DATA_SPAN = 4

  const indent = depth || 0
  const icons = 1
  const dataCount = Object.values(dataObj).length
  const data = DATA_SPAN * Math.max(0, dataCount - 1)
  const name = 24 - indent - icons - data

  return {
    indent,
    icons,
    data: DATA_SPAN,
    name
  }
}
