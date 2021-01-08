import React from "react"
import "./Page.less"

export type PageProps = {
  children: React.ReactNode
}

export const Page = (props: PageProps) => (
  <article className="page">{props.children}</article>
)
