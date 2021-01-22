import bem from "bem-ts"
import { eqString } from "fp-ts/lib/Eq"
import { flip, pipe } from "fp-ts/lib/function"
import * as O from "fp-ts/lib/Option"
import * as A from "fp-ts/lib/Array"
import * as Ord from "fp-ts/lib/Ord"
import React, { useMemo, useState } from "react"
// import { update } from "../../utils/arrays"
import { toFilterableString } from "../../utils/conversions"
import { ArrowDropDownIcon, ArrowDropUpIcon } from "../icons/Icon"
import {
  DataFilter,
  dataFilterUsesValueList,
  DataTableFilter,
  FilterFn,
} from "./DataTableFilter"
import { Data, HeaderCell, Row, Table, TableBody, TableHeader } from "./Table"
import "./Table.less"

const b = bem("table")

export type DataTableProps = {
  className?: string
  columns: Column[]
  data: Datum[]
}

export type Column = {
  label: React.ReactNode
  filter?: DataFilter
}

export type Datum = {
  key: string
  values: Value[]
}

export type Value = {
  value: string | number
  display?: React.ReactNode
  icon?: React.ReactNode
}

export const DataTable = (props: DataTableProps) => {
  const [sortColumnIndex, setSortColumnIndex] = useState(0)
  const [sortAscending, setSortAscending] = useState(true)
  const [filters, setFilters] = useState<Array<FilterFn | null>>(
    new Array(props.columns.length).fill(null)
  )

  const filteredData = useMemo(
    () =>
      props.data.filter((datum) =>
        filters.every(
          (filter, index) =>
            !filter || filter(toFilterableString(datum.values[index]?.value))
        )
      ),
    [filters, props.data]
  )

  const sortedData = useMemo(() => {
    const compare = compareDatum(sortColumnIndex)
    const ordDatum = Ord.fromCompare(sortAscending ? compare : flip(compare))
    return A.sortBy([ordDatum])(filteredData)
  }, [sortColumnIndex, sortAscending, filteredData])

  const optionsForFilters = useMemo(
    () =>
      props.columns.map((col, index) =>
        dataFilterUsesValueList(col.filter)
          ? pipe(
              props.data,
              A.map(selectValue(index)),
              A.map(toFilterableString),
              A.uniq(eqString),
              A.sortBy([Ord.ordString])
            )
          : []
      ),
    [props.data]
  )

  const sortByColumn = (index: number) => {
    if (index === sortColumnIndex) {
      setSortAscending(!sortAscending)
    } else {
      setSortColumnIndex(index)
      setSortAscending(true)
    }
  }

  return (
    <Table className={props.className}>
      <TableHeader>
        <Row>
          {props.columns.map((col, index) => (
            <HeaderCell key={index}>
              <div className={b("label")} onClick={() => sortByColumn(index)}>
                {col.label}
                <SortIndicator
                  visible={sortColumnIndex === index}
                  ascending={sortAscending}
                />
              </div>
              {col.filter && (
                <div className={b("filter")}>
                  <DataTableFilter
                    type={col.filter}
                    values={optionsForFilters[index] || []}
                    onChange={(filter) =>
                      pipe(
                        filters,
                        A.updateAt(index, filter),
                        O.map(setFilters)
                      )
                    }
                  />
                </div>
              )}
            </HeaderCell>
          ))}
        </Row>
      </TableHeader>
      <TableBody>
        {sortedData.map((datum) => (
          <Row key={datum.key}>
            {datum.values.map((value, index) => (
              <Data key={index} icon={value.icon}>
                {value.display || value.value}
              </Data>
            ))}
          </Row>
        ))}
      </TableBody>
    </Table>
  )
}

type SortIndicatorProps = {
  visible: boolean
  ascending: boolean
}

const SortIndicator = ({ visible, ascending }: SortIndicatorProps) => (
  <span className={b("sortindicator", { visible })}>
    {visible &&
      (ascending ? <ArrowDropDownIcon inline /> : <ArrowDropUpIcon inline />)}
  </span>
)

const compareDatum = (index: number) => (a: Datum, b: Datum) => {
  const valueOf = selectValue(index)
  const av = valueOf(a)
  const bv = valueOf(b)
  if (typeof av === "string" && typeof bv === "string") {
    return Ord.ordString.compare(av, bv)
  } else if (typeof av === "number" && typeof bv === "number") {
    return Ord.ordNumber.compare(av, bv)
  }
  return 0
}

const selectValue = (index: number) => (datum: Datum) =>
  datum.values[index]?.value
