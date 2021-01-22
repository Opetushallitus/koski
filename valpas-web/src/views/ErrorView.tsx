import React from "react"
import { Page } from "../components/containers/Page"
import { Heading } from "../components/typography/headings"

export type ErrorViewProps = {
  title: string
  message: string
}

export const ErrorView = (props: ErrorViewProps) => (
  <Page>
    <Heading>{props.title}</Heading>
    <p>{props.message}</p>
  </Page>
)
