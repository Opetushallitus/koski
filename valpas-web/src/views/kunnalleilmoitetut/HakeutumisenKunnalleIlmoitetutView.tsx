import React from "react"
import { t } from "../../i18n/i18n"
import { withRequiresHakeutumisenValvonta } from "../../state/accessRights"
import {
  createHakeutumisvalvonnanKunnalleIlmoitetutPathWithOrg,
  KunnalleIlmoitetutViewRouteProps,
} from "../../state/paths"
import { ErrorView } from "../ErrorView"
import { HakutilanneNavigation } from "../hakutilanne/HakutilanneNavigation"
import { useHakeutumisvalvonnanKunnalleTehdytIlmoitukset } from "../hakutilanne/useOppijatData"
import { OrganisaatioAutoRedirect } from "../OrganisaatioAutoRedirect"
import { KunnalleIlmoitetutView } from "./KunnalleIlmoitetutView"

const organisaatioTyyppi = "OPPILAITOS"
const organisaatioHakuRooli = "OPPILAITOS_HAKEUTUMINEN"

export const HakeutumisenKunnalleIlmoitetutViewWithoutOrgOid = withRequiresHakeutumisenValvonta(
  () => (
    <OrganisaatioAutoRedirect
      organisaatioHakuRooli={organisaatioHakuRooli}
      organisaatioTyyppi={organisaatioTyyppi}
      redirectTo={(basePath, organisaatioOid) =>
        createHakeutumisvalvonnanKunnalleIlmoitetutPathWithOrg(basePath, {
          organisaatioOid,
        })
      }
      renderError={() => <OrganisaatioMissingView />}
    />
  )
)

export type HakeutumisenKunnalleIlmoitetutViewProps = KunnalleIlmoitetutViewRouteProps

export const HakeutumisenKunnalleIlmoitetutView = withRequiresHakeutumisenValvonta(
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
      linkCreator={createHakeutumisvalvonnanKunnalleIlmoitetutPathWithOrg}
    />
  )
)

const OrganisaatioMissingView = () => (
  <ErrorView
    title={t("hakutilanne__ei_oikeuksia_title")}
    message={t("hakutilanne__ei_oikeuksia_teksti")}
  />
)
