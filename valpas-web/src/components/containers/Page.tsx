import React from "react"
import "./Page.less"

export type Props = {
  children: React.ReactNode
}

export const Page = (props: Props) => (
  <article className="page">{props.children}</article>
)
