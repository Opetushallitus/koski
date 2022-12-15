import React, { useState } from "react"
import { update } from "../../utils/arrays"
import { Checkbox } from "../forms/Checkbox"
import {
  Column,
  DataTable,
  DataTableProps,
  DatumKey,
  equalKeys,
} from "./DataTable"

export type SelectableDataTableProps = DataTableProps & {
  /**
   * Valintojen muuttuessa palauttaa taulukon valittujen rivien avaimista.
   */
  onSelect: (selectedKeys: DatumKey[]) => void

  /**
   * Oletuksena SelectableDataTable käsittelee jokaista rivin valintaa uniikkina valintana.
   * Antamalla oman löysemmän vertailuehdon saadaan valittua myös rivin vertaisrivit
   * (esim. hakutilannetaulusta valitaan kaikki muutkin rivit, joilla on sama oppija oid, kuin valitulla rivillä).
   *
   * Tämä vaikuttaa ainoastaan rivien automaattiseen valintaan; onSelectin palauttamia avaimia ei siis aggregoida.
   */
  peerEquality?: (a: DatumKey) => (b: DatumKey) => boolean
}

export const SelectableDataTable = ({
  data,
  columns,
  onSelect,
  peerEquality: equalForSelection,
  ...rest
}: SelectableDataTableProps) => {
  const [selectedKeys, setSelectedKeys] = useState<DatumKey[]>([])
  const equals = equalForSelection || equalKeys

  const dataWithCheckboxes = data.map((datum) => ({
    ...datum,
    values: datum.values[0]
      ? update(datum.values, 0, {
          ...datum.values[0],
          icon: (
            <Checkbox
              value={selectedKeys.some(equals(datum.key))}
              onChange={(selected) => {
                const newKeys: DatumKey[] = selected
                  ? [
                      ...selectedKeys,
                      ...data.map((d) => d.key).filter(equals(datum.key)),
                    ]
                  : selectedKeys.filter((key) => !equals(key)(datum.key))
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
