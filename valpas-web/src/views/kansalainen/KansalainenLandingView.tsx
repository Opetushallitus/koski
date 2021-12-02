import React from "react"
import { Link } from "react-router-dom"
import { RaisedButton } from "../../components/buttons/RaisedButton"
import { Page } from "../../components/containers/Page"
import { Heading } from "../../components/typography/headings"
import { T } from "../../i18n/i18n"
import { useBasePath } from "../../state/basePath"
import { kansalainenOmatTiedotPath } from "../../state/kansalainenPaths"

export const KansalainenLandingView = () => {
  const basePath = useBasePath()
  return (
    <Page>
      <Heading>Valpas</Heading>
      <Link to={kansalainenOmatTiedotPath.href(basePath)}>
        <RaisedButton>
          <T id="login__btn_kirjaudu" />
        </RaisedButton>
      </Link>
    </Page>
  )
}
