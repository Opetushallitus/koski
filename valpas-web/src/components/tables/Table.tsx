import bem from "bem-ts"
import React from "react"
import { joinClassNames } from "../../utils/classnames"
import "./Table.less"

const b = bem("table")

export type TableProps = React.HTMLAttributes<HTMLTableElement>

export const Table = ({ children, className, ...rest }: TableProps) => (
  <table {...rest} className={joinClassNames(b(), className)}>
    {children}
  </table>
)

export const TableHeader = ({
  className,
  ...rest
}: React.HTMLAttributes<HTMLTableSectionElement>) => (
  <thead {...rest} className={joinClassNames(b("head"), className)} />
)

export const TableBody = ({
  className,
  ...rest
}: React.HTMLAttributes<HTMLTableSectionElement>) => (
  <thead {...rest} className={joinClassNames(b("body"), className)} />
)

export const Row = ({
  className,
  ...rest
}: React.HTMLAttributes<HTMLTableRowElement>) => (
  <tr {...rest} className={joinClassNames(b("row"), className)} />
)

export type DataProps = React.HTMLAttributes<HTMLTableDataCellElement> & {
  icon?: React.ReactNode
}

export const Data = ({ className, children, icon, ...rest }: DataProps) => (
  <td {...rest} className={joinClassNames(b("td"), className)}>
    {icon && <div className={b("icon")}>{icon}</div>}
    {children}
  </td>
)

export const HeaderCell = ({
  className,
  ...rest
}: React.HTMLAttributes<HTMLTableHeaderCellElement>) => (
  <th {...rest} className={joinClassNames(b("th"), className)} />
)
