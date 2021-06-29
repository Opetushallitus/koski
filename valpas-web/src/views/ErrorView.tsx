import React from "react"
import { Page } from "../components/containers/Page"
import { Heading } from "../components/typography/headings"
import { t } from "../i18n/i18n"

export type ErrorViewProps = {
  title: string
  message: string
  head?: React.ReactNode
  children?: React.ReactNode
}

export const ErrorView = (props: ErrorViewProps) => (
  <Page id="error-view">
    {props.head}
    <Heading>{props.title}</Heading>
    <p className="error-message">{props.message}</p>
    {props.children}
  </Page>
)

export const NotFoundView = () => (
  <ErrorView title={t("not_found_title")} message={t("not_found_teksti")} />
)
