import React, { useState } from 'react'
import { t } from '../../i18n/i18n'
import { useLayout } from '../../util/useDepth'
import { CommonProps, subTestId } from '../CommonProps'
import {
  Column,
  ColumnRow,
  COLUMN_COUNT,
  mapResponsiveValue,
  ResponsiveValue
} from '../containers/Columns'
import { Section } from '../containers/Section'
import { ExpandButton } from '../controls/ExpandButton'
import { IconButton } from '../controls/IconButton'
import { RaisedButton } from '../controls/RaisedButton'
import { Spacer } from '../layout/Spacer'
import { CHARCODE_REMOVE } from '../texts/Icon'

export const OSASUORITUSTABLE_DEPTH_KEY = 'OsasuoritusTable'

export type OsasuoritusTableProps<DATA_KEYS extends string> = CommonProps<{
  editMode: boolean
  rows: Array<OsasuoritusRowData<DATA_KEYS>>
  onRemove?: (index: number) => void
}>

export type OsasuoritusRowData<DATA_KEYS extends string> = {
  suoritusIndex: number
  osasuoritusIndex: number
  columns: Record<DATA_KEYS, React.ReactNode>
  content?: React.ReactElement
}

export const OsasuoritusTable = <DATA_KEYS extends string>(
  props: OsasuoritusTableProps<DATA_KEYS>
) => {
  const [openState, setOpenState] = useState<Record<number, boolean>>(
    props.rows.reduce(
      (prev, curr) => ({ ...prev, [`${curr.osasuoritusIndex}`]: false }),
      {}
    )
  )
  const isOpen = Object.values(openState).some((val) => val === true)
  return (
    <>
      {props.rows[0] && (
        <>
          <RaisedButton
            onClick={(e) => {
              e.preventDefault()
              setOpenState((oldState) =>
                Object.keys(oldState).reduce((prev, curr) => {
                  return { ...prev, [`${curr}`]: !isOpen }
                }, oldState)
              )
            }}
          >
            {isOpen ? t('Sulje kaikki') : t('Avaa kaikki')}
          </RaisedButton>
          <Spacer />
        </>
      )}
      {props.rows[0] && (
        <OsasuoritusHeader row={props.rows[0]} editMode={props.editMode} />
      )}
      {props.rows.map((row, index) => (
        <OsasuoritusRow
          key={index}
          editMode={props.editMode}
          row={row}
          isOpen={openState[`${row.osasuoritusIndex}`]}
          onClickExpand={() => {
            setOpenState((oldState) => ({
              ...oldState,
              [`${row.osasuoritusIndex}`]: !oldState[`${row.osasuoritusIndex}`]
            }))
          }}
          onRemove={props.onRemove ? () => props.onRemove?.(index) : undefined}
          testId={`suoritukset.${row.suoritusIndex}.osasuoritukset.${row.osasuoritusIndex}`}
        />
      ))}
    </>
  )
}

export type OsasuoritusRowProps<DATA_KEYS extends string> = CommonProps<{
  editMode: boolean
  row: OsasuoritusRowData<DATA_KEYS>
  isOpen: boolean
  onClickExpand: () => void
  onRemove?: () => void
}>

export const OsasuoritusHeader = <DATA_KEYS extends string>(
  props: Omit<OsasuoritusRowProps<DATA_KEYS>, 'isOpen' | 'onClickExpand'>
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
            span={index === 0 ? spans.nameHeader : spans.data}
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
            <ExpandButton
              expanded={props.isOpen}
              onChange={props.onClickExpand}
              label={t('Osasuoritus')}
              testId={subTestId(props, 'expand')}
            />
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
                testId={subTestId(props, 'delete')}
              />
            )}
          </Column>
        )}
      </ColumnRow>
      {props.isOpen && props.row.content && (
        <LayoutProvider indent={1}>
          <Section testId={subTestId(props, 'properties')}>
            {props.row.content}
          </Section>
        </LayoutProvider>
      )}
    </>
  )
}

const getSpans = (dataObj: object, depth?: number, canRemove?: boolean) => {
  const DATA_SPAN: ResponsiveValue<number> = { default: 4, phone: 8, small: 6 }

  const indent = depth || 0
  const leftIcons = 1
  const rightIcons = canRemove ? 1 : 0
  const dataCount = Object.values(dataObj).length
  const data = mapResponsiveValue(
    (w: number) => w * Math.max(0, dataCount - 1)
  )(DATA_SPAN)
  const name = mapResponsiveValue(
    (w: number) => COLUMN_COUNT - indent - leftIcons - w - rightIcons
  )(data)
  const nameHeader = mapResponsiveValue((w: number) => w + leftIcons)(name)

  return {
    indent,
    leftIcons,
    rightIcons,
    data: DATA_SPAN,
    name,
    nameHeader
  }
}

export const osasuoritusTestId = (
  suoritusIndex: number,
  osasuoritusIndex: number,
  subItem?: string
): string =>
  `suoritukset.${suoritusIndex}.osasuoritukset.${osasuoritusIndex}` +
  (subItem ? `.${subItem}` : '')
