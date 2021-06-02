import bem from "bem-ts"
import React from "react"
import "./InfoTable.less"

const b = bem("infotable")

export type InfoTableProps = {
  children: React.ReactNode
  size?: InfoTableSize
}

export type InfoTableSize = "normal" | "tighter"

export const InfoTable = (props: InfoTableProps) => (
  <table className={b([props.size])}>
    <tbody>{props.children}</tbody>
  </table>
)

export type InfoTableRow = {
  label?: React.ReactNode
  value: React.ReactNode
  testId?: string
}

export const InfoTableRow = (props: InfoTableRow) => (
  <tr className={b("row")}>
    {props.label ? (
      <>
        <th className={b("label")}>{props.label}:</th>
        <td className={b("value")} data-testid={props.testId}>
          {props.value}
        </td>
      </>
    ) : (
      <td className={b("value")} colSpan={2} data-testid={props.testId}>
        {props.value}
      </td>
    )}
  </tr>
)
