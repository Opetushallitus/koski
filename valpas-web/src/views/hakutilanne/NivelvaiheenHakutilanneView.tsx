import React from "react"
import { Page } from "../../components/containers/Page"
import { t } from "../../i18n/i18n"
import { withRequiresHakeutumisenValvonta } from "../../state/accessRights"
import { createNivelvaiheenHakutilannePathWithOrg } from "../../state/paths"
import { ErrorView } from "../ErrorView"
import { OrganisaatioAutoRedirect } from "../OrganisaatioAutoRedirect"

const organisaatioTyyppi = "OPPILAITOS"
const organisaatioHakuRooli = "OPPILAITOS_HAKEUTUMINEN"

export const NivelvaiheenHakutilanneViewWithoutOrgOid = withRequiresHakeutumisenValvonta(
  () => (
    <OrganisaatioAutoRedirect
      organisaatioHakuRooli={organisaatioHakuRooli}
      organisaatioTyyppi={organisaatioTyyppi}
      redirectTo={(basePath, organisaatioOid) =>
        createNivelvaiheenHakutilannePathWithOrg(basePath, {
          organisaatioOid,
        })
      }
      renderError={() => <OrganisaatioMissingView />}
    />
  )
)

export const NivelvaiheenHakutilanneView = withRequiresHakeutumisenValvonta(
  () => <Page>TODO: Nivelvaiheen hakutilanne</Page>
)

const OrganisaatioMissingView = () => (
  <ErrorView
    title={t("hakutilanne__ei_oikeuksia_title")}
    message={t("hakutilanne__ei_oikeuksia_teksti")}
  />
)
