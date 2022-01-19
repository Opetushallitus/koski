import bem from "bem-ts"
import React from "react"
import { Link } from "react-router-dom"
import { RaisedButton } from "../../components/buttons/RaisedButton"
import { Page } from "../../components/containers/Page"
import { Heading } from "../../components/typography/headings"
import { Lead } from "../../components/typography/Lead"
import { T, TParagraphs } from "../../i18n/i18n"
import { useBasePath } from "../../state/basePath"
import { kansalainenOmatTiedotPath } from "../../state/kansalainenPaths"
import "./KansalainenLandingView.less"

const b = bem("landing")

export const KansalainenLandingView = () => {
  const basePath = useBasePath()
  return (
    <Page className={b("content")}>
      <Heading>
        <T id="kansalainen_omattiedot_otsikko" />
      </Heading>
      <Lead className={b("lead")}>
        <TParagraphs id="kansalainen_etusivu_kuvaus" />
      </Lead>
      <TParagraphs id="kansalainen_etusivu_tietojen_käyttö" />
      <div className={b("login")}>
        <Link to={kansalainenOmatTiedotPath.href(basePath)}>
          <RaisedButton className={b("button")}>
            <T id="login__btn_kirjaudu" />
          </RaisedButton>
        </Link>
      </div>
    </Page>
  )
}
