import bem from "bem-ts"
import React from "react"
import { joinClassNames } from "../../utils/classnames"
import { DataTableProps } from "./DataTable"
import "./LeanTable.less"

const b = bem("leantable")

export const LeanTable = (props: DataTableProps) => (
  <table className={joinClassNames(b(), props.className)}>
    <thead>
      <tr className={b("headerrow")}>
        {props.columns.map((col, index) => (
          <th key={index} className={b("th", [col.size])}>
            <div className={b("label")}>{col.label}</div>
          </th>
        ))}
      </tr>
    </thead>
    <tbody>
      {props.data.map((datum) => (
        <tr key={datum.key} className={b("datarow")}>
          {datum.values.map((value, index) => (
            <td key={index} className={b("td")}>
              {value.display || value.value || (
                <span className={b("null")}>–</span>
              )}
            </td>
          ))}
        </tr>
      ))}
    </tbody>
  </table>
)
