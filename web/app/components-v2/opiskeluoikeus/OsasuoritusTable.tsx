import React, { useCallback } from 'react'
import { t } from '../../i18n/i18n'
import { Opiskeluoikeus } from '../../types/fi/oph/koski/schema/Opiskeluoikeus'
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
import { FormOptic } from '../forms/FormModel'
import { Spacer } from '../layout/Spacer'
import { CHARCODE_REMOVE } from '../texts/Icon'

export const OSASUORITUSTABLE_DEPTH_KEY = 'OsasuoritusTable'

export type ExpandState = {
  key: string
  expanded: boolean
}

export type OsasuorituksetExpandedState = Array<ExpandState>

export const constructOsasuorituksetOpenState = (
  prevState: Array<ExpandState>,
  level: number,
  suoritusIndex: number,
  // TODO: Tyyppi
  osasuoritukset: any[]
): OsasuorituksetExpandedState => {
  return osasuoritukset.reduce<OsasuorituksetExpandedState>(
    (prev, _curr, i) => {
      const key = `level_${level}_suoritus_${suoritusIndex}_osasuoritus_${i}`
      const existing = prevState.find((v) => v.key === key)
      if (
        'osasuoritukset' in osasuoritukset[i] &&
        Array.isArray(osasuoritukset[i].osasuoritukset) &&
        osasuoritukset[i].osasuoritukset.length > 0
      ) {
        return [
          ...prev.filter((p) => p.key !== key),
          {
            key,
            expanded: existing !== undefined ? existing.expanded : false
          },
          ...constructOsasuorituksetOpenState(
            prevState,
            level + 1,
            i,
            osasuoritukset[i].osasuoritukset
          )
        ]
      } else {
        return [
          ...prev.filter((p) => p.key !== key),
          {
            key,
            expanded: existing !== undefined ? existing.expanded : false
          }
        ]
      }
    },
    prevState
  )
}

type Completed = (osasuoritusIndex: number) => boolean | undefined
export type SetOsasuoritusOpen = (key: string, value: boolean) => void
export type ToggleOsasuoritusOpen = () => void
export type ToggleModal = () => void
export type SetModal = React.Dispatch<
  React.SetStateAction<{
    open: boolean
    data: {
      osasuoritusPath: FormOptic<any, any>
    } | null
  }>
>
export type OsasuoritusTableProps<DATA_KEYS extends string> = CommonProps<{
  editMode: boolean
  level: number
  toggleModal: ToggleModal
  setModal?: SetModal // TODO: Abstraktoi tämä VST-spesifinen logiikka pois
  setOsasuoritusOpen: SetOsasuoritusOpen
  pathWithOsasuoritukset?: FormOptic<Opiskeluoikeus, any>
  openState: OsasuorituksetExpandedState
  rows: Array<OsasuoritusRowData<DATA_KEYS>>
  completed?: Completed
  onRemove?: (index: number) => void
}>

export type OsasuoritusRowData<DATA_KEYS extends string> = {
  suoritusIndex: number
  osasuoritusIndex: number
  osasuoritusPath?: FormOptic<Opiskeluoikeus, any>
  expandable: boolean
  columns: Partial<Record<DATA_KEYS, React.ReactNode>>
  content?: React.ReactElement
}

function getOsasuoritusButtonText() {
  // TODO: Suorituksesta riippuva tekstin nimi
  return t('Lisää osasuoritus')
}

export const OsasuoritusTable = <DATA_KEYS extends string>(
  props: OsasuoritusTableProps<DATA_KEYS>
) => {
  const {
    editMode,
    level,
    onRemove,
    setOsasuoritusOpen,
    completed,
    setModal,
    pathWithOsasuoritukset,
    rows,
    openState
  } = props

  const onClickLisääOsasuoritus = useCallback(
    (e: React.MouseEvent<HTMLButtonElement, MouseEvent>) => {
      e.preventDefault()
      if (setModal !== undefined && pathWithOsasuoritukset !== undefined) {
        pathWithOsasuoritukset &&
          setModal({
            open: true,
            data: { osasuoritusPath: pathWithOsasuoritukset }
          })
      } else {
        console.warn(
          'onClickLisääOsasuoritus: setModal or pathWithOsasuoritukset is undefined.'
        )
      }
    },
    [pathWithOsasuoritukset, setModal]
  )

  const onRemoveCb = useCallback(
    (index: number) => {
      return () => {
        if (onRemove !== undefined) {
          onRemove(index)
        }
      }
    },
    [onRemove]
  )

  return (
    <>
      {rows[0] && <OsasuoritusHeader row={rows[0]} editMode={editMode} />}
      {rows.map((row, index) => (
        <OsasuoritusRow
          key={index}
          editMode={editMode}
          row={row}
          isExpanded={
            openState.find(
              (s) =>
                s.key ===
                `level_${level}_suoritus_${row.suoritusIndex}_osasuoritus_${row.osasuoritusIndex}`
            )?.expanded || false
          }
          expandedState={openState}
          expandable={row.expandable}
          completed={completed ? completed(index) : undefined}
          onClickExpand={() => {
            setOsasuoritusOpen(
              `level_${level}_suoritus_${row.suoritusIndex}_osasuoritus_${row.osasuoritusIndex}`,
              !openState.find(
                (s) =>
                  s.key ===
                  `level_${level}_suoritus_${row.suoritusIndex}_osasuoritus_${row.osasuoritusIndex}`
              )?.expanded
            )
          }}
          onRemove={onRemoveCb(index)}
          testId={`suoritukset.${row.suoritusIndex}.taso.${props.level}.osasuoritukset.${row.osasuoritusIndex}`}
        />
      ))}
      <Spacer />
      {/*
      TODO: Otettu pois väliaikaisesti päältä TPO:n takia.
      editMode && (
        <ColumnRow indent={level}>
          <Column>
            <RaisedButton onClick={onClickLisääOsasuoritus}>
              {getOsasuoritusButtonText()}
            </RaisedButton>
          </Column>
        </ColumnRow>
      )*/}
      <Spacer />
    </>
  )
}

export type OsasuoritusRowProps<DATA_KEYS extends string> = CommonProps<{
  editMode: boolean
  completed?: boolean
  expandable?: boolean
  row: OsasuoritusRowData<DATA_KEYS>
  expandedState: OsasuorituksetExpandedState
  isExpanded: OsasuorituksetExpandedState[number]['expanded']
  onClickExpand: () => void
  onRemove?: () => void
}>

export const OsasuoritusHeader = <DATA_KEYS extends string>(
  props: Omit<
    OsasuoritusRowProps<DATA_KEYS>,
    'expandedState' | 'isExpanded' | 'onClickExpand'
  >
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

  const isAllExpanded = props.expandedState.every((s) => s.expanded === true)

  const expandable = props.expandable === undefined ? true : props.expandable
  const expanded = isAllExpanded || props.isExpanded

  return (
    <>
      <ColumnRow className="OsasuoritusRow">
        {spans.indent > 0 && (
          <Column span={spans.indent} className="OsasuoritusHeader__indent" />
        )}
        <Column span={spans.leftIcons} align="right">
          {props.row.content && expandable && (
            <ExpandButton
              expanded={expanded}
              onChange={props.onClickExpand}
              label={t('Osasuoritus')}
              testId={subTestId(props, 'expand')}
            />
          )}
        </Column>
        <Column span={1}>
          {props.completed === true && (
            // eslint-disable-next-line react/jsx-no-literals
            <span aria-label={t('Suoritus valmis')}>&#x2713;</span>
          )}
          {props.completed === false && (
            // eslint-disable-next-line react/jsx-no-literals
            <span aria-label={t('Suoritus kesken')}>&#x29D6;</span>
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
      {expandable && expanded && props.row.content && (
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
  const completed = 1
  const rightIcons = canRemove ? 1 : 0
  const dataCount = Object.values(dataObj).length
  const data = mapResponsiveValue(
    (w: number) => w * Math.max(0, dataCount - 1)
  )(DATA_SPAN)
  const name = mapResponsiveValue(
    (w: number) =>
      COLUMN_COUNT - indent - leftIcons - completed - w - rightIcons
  )(data)
  const nameHeader = mapResponsiveValue(
    (w: number) => w + leftIcons + completed
  )(name)

  return {
    indent,
    leftIcons,
    completed,
    rightIcons,
    data: DATA_SPAN,
    name,
    nameHeader
  }
}

export const osasuoritusTestId = (
  suoritusIndex: number,
  levelIndex: number,
  osasuoritusIndex: number,
  subItem?: string
): string =>
  `suoritukset.${suoritusIndex}.taso.${levelIndex}.osasuoritukset.${osasuoritusIndex}` +
  (subItem ? `.${subItem}` : '')
