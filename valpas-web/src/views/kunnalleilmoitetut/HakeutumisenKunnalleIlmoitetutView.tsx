import React from "react"
import { Page } from "../../components/containers/Page"
import { t } from "../../i18n/i18n"
import { withRequiresHakeutumisenValvonta } from "../../state/accessRights"
import {
  createHakeutumisvalvonnanKunnalleIlmoitetutPathWithOrg,
  KunnalleIlmoitetutViewRouteProps,
} from "../../state/paths"
import { ErrorView } from "../ErrorView"
import { OrganisaatioAutoRedirect } from "../OrganisaatioAutoRedirect"

export const HakeutumisenKunnalleIlmoitetutViewWithoutOrgOid = withRequiresHakeutumisenValvonta(
  () => (
    <OrganisaatioAutoRedirect
      organisaatioHakuRooli="OPPILAITOS_HAKEUTUMINEN"
      organisaatioTyyppi="OPPILAITOS"
      redirectTo={(basePath, organisaatioOid) =>
        createHakeutumisvalvonnanKunnalleIlmoitetutPathWithOrg(basePath, {
          organisaatioOid,
        })
      }
      renderError={() => <OrganisaatioMissingView />}
    />
  )
)

export const HakeutumisenKunnalleIlmoitetutView = withRequiresHakeutumisenValvonta(
  (props: KunnalleIlmoitetutViewRouteProps) => (
    <Page>
      TODO: HakeutumisenKunnalleIlmoitetutView{" "}
      {props.match.params.organisaatioOid}
    </Page>
  )
)

const OrganisaatioMissingView = () => (
  <ErrorView
    title={t("hakutilanne__ei_oikeuksia_title")}
    message={t("hakutilanne__ei_oikeuksia_teksti")}
  />
)
