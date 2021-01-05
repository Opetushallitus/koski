import bem from "bem-ts"
import { sort, uniq, update, __ } from "ramda"
import React, { useMemo, useState } from "react"
import { toFilterableString } from "../../utils/conversions"
import { ascending, compareStrings, descending } from "../../utils/sort"
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

export type Props = {
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
  display?: React.ReactNode
  value: string | number
}

export const DataTable = (props: Props) => {
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
    const direction = sortAscending ? ascending : descending
    const compare = direction(compareStrings)
    return sort(
      (a, b) =>
        compare(
          a.values[sortColumnIndex]?.value,
          b.values[sortColumnIndex]?.value
        ),
      filteredData
    )
  }, [sortColumnIndex, sortAscending, filteredData])

  const optionsForFilters = useMemo(
    () =>
      props.columns.map((col, index) =>
        dataFilterUsesValueList(col.filter)
          ? uniq(
              props.data.map((datum) =>
                toFilterableString(datum.values[index]?.value)
              )
            ).sort()
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
                      setFilters(update(index, filter, filters))
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
              <Data key={index}>{value.display || value.value}</Data>
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
