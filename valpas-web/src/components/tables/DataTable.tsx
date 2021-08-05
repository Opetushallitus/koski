import bem from "bem-ts"
import * as A from "fp-ts/lib/Array"
import { flip, pipe } from "fp-ts/lib/function"
import * as number from "fp-ts/lib/number"
import * as O from "fp-ts/lib/Option"
import * as Ord from "fp-ts/lib/Ord"
import * as string from "fp-ts/lib/string"
import React, { useEffect, useMemo } from "react"
// import { update } from "../../utils/arrays"
import { FilterableValue, toFilterableString } from "../../utils/conversions"
import { ArrowDropDownIcon, ArrowDropUpIcon } from "../icons/Icon"
import { InfoTooltip } from "../tooltip/InfoTooltip"
import { dataFilterUsesValueList, DataTableFilter } from "./DataTableFilter"
import { Data, HeaderCell, Row, Table, TableBody, TableHeader } from "./Table"
import "./Table.less"
import {
  Column,
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
  value: FilterableValue
  /** Value for rendering */
  display?: string | number | React.ReactNode
  /** Icon alongside the value */
  icon?: React.ReactNode
  filterValues?: Array<string | number>
  tooltip?: string
}

export const DataTable = (props: DataTableProps) => {
  const [state, setState] = useDataTableState(props.storageName, props.columns)

  // TODO: Datan muljuttamiset voisi myöhemmin siirtää omaan hookkiinsa

  const filteredData = useMemo(
    () =>
      props.data.filter((datum) =>
        state.filters.every(
          (filter, index) =>
            !filter?.fn ||
            (datum.values[index]?.filterValues
              ? datum.values[index]?.filterValues?.some((fv) =>
                  filter.fn?.(toFilterableString(fv))
                )
              : filter.fn?.(toFilterableString(datum.values[index]?.value)))
        )
      ),
    [state.filters, props.data]
  )

  const sortedData = useMemo(() => {
    const compare = compareDatum(state.sort.columnIndex)
    const ordDatum = Ord.fromCompare(
      state.sort.ascending ? compare : flip(compare)
    )
    return A.sortBy([ordDatum])(filteredData)
  }, [state.sort.columnIndex, state.sort.ascending, filteredData])

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
              onClick={() => setState(sortByColumn(index))}
              className={b("label")}
            >
              <div className={b("labelcontent")}>
                <div className={b("labeltext")} title={col.label}>
                  {col.label}
                </div>
                <SortIndicator
                  visible={state.sort.columnIndex === index}
                  ascending={state.sort.ascending}
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
                    initialValue={state.filters[index]?.value}
                    onChange={(filter, value) =>
                      pipe(
                        state.filters,
                        A.updateAt(index, { fn: filter, value }),
                        O.map((filters) => setState(setFilters(filters)))
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
