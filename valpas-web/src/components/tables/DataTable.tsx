import bem from "bem-ts"
import * as A from "fp-ts/lib/Array"
import { flip, pipe } from "fp-ts/lib/function"
import * as number from "fp-ts/lib/number"
import * as O from "fp-ts/lib/Option"
import * as Ord from "fp-ts/lib/Ord"
import * as string from "fp-ts/lib/string"
import React, { useEffect, useMemo, useState } from "react"
import {
  nonStoredState,
  sessionStateStorage,
  useStoredState,
} from "../../state/useSessionStoreState"
// import { update } from "../../utils/arrays"
import { toFilterableString } from "../../utils/conversions"
import { ArrowDropDownIcon, ArrowDropUpIcon } from "../icons/Icon"
import { InfoTooltip } from "../tooltip/InfoTooltip"
import {
  createFilter,
  DataFilter,
  dataFilterUsesValueList,
  DataTableFilter,
  FilterFn,
} from "./DataTableFilter"
import {
  Data,
  HeaderCell,
  Row,
  Table,
  TableBody,
  TableCellSize,
  TableHeader,
} from "./Table"
import "./Table.less"

const b = bem("table")

export type DataTableProps = {
  className?: string
  columns: Column[]
  data: Datum[]
  storageName?: string
  onCountChange?: (event: DataTableCountChangeEvent) => void
}

export type DataTableCountChangeEvent = {
  filteredRowCount: number
  unfilteredRowCount: number
}

export type Column = {
  label: string
  tooltip?: string
  filter?: DataFilter
  size?: TableCellSize
  indicatorSpace?: true | false | "auto"
}

/** Represents a piece of table data - a row */
export type DatumKey = string[]
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
  value: string | number | boolean | null | undefined
  /** Value for rendering */
  display?: string | number | React.ReactNode
  /** Icon alongside the value */
  icon?: React.ReactNode
  filterValues?: Array<string | number>
  tooltip?: string
}

type FilterArray = Array<{ fn: FilterFn | null; value: string | null }>

export const DataTable = (props: DataTableProps) => {
  const [sortColumnIndex, setSortColumnIndex] = useState(0)
  const [sortAscending, setSortAscending] = useState(true)

  const [filters, setFilters] = useStoredState<FilterArray>(
    props.storageName
      ? createFilterStorage(props.storageName, props.columns)
      : nonStoredState(initialFilters(props.columns.length))
  )

  const filteredData = useMemo(
    () =>
      // TODO: Tän sotkun vois kirjoittaa kauniimmaksi
      props.data.filter((datum) =>
        filters.every(
          (filter, index) =>
            !filter?.fn ||
            (datum.values[index]?.filterValues
              ? datum.values[index]?.filterValues?.some((fv) =>
                  filter.fn?.(toFilterableString(fv))
                )
              : filter.fn?.(toFilterableString(datum.values[index]?.value)))
        )
      ),
    [filters, props.data]
  )

  const sortedData = useMemo(() => {
    const compare = compareDatum(sortColumnIndex)
    const ordDatum = Ord.fromCompare(sortAscending ? compare : flip(compare))
    return A.sortBy([ordDatum])(filteredData)
  }, [sortColumnIndex, sortAscending, filteredData])

  useEmitCountChanges(props.data, sortedData, props.onCountChange)

  const optionsForFilters = useOptionsForFilters(props.columns, props.data)

  const sortByColumn = (index: number) => {
    if (index === sortColumnIndex) {
      setSortAscending(!sortAscending)
    } else {
      setSortColumnIndex(index)
      setSortAscending(true)
    }
  }

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

  return (
    <Table className={props.className}>
      <TableHeader>
        {/* Label row */}
        <Row>
          {columns.map((col, index) => (
            <HeaderCell
              key={index}
              size={col.size}
              indicatorSpace={!!col.indicatorSpace}
              onClick={() => sortByColumn(index)}
              className={b("label")}
            >
              <div className={b("labelcontent")}>
                <div className={b("labeltext")} title={col.label}>
                  {col.label}
                </div>
                <SortIndicator
                  visible={sortColumnIndex === index}
                  ascending={sortAscending}
                />
                {col.tooltip && <InfoTooltip>{col.tooltip}</InfoTooltip>}
              </div>
            </HeaderCell>
          ))}
        </Row>

        {/* Filter row */}
        {containsFilters && (
          <Row>
            {columns.map((col, index) => (
              <HeaderCell
                key={index}
                indicatorSpace={!!col.indicatorSpace}
                className={b("filter")}
              >
                {col.filter && (
                  <DataTableFilter
                    type={col.filter}
                    values={optionsForFilters[index] || []}
                    initialValue={filters[index]?.value}
                    onChange={(filter, value) =>
                      pipe(
                        filters,
                        A.updateAt(index, { fn: filter, value }),
                        O.map(setFilters)
                      )
                    }
                  />
                )}
              </HeaderCell>
            ))}
          </Row>
        )}
      </TableHeader>
      <TableBody>
        {sortedData.map((datum) => (
          <Row key={keyToString(datum.key)} data-row={datum.key}>
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
        ))}
      </TableBody>
    </Table>
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
  }
  return 0
}

const selectValue = (index: number) => (datum: Datum) =>
  datum.values[index]?.value

const selectFilterValues = (index: number) => (datum: Datum) => {
  const item = datum.values[index]
  return item?.filterValues || (item?.value ? [item.value] : [])
}

const createFilterStorage = (storageName: string, columns: Column[]) =>
  sessionStateStorage<FilterArray, Array<string | null>>(
    `${storageName}-${columns.map((c) => c.filter?.slice(0, 2)).join(".")}`,
    initialFilters(columns.length),
    (filts) => filts.map((f) => f.value),
    (values) =>
      values.map((value, index) => ({
        value,
        fn: createFilter(columns[index]?.filter, value),
      }))
  )

const initialFilters = (length: number): FilterArray =>
  new Array(length).fill({ value: null, fn: null })
