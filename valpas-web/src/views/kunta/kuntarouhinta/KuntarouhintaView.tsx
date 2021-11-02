import React from "react"
import { Page } from "../../../components/containers/Page"
import { t } from "../../../i18n/i18n"
import { withRequiresKuntavalvonta } from "../../../state/accessRights"
import {
  kuntarouhintaPathWithOid,
  OrganisaatioOidRouteProps,
} from "../../../state/paths"
import { ErrorView } from "../../ErrorView"
import { OrganisaatioAutoRedirect } from "../../OrganisaatioAutoRedirect"
import { KuntaNavigation } from "../KuntaNavigation"

const organisaatioTyyppi = "KUNTA"
const organisaatioHakuRooli = "KUNTA"

export const KuntarouhintaViewWithoutOrg = withRequiresKuntavalvonta(() => (
  <OrganisaatioAutoRedirect
    organisaatioHakuRooli={organisaatioHakuRooli}
    organisaatioTyyppi={organisaatioTyyppi}
    redirectTo={(basePath, organisaatioOid) =>
      kuntarouhintaPathWithOid.href(basePath, {
        organisaatioOid,
      })
    }
    renderError={() => <OrganisaatioMissingView />}
  />
))

export type KuntarouhintaViewProps = OrganisaatioOidRouteProps

export const KuntarouhintaView = withRequiresKuntavalvonta(
  (props: KuntarouhintaViewProps) => {
    const organisaatioOid = props.match.params.organisaatioOid!

    return (
      <Page>
        <KuntaNavigation selectedOrganisaatio={organisaatioOid} />
        TODO: Rouhinta {organisaatioOid}
      </Page>
    )
  }
)

const OrganisaatioMissingView = () => (
  <ErrorView
    title={t("hakutilanne__ei_oikeuksia_title")}
    message={t("hakutilanne__ei_oikeuksia_teksti")}
  />
)
