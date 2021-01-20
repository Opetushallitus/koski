import bem from "bem-ts"
import React from "react"
import { joinClassNames } from "../../utils/classnames"
import "./Page.less"

const b = bem("page")

export type PageProps = React.HTMLAttributes<HTMLDivElement>

export const Page = ({ className, ...props }: PageProps) => (
  <article className={joinClassNames(b(), className)} {...props} />
)
