import React from "react"
import { Page } from "../components/containers/Page"
import { Heading } from "../components/typography/headings"

export type ErrorViewProps = {
  title: string
  message: string
}

export default (props: ErrorViewProps) => (
  <Page id="error-view">
    <Heading>{props.title}</Heading>
    <p className="error-message">{props.message}</p>
  </Page>
)
