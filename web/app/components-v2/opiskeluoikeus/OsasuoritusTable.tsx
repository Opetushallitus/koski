import React, { createContext, ReactNode, useCallback, useContext } from 'react'
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
import { OsasuoritusProperties } from './OsasuoritusProperty'
import { ExpandButton } from '../controls/ExpandButton'
import { IconButton } from '../controls/IconButton'
import { FormModel, FormOptic } from '../forms/FormModel'
import { Spacer } from '../layout/Spacer'
import {
  CHARCODE_KESKEN,
  CHARCODE_REMOVE,
  CHARCODE_VALMIS,
  Icon
} from '../texts/Icon'
import { useNewItems } from '../../appstate/newItems'
import { TestIdLayer } from '../../appstate/useTestId'

// Ammatillinen-v2:n oma valinta: rivi avautuu myös nimestä. Taulukon asettelu
// on muuten sama kaikissa v2-käyttöliittymissä.
export const AmmatillinenTyyliContext = createContext(false)

// Taulukon sisäkkäisyystaso (0 = päätason taulukko). Sisennys on kiinteä
// pikselimitta tasoa kohden eikä ruudukon sarakkeita, jotta sisäkkäinen
// taulukko ja kentät osuvat ylemmän rivin nimen alle taulukon leveydestä
// riippumatta. Ks. OsasuoritusTable.less.
const TASO_KEY = 'OsasuoritusTableTaso'

type TasoStyle = React.CSSProperties & { '--osasuoritus-taso': number }

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
  // Ryhmän otsikko (esim. "Yhteiset oppiaineet") otsikkorivin nimisarakkeeseen,
  // samalle riville muiden sarakkeiden otsikoiden kanssa. Ilman rivejä otsikko
  // näytetään yksinään, koska sarakeotsikoille ei silloin ole sisältöä.
  title?: ReactNode
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
  const [taso] = useLayout(TASO_KEY)
  const addNewView = editMode && AddNewOsasuoritusView && (
    // @ts-expect-error React.JSX.IntristicAttributes virhe
    <AddNewOsasuoritusView {...(props.addNewOsasuoritusViewProps || {})} />
  )

  // Laajennussarake (+-painike) varataan aina, vaikka mikään rivi ei olisi
  // laajennettavissa, jotta otsikkorivi pysyy kohdistettuna rivien kanssa.
  const skipExpandableColumn = false
  // Completed-sarake (suoritettu/kesken -merkki) varataan vain, jos taulukko
  // käyttää completed-tilaa. Esim. perusopetus ei, joten sarake jätetään pois.
  const showCompleted = completed !== undefined
  const tableColumns = rows[0] ? getTableColumns(rows[0], props.columns) : []

  return (
    <>
      {rows[0] ? (
        <OsasuoritusHeader
          columns={tableColumns}
          canRemove={editMode && onRemove !== undefined}
          skipExpandableColumn={skipExpandableColumn}
          showCompleted={showCompleted}
          title={props.title}
        />
      ) : (
        props.title
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
      {taso > 0 && addNewView ? (
        <div
          className="OsasuoritusTable__lisays"
          style={{ '--osasuoritus-taso': taso } as TasoStyle}
        >
          {addNewView}
        </div>
      ) : (
        addNewView
      )}
      {/* Sisäkkäisen taulukon alaväli tulee laajennetun rivin omasta
          alareunuksesta. */}
      {taso === 0 && <Spacer />}
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
}>

type OsasuoritusHeaderProps<DATA_KEYS extends string> = CommonProps<{
  columns: Array<OsasuoritusTableColumn<DATA_KEYS>>
  canRemove?: boolean
  skipExpandableColumn?: boolean
  showCompleted?: boolean
  title?: ReactNode
}>

export const OsasuoritusHeader = <DATA_KEYS extends string>(
  props: OsasuoritusHeaderProps<DATA_KEYS>
) => {
  const [taso] = useLayout(TASO_KEY)
  const spans = getSpans(
    props.columns,
    props.canRemove,
    props.skipExpandableColumn,
    props.showCompleted
  )
  // Nimisarakkeen otsikko ("… tutkinnon osat" tai ryhmän otsikko) alkaa rivin
  // reunasta laajennus- ja completed-sarakkeiden yli kuten rivien nimisolu.
  const leadingSpan =
    (props.skipExpandableColumn ? 0 : spans.leftIcons) + spans.completed
  return (
    <ColumnRow
      className={cx(
        'OsasuoritusHeader',
        props.title !== undefined && 'OsasuoritusHeader--title',
        taso > 0 && 'OsasuoritusHeader--sisakkainen'
      )}
      style={{ '--osasuoritus-taso': taso } as TasoStyle}
    >
      {props.columns.map((column, index) => {
        const isNameColumn = index === 0
        const span = isNameColumn
          ? mapResponsiveValue((w: number) => leadingSpan + w)(spans.name)
          : spans.data[index - 1]
        const isTitle = isNameColumn && props.title !== undefined
        const label = isTitle ? props.title : getColumnLabel(column)
        return (
          <Column
            key={index}
            span={span}
            align={column.align}
            className={cx(
              isTitle && 'OsasuoritusHeader__title',
              label === null && 'OsasuoritusHeader__tyhja'
            )}
          >
            {label}
          </Column>
        )
      })}
      {spans.rightIcons > 0 && (
        <Column
          span={spans.rightIcons}
          className="OsasuoritusHeader__rightIcons"
        />
      )}
    </ColumnRow>
  )
}

export const OsasuoritusRow = <DATA_KEYS extends string>(
  props: OsasuoritusRowProps<DATA_KEYS>
) => {
  const [taso, TasoProvider] = useLayout(TASO_KEY)
  const ammatillinenTyyli = useContext(AmmatillinenTyyliContext)
  const canRemove = Boolean(props.editMode && props.onRemove)
  const spans = getSpans(
    props.columns,
    canRemove,
    props.skipExpandableColumn,
    props.showCompleted
  )

  const expandable = props.expandable === undefined ? true : props.expandable

  const { TreeNode, ...tree } = useTree(props.initiallyOpen)
  const isOpen = props.forceOpen || tree.isOpen

  const nameTogglesRow =
    ammatillinenTyyli &&
    expandable &&
    Boolean(props.row.content) &&
    !props.forceOpen

  const expandButton = props.row.content && expandable && (
    <ExpandButton
      expanded={isOpen}
      onChange={tree.toggle}
      label={t('Osasuoritus')}
    />
  )

  // Vanhan käyttöliittymän valmis- ja tiimalasi-ikonit (FontAwesome)
  const completedMark = (
    <>
      {props.completed === true && (
        <span aria-label={t('Suoritus valmis')}>
          <Icon charCode={CHARCODE_VALMIS} />
        </span>
      )}
      {props.completed === false && (
        <span aria-label={t('Suoritus kesken')}>
          <Icon charCode={CHARCODE_KESKEN} />
        </span>
      )}
    </>
  )

  const cellContent = (
    column: OsasuoritusTableColumn<DATA_KEYS>,
    index: number
  ) => {
    const clickableName = index === 0 && nameTogglesRow
    return (
      <div
        className={cx(
          'OsasuoritusRow__cellContent',
          clickableName && 'OsasuoritusRow__cellContent--clickable'
        )}
        onClick={
          clickableName
            ? (e) => {
                if (
                  !(e.target as HTMLElement).closest(
                    'input, button, a, select, textarea, label, .Select'
                  )
                ) {
                  tree.toggle()
                }
              }
            : undefined
        }
      >
        {props.row.columns[column.key]}
      </div>
    )
  }

  const removeColumn = props.editMode && props.onRemove && (
    <Column span={spans.rightIcons} className="OsasuoritusRow__removeColumn">
      <IconButton
        charCode={CHARCODE_REMOVE}
        label={t('Poista')}
        size="input"
        onClick={props.onRemove}
        testId="delete"
      />
    </Column>
  )

  return (
    <TreeNode>
      {/* Laajennuspainike, valmis-merkki ja nimi ovat yhdessä solussa, jonka
          sisennys on kiinteä mitta tasoa kohden. Datasarakkeet pysyvät
          ruudukossa, joten ne ovat linjassa kaikilla tasoilla. */}
      <ColumnRow
        className={cx(
          'OsasuoritusRow',
          isOpen && 'OsasuoritusRow--open',
          props.editMode && 'OsasuoritusRow--edit'
        )}
        style={{ '--osasuoritus-taso': taso } as TasoStyle}
      >
        <Column
          span={mapResponsiveValue(
            (w: number) => spans.leftIcons + spans.completed + w
          )(spans.name)}
          className="OsasuoritusRow__lead"
        >
          <div className="OsasuoritusRow__expandColumn">{expandButton}</div>
          <div className="OsasuoritusRow__completedColumn">{completedMark}</div>
          <div className="OsasuoritusRow__nameColumn">
            {cellContent(props.columns[0], 0)}
          </div>
        </Column>
        {props.columns.slice(1).map((column, index) => (
          <Column
            key={index}
            span={spans.data[index]}
            align={column.align}
            className="OsasuoritusRow__dataColumn"
          >
            {cellContent(column, index + 1)}
          </Column>
        ))}
        {removeColumn}
      </ColumnRow>
      {props.row.alwaysVisibleContent && (
        <OsasuoritusProperties taso={taso}>
          {props.row.alwaysVisibleContent}
        </OsasuoritusProperties>
      )}
      {expandable && isOpen && props.row.content && (
        <TasoProvider indent={1}>
          <TestIdLayer id="properties">
            <OsasuoritusProperties taso={taso}>
              {props.row.content}
            </OsasuoritusProperties>
          </TestIdLayer>
        </TasoProvider>
      )}
    </TreeNode>
  )
}

const getSpans = (
  columns: Array<OsasuoritusTableColumn<string>>,
  canRemove?: boolean,
  skipExpandableColumn?: boolean,
  showCompleted?: boolean
) => {
  const leftIcons = skipExpandableColumn ? 0 : 1
  const completed = showCompleted ? 1 : 0
  const rightIcons = canRemove ? 1 : 0
  const availableColumns = COLUMN_COUNT - leftIcons - completed - rightIcons
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
