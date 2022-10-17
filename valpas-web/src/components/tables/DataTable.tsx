import bem from "bem-ts"
import * as A from "fp-ts/lib/Array"
import { flip, pipe } from "fp-ts/lib/function"
import * as number from "fp-ts/lib/number"
import * as O from "fp-ts/lib/Option"
import * as Ord from "fp-ts/lib/Ord"
import * as string from "fp-ts/lib/string"
import * as NEA from "fp-ts/NonEmptyArray"
import React, {
  CSSProperties,
  useCallback,
  useEffect,
  useMemo,
  useState,
} from "react"
import { T } from "../../i18n/i18n"
// import { update } from "../../utils/arrays"
import { FilterableValue, toFilterableString } from "../../utils/conversions"
import { RaisedButton } from "../buttons/RaisedButton"
import { ArrowDropDownIcon, ArrowDropUpIcon } from "../icons/Icon"
import { InfoTooltip } from "../tooltip/InfoTooltip"
import { dataFilterUsesValueList, DataTableFilter } from "./DataTableFilter"
import { Data, HeaderCell, Row, Table, TableBody, TableHeader } from "./Table"
import "./Table.less"
import {
  Column,
  DataTableState,
  setFilters,
  sortByColumn,
  useDataTableState,
} from "./useDataTableState"

const b = bem("table")

export type DataTableProps = {
  className?: string
  columns: Column[]
  data: Datum[]
  storageName?: string
  onCountChange?: (event: DataTableCountChangeEvent) => void
}

export { Column } from "./useDataTableState"

export type DataTableCountChangeEvent = {
  filteredRowCount: number
  unfilteredRowCount: number
}

/** Represents a piece of table data - a row */
export type DatumKey = NEA.NonEmptyArray<string>
export type Datum = {
  /** Unique identifier */
  key: DatumKey
  /** List of values, length and order equals columns */
  values: Value[]
}

export const keyToString = (key: DatumKey): string => key.join(".")

export const equalKeys = (a: DatumKey) => (b: DatumKey) =>
  A.getEq(string.Eq).equals(a, b)

export type Value = {
  /** Value for filtering and ordering */
  value: FilterableValue
  /** Value for rendering */
  display?: string | number | React.ReactNode
  /** Icon alongside the value */
  icon?: React.ReactNode
  filterValues?: Array<string | number>
  tooltip?: string
}

export const fromNullableValue = (
  value: Value | null | undefined,
  nullFilterValues?: Array<string | number>
): Value =>
  value || {
    value: "–",
    filterValues: nullFilterValues,
  }

export const fromNullable = (
  value: FilterableValue | null | undefined
): Value =>
  fromNullableValue({
    value: value ? value : "-",
  })

export const DataTable = (props: DataTableProps) => {
  const { columns, sortedData, ...headerProps } = useTableData(props)

  return (
    <Table className={props.className}>
      <DataTableHeader columns={columns} {...headerProps} />
      <TableBody>
        {sortedData.map((datum) => (
          <DataRow
            key={keyToString(datum.key)}
            datum={datum}
            columns={columns}
          />
        ))}
      </TableBody>
    </Table>
  )
}

export type PaginatedDataTableProps = DataTableProps & {
  paginationSize: number
}

export const PaginatedDataTable = (props: PaginatedDataTableProps) => {
  const { columns, sortedData, ...headerProps } = useTableData(props)

  const [visibleCount, setVisibleCount] = useState(props.paginationSize)
  const showMore = useCallback(
    () => setVisibleCount(visibleCount + props.paginationSize),
    [visibleCount, props.paginationSize]
  )
  useEffect(
    () => setVisibleCount(props.paginationSize),
    [sortedData, props.paginationSize]
  )

  const shownData = useMemo(
    () => sortedData.slice(0, visibleCount),
    [sortedData, visibleCount]
  )

  return (
    <Table className={props.className}>
      <DataTableHeader columns={columns} {...headerProps} />
      <TableBody>
        {shownData.map((datum) => (
          <DataRow
            key={keyToString(datum.key)}
            datum={datum}
            columns={columns}
          />
        ))}
        {shownData.length < sortedData.length ? (
          <Row>
            <td className={b("showmore")} colSpan={columns.length}>
              <RaisedButton onClick={showMore}>
                <T id="btn_näytä_lisää" />
              </RaisedButton>
            </td>
          </Row>
        ) : null}
      </TableBody>
    </Table>
  )
}

type DataTableHeaderProps = {
  columns: Column[]
  tableState: DataTableState
  setTableState: React.Dispatch<React.SetStateAction<DataTableState>>
  containsFilters: boolean
  optionsForFilters: string[][]
}

const DataTableHeader = (props: DataTableHeaderProps) => (
  <TableHeader>
    {/* Label row */}
    <Row>
      {props.columns.map((col, index) => (
        <HeaderCell
          key={index}
          size={col.size}
          indicatorSpace={!!col.indicatorSpace}
          onClick={() => props.setTableState(sortByColumn(index))}
          className={b("label")}
        >
          <div className={b("labelcontent")}>
            <div className={b("labeltext")} title={col.label}>
              {col.label}
            </div>
            <SortIndicator
              visible={props.tableState.sort.columnIndex === index}
              ascending={props.tableState.sort.ascending}
            />
            {col.tooltip && <InfoTooltip content={col.tooltip} />}
          </div>
        </HeaderCell>
      ))}
    </Row>

    {/* Filter row */}
    {props.containsFilters && (
      <Row>
        {props.columns.map((col, index) => (
          <HeaderCell
            key={index}
            indicatorSpace={!!col.indicatorSpace}
            className={b("filter")}
          >
            {col.filter && (
              <DataTableFilter
                type={col.filter}
                values={props.optionsForFilters[index] || []}
                initialValue={props.tableState.filters[index]?.value}
                onChange={(filter, value) =>
                  pipe(
                    props.tableState.filters,
                    A.updateAt(index, { fn: filter, value }),
                    O.map((filters) => props.setTableState(setFilters(filters)))
                  )
                }
              />
            )}
          </HeaderCell>
        ))}
      </Row>
    )}
  </TableHeader>
)

type DataRowProps = {
  datum?: Datum
  columns: Column[]
  style?: CSSProperties
}

const DataRow = ({ datum, columns, style }: DataRowProps) =>
  datum ? (
    <Row data-row={datum.key} style={style}>
      {datum.values.map((value, index) => {
        const column = columns[index]
        return (
          <Data
            key={index}
            icon={value.icon}
            size={column?.size}
            indicatorSpace={!!column?.indicatorSpace}
            title={value.tooltip || value.value?.toString()}
          >
            {value.display || value.value}
          </Data>
        )
      })}
    </Row>
  ) : null

const useTableData = (props: DataTableProps) => {
  const [tableState, setTableState] = useDataTableState(
    props.storageName,
    props.columns
  )

  const filteredData = useMemo(
    () =>
      props.data.filter((datum) =>
        tableState.filters.every(
          (filter, index) =>
            !filter?.fn ||
            (datum.values[index]?.filterValues
              ? datum.values[index]?.filterValues?.some((fv) =>
                  filter.fn?.(toFilterableString(fv))
                )
              : filter.fn?.(toFilterableString(datum.values[index]?.value)))
        )
      ),
    [tableState.filters, props.data]
  )

  const sortedData = useMemo(() => {
    const compare = compareDatum(tableState.sort.columnIndex)
    const ordDatum = Ord.fromCompare(
      tableState.sort.ascending ? compare : flip(compare)
    )
    return A.sortBy([ordDatum])(filteredData)
  }, [tableState.sort.columnIndex, tableState.sort.ascending, filteredData])

  useEmitCountChanges(props.data, sortedData, props.onCountChange)

  const optionsForFilters = useOptionsForFilters(props.columns, props.data)

  const containsFilters = props.columns.some((col) => col.filter)

  const columns = props.columns.map((column, colIndex) =>
    column.indicatorSpace === "auto"
      ? {
          ...column,
          indicatorSpace: filteredData.some(
            (row) => row.values[colIndex]?.icon
          ),
        }
      : column
  )

  return useMemo(
    () => ({
      columns,
      sortedData,
      tableState,
      setTableState,
      optionsForFilters,
      containsFilters,
    }),
    [
      columns,
      containsFilters,
      optionsForFilters,
      setTableState,
      sortedData,
      tableState,
    ]
  )
}

const useOptionsForFilters = (columns: Column[], data: Datum[]) => {
  return useMemo(
    () =>
      columns.map((col, index) =>
        dataFilterUsesValueList(col.filter)
          ? pipe(
              data,
              A.chain(selectFilterValues(index)),
              A.map(toFilterableString),
              A.uniq(string.Eq),
              A.sortBy([string.Ord])
            )
          : []
      ),
    [columns, data]
  )
}

const useEmitCountChanges = (
  data: Datum[],
  sortedData: Datum[],
  onChange?: (event: DataTableCountChangeEvent) => void
) => {
  const filteredRowCount = sortedData.length
  const unfilteredRowCount = data.length

  useEffect(() => {
    onChange?.({
      filteredRowCount,
      unfilteredRowCount,
    })
  }, [filteredRowCount, unfilteredRowCount, onChange])
}

type SortIndicatorProps = {
  visible: boolean
  ascending: boolean
}

const SortIndicator = ({ visible, ascending }: SortIndicatorProps) => (
  <div className={b("sortindicator", { visible })}>
    {visible &&
      (ascending ? <ArrowDropDownIcon inline /> : <ArrowDropUpIcon inline />)}
  </div>
)

const compareDatum = (index: number) => (a: Datum, b: Datum) => {
  const valueOf = selectValue(index)
  const av = valueOf(a)
  const bv = valueOf(b)
  if (typeof av === "string" && typeof bv === "string") {
    return string.Ord.compare(av, bv)
  } else if (typeof av === "number" && typeof bv === "number") {
    return number.Ord.compare(av, bv)
  } else if (av == null && bv != null) {
    return 1
  } else if (bv == null && av != null) {
    return -1
  } else {
    return 0
  }
}

const selectValue = (index: number) => (datum: Datum) =>
  datum.values[index]?.value

const selectFilterValues = (index: number) => (datum: Datum) => {
  const item = datum.values[index]
  return item?.filterValues || (item?.value ? [item.value] : [])
}
