import React, { useState } from "react"
import { update } from "../../utils/arrays"
import { Checkbox } from "../forms/Checkbox"
import { DataTable, DataTableProps as DataTableProps } from "./DataTable"

export type SelectableDataTableProps = DataTableProps & {
  onChange: (selectedKeys: string[]) => void
}

export const SelectableDataTable = ({
  data,
  onChange,
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
                onChange(newKeys)
              }}
            />
          ),
        })
      : datum.values,
  }))
  return <DataTable data={dataWithCheckboxes} {...rest} />
}
