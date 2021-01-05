import React from "react"
import ReactDOM from "react-dom"
import "./style/index.less"
import { Page } from "./components/containers/Page"
import { Heading, SecondaryHeading } from "./components/typography/headings"
import { Card } from "./components/containers/Card"

ReactDOM.render(
  <Page>
    <Heading>Valpas-komponenttikirjasto</Heading>
    <SecondaryHeading>Napit</SecondaryHeading>
    <Card>Todo</Card>
  </Page>,
  document.getElementById("app")
)
