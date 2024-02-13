import React from "react"
import { t } from "../../i18n/i18n"
import { withRequiresHakeutumisenTaiSuorittamisenValvonta } from "../../state/accessRights"
import { oppilaitosroolit } from "../../state/common"
import {
  kunnalleIlmoitetutPathWithOrg,
  OrganisaatioOidRouteProps,
} from "../../state/paths"
import { ErrorView } from "../ErrorView"
import { OrganisaatioAutoRedirect } from "../OrganisaatioAutoRedirect"
import { KunnalleIlmoitetutView } from "./KunnalleIlmoitetutView"

const organisaatioTyyppi = "OPPILAITOS"

export const YhdistettyKunnalleIlmoitetutViewWithoutOrgOid =
  withRequiresHakeutumisenTaiSuorittamisenValvonta(() => (
    <OrganisaatioAutoRedirect
      organisaatioHakuRooli={oppilaitosroolit}
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
    (props: YhdistettyKunnalleIlmoitetutViewProps) => {
      return (
        <KunnalleIlmoitetutView
          organisaatioOid={props.match.params.organisaatioOid!}
          organisaatioTyyppi={organisaatioTyyppi}
        />
      )
    },
  )

const OrganisaatioMissingView = () => (
  <ErrorView
    title={t("hakutilanne__ei_oikeuksia_title")}
    message={t("hakutilanne__ei_oikeuksia_teksti")}
  />
)
