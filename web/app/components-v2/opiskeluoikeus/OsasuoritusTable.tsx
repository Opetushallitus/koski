import React, { ReactNode, useCallback } from 'react'
import { useTree } from '../../appstate/tree'
import { t } from '../../i18n/i18n'
import { Opiskeluoikeus } from '../../types/fi/oph/koski/schema/Opiskeluoikeus'
import { useLayout } from '../../util/useDepth'
import { CommonProps, cx } from '../CommonProps'
import {
  COLUMN_COUNT,
  Column,
  ColumnRow,
  ResponsiveValue,
  ResponsiveValueObj,
  ResponsiveValueTarget,
  isResponsiveValueObj,
  mapResponsiveValue
} from '../containers/Columns'
import { Section } from '../containers/Section'
import { ExpandButton } from '../controls/ExpandButton'
import { IconButton } from '../controls/IconButton'
import { FormModel, FormOptic } from '../forms/FormModel'
import { Spacer } from '../layout/Spacer'
import { CHARCODE_REMOVE } from '../texts/Icon'
import { useNewItems } from '../../appstate/newItems'
import { TestIdLayer } from '../../appstate/useTestId'

export const OSASUORITUSTABLE_DEPTH_KEY = 'OsasuoritusTable'

type Completed = (osasuoritusIndex: number) => boolean | undefined

export type OsasuoritusTableProps<
  DATA_KEYS extends string,
  P = object
> = CommonProps<{
  editMode: FormModel<object>['editMode']
  columns?: Array<OsasuoritusTableColumn<DATA_KEYS>>
  rows: Array<OsasuoritusRowData<DATA_KEYS>>
  completed?: Completed
  onRemove?: (index: number) => void
  addNewOsasuoritusView?: React.FC<P>
  addNewOsasuoritusViewProps?: P
  forceOpen?: boolean
  expandedContentIndent?: number
}>

export type OsasuoritusTableColumn<DATA_KEYS extends string> = {
  key: DATA_KEYS
  label?: string
  span?: ResponsiveValue<number>
  align?: ResponsiveValue<'left' | 'center' | 'right'>
}

export type OsasuoritusRowData<DATA_KEYS extends string> = {
  suoritusIndex: number
  osasuoritusIndex: number
  osasuoritusPath?: FormOptic<Opiskeluoikeus, any>
  expandable: boolean
  columns: Partial<Record<DATA_KEYS, React.ReactNode>>
  alwaysVisibleContent?: React.ReactElement
  content?: React.ReactElement
}

const getRowId = (row: OsasuoritusRowData<string>) =>
  `${row.suoritusIndex}_${row.osasuoritusIndex}`

export const OsasuoritusTable = <DATA_KEYS extends string, P>(
  props: OsasuoritusTableProps<DATA_KEYS, P>
) => {
  const { editMode, onRemove, completed, rows } = props

  const { addNewOsasuoritusView: AddNewOsasuoritusView } = props
  const newOsasuoritusIds = useNewItems(getRowId, props.rows)

  // Laajennussarake (+-painike) varataan aina, vaikka mikään rivi ei olisi
  // laajennettavissa, jotta otsikkorivi pysyy kohdistettuna rivien kanssa.
  const skipExpandableColumn = false
  // Completed-sarake (suoritettu/kesken -merkki) varataan vain, jos taulukko
  // käyttää completed-tilaa. Esim. perusopetus ei, joten sarake jätetään pois.
  const showCompleted = completed !== undefined
  const tableColumns = rows[0] ? getTableColumns(rows[0], props.columns) : []

  return (
    <>
      {rows[0] && (
        <OsasuoritusHeader
          columns={tableColumns}
          canRemove={editMode && onRemove !== undefined}
          skipExpandableColumn={skipExpandableColumn}
          showCompleted={showCompleted}
        />
      )}
      <TestIdLayer id="osasuoritukset">
        {rows.map((row, index) => (
          <TestIdLayer
            key={row.osasuoritusIndex ? row.osasuoritusIndex : index}
            id={row.osasuoritusIndex ? row.osasuoritusIndex : index}
          >
            <OsasuoritusRow
              editMode={editMode}
              row={row}
              columns={tableColumns}
              initiallyOpen={newOsasuoritusIds.includes(getRowId(row))}
              forceOpen={props.forceOpen}
              expandable={row.expandable}
              skipExpandableColumn={skipExpandableColumn}
              expandedContentIndent={props.expandedContentIndent}
              showCompleted={showCompleted}
              completed={completed ? completed(index) : undefined}
              onRemove={
                onRemove !== undefined ? () => onRemove(index) : undefined
              }
            />
          </TestIdLayer>
        ))}
      </TestIdLayer>
      {editMode && <Spacer />}
      {editMode && AddNewOsasuoritusView && (
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
  showCompleted?: boolean
  expandable?: boolean
  skipExpandableColumn?: boolean
  row: OsasuoritusRowData<DATA_KEYS>
  columns: Array<OsasuoritusTableColumn<DATA_KEYS>>
  onRemove?: () => void
  initiallyOpen?: boolean
  forceOpen?: boolean
  expandedContentIndent?: number
}>

type OsasuoritusHeaderProps<DATA_KEYS extends string> = CommonProps<{
  columns: Array<OsasuoritusTableColumn<DATA_KEYS>>
  canRemove?: boolean
  skipExpandableColumn?: boolean
  showCompleted?: boolean
}>

export const OsasuoritusHeader = <DATA_KEYS extends string>(
  props: OsasuoritusHeaderProps<DATA_KEYS>
) => {
  const [indentation] = useLayout(OSASUORITUSTABLE_DEPTH_KEY)
  const spans = getSpans(
    props.columns,
    indentation,
    props.canRemove,
    props.skipExpandableColumn,
    props.showCompleted
  )
  return (
    <>
      <ColumnRow className="OsasuoritusHeader">
        {spans.indent > 0 && (
          <Column span={spans.indent} className="OsasuoritusHeader__indent" />
        )}
        {/* Tyhjät sarakkeet laajennus- ja completed-painikkeille, jotta
            ensimmäisen sarakkeen otsikko on rivien nimien yläpuolella. */}
        {!props.skipExpandableColumn && <Column span={spans.leftIcons} />}
        {spans.completed > 0 && <Column span={spans.completed} />}
        {props.columns.map((column, index) => (
          <Column
            key={index}
            span={index === 0 ? spans.name : spans.data[index - 1]}
            align={column.align}
          >
            {getColumnLabel(column)}
          </Column>
        ))}
        {spans.rightIcons > 0 && <Column span={spans.rightIcons} />}
      </ColumnRow>
    </>
  )
}

export const OsasuoritusRow = <DATA_KEYS extends string>(
  props: OsasuoritusRowProps<DATA_KEYS>
) => {
  const [indentation, LayoutProvider] = useLayout(OSASUORITUSTABLE_DEPTH_KEY)
  const spans = getSpans(
    props.columns,
    indentation,
    Boolean(props.editMode && props.onRemove),
    props.skipExpandableColumn,
    props.showCompleted
  )

  const expandable = props.expandable === undefined ? true : props.expandable

  const { TreeNode, ...tree } = useTree(props.initiallyOpen)
  const isOpen = props.forceOpen || tree.isOpen

  return (
    <TreeNode>
      <ColumnRow
        className={cx(
          'OsasuoritusRow',
          props.editMode && 'OsasuoritusRow--edit'
        )}
      >
        {spans.indent > 0 && (
          <Column span={spans.indent} className="OsasuoritusHeader__indent" />
        )}
        {!props.skipExpandableColumn && (
          <Column
            span={spans.leftIcons}
            align="right"
            className="OsasuoritusRow__expandColumn"
          >
            {props.row.content && expandable && (
              <ExpandButton
                expanded={isOpen}
                onChange={tree.toggle}
                label={t('Osasuoritus')}
              />
            )}
          </Column>
        )}
        {props.showCompleted && (
          <Column span={1} className="OsasuoritusRow__completedColumn">
            {props.completed === true && (
              // eslint-disable-next-line react/jsx-no-literals
              <span aria-label={t('Suoritus valmis')}>&#x2713;</span>
            )}
            {props.completed === false && (
              // eslint-disable-next-line react/jsx-no-literals
              <span aria-label={t('Suoritus kesken')}>&#x29D6;</span>
            )}
          </Column>
        )}
        {props.columns.map((column, index) => (
          <Column
            key={index}
            span={index === 0 ? spans.name : spans.data[index - 1]}
            align={column.align}
            className={
              index === 0
                ? 'OsasuoritusRow__nameColumn'
                : 'OsasuoritusRow__dataColumn'
            }
          >
            <div className="OsasuoritusRow__cellContent">
              {props.row.columns[column.key]}
            </div>
          </Column>
        ))}
        {props.editMode && props.onRemove && (
          <Column
            span={spans.rightIcons}
            className="OsasuoritusRow__removeColumn"
          >
            <IconButton
              charCode={CHARCODE_REMOVE}
              label={t('Poista')}
              size="input"
              onClick={props.onRemove}
              testId="delete"
            />
          </Column>
        )}
      </ColumnRow>
      {props.row.alwaysVisibleContent && (
        <LayoutProvider indent={3}>
          {props.row.alwaysVisibleContent}
        </LayoutProvider>
      )}
      {expandable && isOpen && props.row.content && (
        <LayoutProvider indent={props.expandedContentIndent ?? 2}>
          <TestIdLayer id="properties">
            <Section>{props.row.content}</Section>
          </TestIdLayer>
        </LayoutProvider>
      )}
    </TreeNode>
  )
}

const getSpans = (
  columns: Array<OsasuoritusTableColumn<string>>,
  depth?: number,
  canRemove?: boolean,
  skipExpandableColumn?: boolean,
  showCompleted?: boolean
) => {
  const indent = depth || 0
  const leftIcons = skipExpandableColumn ? 0 : 1
  const completed = showCompleted ? 1 : 0
  const rightIcons = canRemove ? 1 : 0
  const availableColumns =
    COLUMN_COUNT - indent - leftIcons - completed - rightIcons
  const dataColumnCount = Math.max(0, columns.length - 1)
  const dataSpan: ResponsiveValue<number> = {
    default: 4,
    phone: fitDataColumnSpan(8, availableColumns, dataColumnCount),
    small: fitDataColumnSpan(6, availableColumns, dataColumnCount)
  }
  const data = columns.slice(1).map((column) => column.span ?? dataSpan)
  const dataWidth = sumResponsiveValues(data)
  const name = mapResponsiveValue((w: number) => availableColumns - w)(
    dataWidth
  )

  return {
    indent,
    leftIcons,
    completed,
    rightIcons,
    data,
    name
  }
}

const MIN_NARROW_NAME_SPAN = 8

const RESPONSIVE_VALUE_TARGETS: ResponsiveValueTarget[] = [
  'default',
  'phone',
  'small',
  'large'
]

const valueAtTarget = (
  value: ResponsiveValue<number>,
  target: ResponsiveValueTarget
): number =>
  isResponsiveValueObj(value) ? (value[target] ?? value.default) : value

const sumResponsiveValues = (
  values: Array<ResponsiveValue<number>>
): ResponsiveValue<number> =>
  RESPONSIVE_VALUE_TARGETS.reduce<ResponsiveValueObj<number>>((sum, target) => {
    const value = values.reduce<number>(
      (total, current) => total + valueAtTarget(current, target),
      0
    )

    return target === 'default' ||
      values.some(
        (current) =>
          isResponsiveValueObj(current) && current[target] !== undefined
      )
      ? {
          ...sum,
          [target]: value
        }
      : sum
  }, {} as ResponsiveValueObj<number>)

const fitDataColumnSpan = (
  preferredSpan: number,
  availableColumns: number,
  dataColumnCount: number
): number => {
  if (dataColumnCount === 0) {
    return preferredSpan
  }

  const maxSpan = Math.floor(
    (availableColumns - MIN_NARROW_NAME_SPAN) / dataColumnCount
  )

  return Math.max(1, Math.min(preferredSpan, maxSpan))
}

const getTableColumns = <DATA_KEYS extends string>(
  row: OsasuoritusRowData<DATA_KEYS>,
  columns?: Array<OsasuoritusTableColumn<DATA_KEYS>>
): Array<OsasuoritusTableColumn<DATA_KEYS>> =>
  columns ||
  Object.keys(row.columns).map((key) => ({
    key: key as DATA_KEYS
  }))

const getColumnLabel = <DATA_KEYS extends string>(
  column: OsasuoritusTableColumn<DATA_KEYS>
) =>
  column.label === undefined
    ? t(column.key)
    : column.label
      ? t(column.label)
      : null

export const osasuoritusTestId = (
  suoritusIndex: number,
  osasuoritusIndex: number,
  subItem?: string
): string =>
  `suoritukset.${suoritusIndex}.osasuoritukset.${osasuoritusIndex}` +
  (subItem ? `.${subItem}` : '')
