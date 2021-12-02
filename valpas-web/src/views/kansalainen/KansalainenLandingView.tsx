import React from "react"
import { Link } from "react-router-dom"
import { RaisedButton } from "../../components/buttons/RaisedButton"
import { Page } from "../../components/containers/Page"
import { Heading } from "../../components/typography/headings"
import { useBasePath } from "../../state/basePath"
import { kansalainenOmatTiedotPath } from "../../state/kansalainenPaths"

export const KansalainenLandingView = () => {
  const basePath = useBasePath()
  return (
    <Page>
      <Heading>Valpas</Heading>
      <Link to={kansalainenOmatTiedotPath.href(basePath)}>
        <RaisedButton>Kirjaudu</RaisedButton>
      </Link>
    </Page>
  )
}
