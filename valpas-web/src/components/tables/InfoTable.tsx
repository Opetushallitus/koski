import bem from "bem-ts"
import React from "react"
import { makeAutoWrappable } from "../../i18n/i18n"
import "./InfoTable.less"

const b = bem("infotable")

export type InfoTableProps = {
  children: React.ReactNode
  size?: InfoTableSize
}

export type InfoTableSize = "normal" | "tighter"

export const InfoTable = (props: InfoTableProps) => (
  <section className={b([props.size])}>
    <ul className={b("body")}>{props.children}</ul>
  </section>
)

export type InfoTableRow = {
  label?: React.ReactNode
  value: React.ReactNode
  testId?: string
}

export const InfoTableRow = (props: InfoTableRow) => (
  <li className={b("row")}>
    {props.label ? (
      <>
        <span className={b("label")}>
          {typeof props.label === "string"
            ? makeAutoWrappable(props.label)
            : props.label}
          :
        </span>
        <span className={b("value")} data-testid={props.testId}>
          {props.value}
        </span>
      </>
    ) : (
      <span
        className={b("value", { nolabel: true })}
        data-testid={props.testId}
      >
        {props.value}
      </span>
    )}
  </li>
)
