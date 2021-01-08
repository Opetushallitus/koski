import bem from "bem-ts"
import React from "react"
import "./Columns.less"

const b = bem("columns")

export type ColumnsContainerProps = {
  children: React.ReactNode
}

/**
 * Container for 12-column grid system.
 * Child elements must be Column components.
 */
export const ColumnsContainer = (props: ColumnsContainerProps) => (
  <div className={b("container")}>{props.children}</div>
)

export type ColumnProps = React.HTMLAttributes<HTMLDivElement> & {
  size: number
}

/**
 * Child of ColumnsContainer.
 * @param size Width of the column 1-12 where 12 is full width, 6 half of the container etc.
 */
export const Column = ({ size, children, ...rest }: ColumnProps) => (
  <div className={b("column", [size.toString()])} {...rest}>
    {children}
  </div>
)
