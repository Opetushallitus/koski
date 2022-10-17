import React from "react"
import { t } from "../../i18n/i18n"
import { withRequiresHakeutumisenValvonta } from "../../state/accessRights"
import {
  hakeutumisvalvonnanKunnalleIlmoitetutPathWithOrg,
  OrganisaatioOidRouteProps,
} from "../../state/paths"
import { ErrorView } from "../ErrorView"
import { HakutilanneNavigation } from "../hakutilanne/HakutilanneNavigation"
import { useHakeutumisvalvonnanKunnalleTehdytIlmoitukset } from "../hakutilanne/useOppijatData"
import { OrganisaatioAutoRedirect } from "../OrganisaatioAutoRedirect"
import { KunnalleIlmoitetutView } from "./KunnalleIlmoitetutView"

const organisaatioTyyppi = "OPPILAITOS"
const organisaatioHakuRooli = "OPPILAITOS_HAKEUTUMINEN"

export const HakeutumisenKunnalleIlmoitetutViewWithoutOrgOid =
  withRequiresHakeutumisenValvonta(() => (
    <OrganisaatioAutoRedirect
      organisaatioHakuRooli={organisaatioHakuRooli}
      organisaatioTyyppi={organisaatioTyyppi}
      redirectTo={(basePath, organisaatioOid) =>
        hakeutumisvalvonnanKunnalleIlmoitetutPathWithOrg.href(basePath, {
          organisaatioOid,
        })
      }
      renderError={() => <OrganisaatioMissingView />}
    />
  ))

export type HakeutumisenKunnalleIlmoitetutViewProps = OrganisaatioOidRouteProps

export const HakeutumisenKunnalleIlmoitetutView =
  withRequiresHakeutumisenValvonta(
    (props: HakeutumisenKunnalleIlmoitetutViewProps) => (
      <KunnalleIlmoitetutView
        organisaatioOid={props.match.params.organisaatioOid!}
        organisaatioHakuRooli={organisaatioHakuRooli}
        organisaatioTyyppi={organisaatioTyyppi}
        dataFetcher={useHakeutumisvalvonnanKunnalleTehdytIlmoitukset}
        backRefName="hakutilanneIlmoitetutRef"
        storageName="hakutilanneIlmoitetut"
        navigation={
          <HakutilanneNavigation
            selectedOrganisaatio={props.match.params.organisaatioOid!}
          />
        }
        linkCreator={hakeutumisvalvonnanKunnalleIlmoitetutPathWithOrg.href}
      />
    )
  )

const OrganisaatioMissingView = () => (
  <ErrorView
    title={t("hakutilanne__ei_oikeuksia_title")}
    message={t("hakutilanne__ei_oikeuksia_teksti")}
  />
)
