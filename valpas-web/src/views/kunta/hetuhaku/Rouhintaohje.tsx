import bem from "bem-ts"
import React from "react"
import { InfoIcon } from "../../../components/icons/Icon"
import { ExternalLink } from "../../../components/navigation/ExternalLink"
import { t, T } from "../../../i18n/i18n"
import { plainComponent } from "../../../utils/plaincomponent"
import "./Rouhintaohje.less"

const b = bem("rouhintaohje")

export type RouhintaohjeProps = React.HTMLAttributes<HTMLElement>

const Container = plainComponent("div", b())

export const Rouhintaohje = ({ children, ...rest }: RouhintaohjeProps) => (
  <Container {...rest}>
    <InfoIcon />
    <div className={b("content")}>
      {children}
      <p>
        <ExternalLink to={t("rouhinta_ohjesivu_url")}>
          <T id="rouhinta_ohjesivu_linkkiteksti" />
        </ExternalLink>
      </p>
    </div>
  </Container>
)
