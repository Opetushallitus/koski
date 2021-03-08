import bem from "bem-ts"
import React from "react"
import "./InfoTable.less"

const b = bem("infotable")

export type InfoTableProps = {
  children: React.ReactNode
}

export const InfoTable = (props: InfoTableProps) => (
  <table className={b()}>
    <tbody>{props.children}</tbody>
  </table>
)

export type InfoTableRow = {
  label: React.ReactNode
  value: React.ReactNode
}

export const InfoTableRow = (props: InfoTableRow) => (
  <tr className={b("row")}>
    <th className={b("label")}>{props.label}:</th>
    <td className={b("value")}>{props.value}</td>
  </tr>
)
