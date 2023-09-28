import React, { useCallback } from 'react'
import { t } from '../../i18n/i18n'
import { Opiskeluoikeus } from '../../types/fi/oph/koski/schema/Opiskeluoikeus'
import { useLayout } from '../../util/useDepth'
import {
  OsasuorituksetExpandedState,
  SetOsasuoritusOpen
} from '../../osasuoritus/hooks'
import { CommonProps, subTestId, testId } from '../CommonProps'
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
import { FormModel, FormOptic } from '../forms/FormModel'
import { Spacer } from '../layout/Spacer'
import { CHARCODE_REMOVE } from '../texts/Icon'

export const OSASUORITUSTABLE_DEPTH_KEY = 'OsasuoritusTable'

type Completed = (osasuoritusIndex: number) => boolean | undefined

export type OsasuoritusTableProps<
  DATA_KEYS extends string,
  P = object
> = CommonProps<{
  editMode: FormModel<object>['editMode']
  level: number
  setOsasuoritusOpen: SetOsasuoritusOpen
  openState: OsasuorituksetExpandedState
  rows: Array<OsasuoritusRowData<DATA_KEYS>>
  completed?: Completed
  onRemove?: (index: number) => void
  addNewOsasuoritusView?: React.FC<P>
  addNewOsasuoritusViewProps?: P
}>

export type OsasuoritusRowData<DATA_KEYS extends string> = {
  suoritusIndex: number
  osasuoritusIndex: number
  osasuoritusPath?: FormOptic<Opiskeluoikeus, any>
  expandable: boolean
  columns: Partial<Record<DATA_KEYS, React.ReactNode>>
  content?: React.ReactElement
}

export const OsasuoritusTable = <DATA_KEYS extends string, P>(
  props: OsasuoritusTableProps<DATA_KEYS, P>
) => {
  const {
    editMode,
    level,
    onRemove,
    setOsasuoritusOpen,
    completed,
    rows,
    openState
  } = props

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

  const { addNewOsasuoritusView: AddNewOsasuoritusView } = props

  return (
    <>
      {rows[0] && <OsasuoritusHeader row={rows[0]} editMode={editMode} />}
      {rows.map((row, index) => {
        const k = `level_${level}_suoritus_${row.suoritusIndex}_osasuoritus_${row.osasuoritusIndex}`
        return (
          <OsasuoritusRow
            key={index}
            editMode={editMode}
            row={row}
            isExpanded={openState[k] === undefined ? false : openState[k]}
            expandedState={openState}
            expandable={row.expandable}
            completed={completed ? completed(index) : undefined}
            onClickExpand={() => {
              setOsasuoritusOpen(
                k,
                openState[k] === undefined ? true : !openState[k]
              )
            }}
            onRemove={onRemoveCb(index)}
            testId={subTestId(props, `osasuoritus.${row.osasuoritusIndex}`)}
          />
        )
      })}
      <Spacer />
      {editMode && AddNewOsasuoritusView !== undefined && (
        // @ts-expect-error React.JSX.IntristicAttributes virhe
        <AddNewOsasuoritusView {...(props.addNewOsasuoritusViewProps || {})} />
      )}
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
  isExpanded: boolean
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

  const expandable = props.expandable === undefined ? true : props.expandable
  const expanded = props.isExpanded

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
              onChange={() => {
                props.onClickExpand()
              }}
              label={t('Osasuoritus')}
              {...testId(props, 'expand')}
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
            {React.cloneElement(props.row.content, {
              testId: subTestId(props, 'properties')
            })}
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
