import React, { useState } from "react"
import { update } from "../../utils/arrays"
import { Checkbox } from "../forms/Checkbox"
import {
  Column,
  DataTable,
  DataTableProps as DataTableProps,
} from "./DataTable"

export type SelectableDataTableProps = DataTableProps & {
  onSelect: (selectedKeys: string[]) => void
}

export const SelectableDataTable = ({
  data,
  columns,
  onSelect,
  ...rest
}: SelectableDataTableProps) => {
  const [selectedKeys, setSelectedKeys] = useState<string[]>([])

  const dataWithCheckboxes = data.map((datum) => ({
    ...datum,
    values: datum.values[0]
      ? update(datum.values, 0, {
          ...datum.values[0],
          icon: (
            <Checkbox
              value={selectedKeys.includes(datum.key)}
              onChange={(selected) => {
                const newKeys = selected
                  ? [...selectedKeys, datum.key]
                  : selectedKeys.filter((key) => key !== datum.key)
                setSelectedKeys(newKeys)
                onSelect(newKeys)
              }}
            />
          ),
        })
      : datum.values,
  }))

  const forcedColumns: Column[] = [
    {
      ...columns[0]!!,
      indicatorSpace: true,
    },
    ...columns.slice(1),
  ]

  return (
    <DataTable data={dataWithCheckboxes} columns={forcedColumns} {...rest} />
  )
}
