import React from "react"
import { t } from "../../i18n/i18n"
import { withRequiresHakeutumisenTaiSuorittamisenValvonta } from "../../state/accessRights"
import {
  kunnalleIlmoitetutPathWithOrg,
  OrganisaatioOidRouteProps,
} from "../../state/paths"
import { ErrorView } from "../ErrorView"
import { useHakeutumisvalvonnanKunnalleTehdytIlmoitukset } from "../hakutilanne/useOppijatData"
import { OrganisaatioAutoRedirect } from "../OrganisaatioAutoRedirect"
import { KunnalleIlmoitetutView } from "./KunnalleIlmoitetutView"

const organisaatioTyyppi = "OPPILAITOS"
const organisaatioHakuRooli = "OPPILAITOS_HAKEUTUMINEN"

export const YhdistettyKunnalleIlmoitetutViewWithoutOrgOid =
  withRequiresHakeutumisenTaiSuorittamisenValvonta(() => (
    <OrganisaatioAutoRedirect
      organisaatioHakuRooli={organisaatioHakuRooli}
      organisaatioTyyppi={organisaatioTyyppi}
      redirectTo={(basePath, organisaatioOid) =>
        kunnalleIlmoitetutPathWithOrg.href(basePath, {
          organisaatioOid,
        })
      }
      renderError={() => <OrganisaatioMissingView />}
    />
  ))

export type YhdistettyKunnalleIlmoitetutViewProps = OrganisaatioOidRouteProps

export const YhdistettyKunnalleIlmoitetutView =
  withRequiresHakeutumisenTaiSuorittamisenValvonta(
    (props: YhdistettyKunnalleIlmoitetutViewProps) => (
      <KunnalleIlmoitetutView
        organisaatioOid={props.match.params.organisaatioOid!}
        organisaatioHakuRooli={organisaatioHakuRooli}
        organisaatioTyyppi={organisaatioTyyppi}
        dataFetcher={useHakeutumisvalvonnanKunnalleTehdytIlmoitukset}
        linkCreator={kunnalleIlmoitetutPathWithOrg.href}
      />
    )
  )

const OrganisaatioMissingView = () => (
  <ErrorView
    title={t("hakutilanne__ei_oikeuksia_title")}
    message={t("hakutilanne__ei_oikeuksia_teksti")}
  />
)
