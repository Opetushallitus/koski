import bem from "bem-ts"
import React from "react"
import { makeAutoWrappable } from "../../i18n/i18n"
import "./InfoTable.less"

const b = bem("infotable")

export type InfoTableProps = React.HTMLAttributes<HTMLDivElement> & {
  children: React.ReactNode
  size?: InfoTableSize
}

export type InfoTableSize = "normal" | "tighter"

export const InfoTable = ({ children, size, ...props }: InfoTableProps) => (
  <section className={b([size])} {...props}>
    <ul className={b("body")}>{children}</ul>
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
