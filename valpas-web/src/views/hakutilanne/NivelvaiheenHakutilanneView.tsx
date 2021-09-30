import React, { useCallback, useMemo } from "react"
import { useHistory } from "react-router-dom"
import { Page } from "../../components/containers/Page"
import {
  getOrganisaatiot,
  OrganisaatioValitsin,
} from "../../components/shared/OrganisaatioValitsin"
import { t } from "../../i18n/i18n"
import {
  useOrganisaatiotJaKäyttöoikeusroolit,
  withRequiresHakeutumisenValvonta,
} from "../../state/accessRights"
import { useBasePath } from "../../state/basePath"
import { Oid } from "../../state/common"
import {
  createNivelvaiheenHakutilannePathWithOrg,
  NivelvaiheenHakutilanneViewRouteProps,
} from "../../state/paths"
import { ErrorView } from "../ErrorView"
import { OrganisaatioAutoRedirect } from "../OrganisaatioAutoRedirect"
import { HakutilanneNavigation } from "./HakutilanneNavigation"

const organisaatioTyyppi = "OPPILAITOS"
const organisaatioHakuRooli = "OPPILAITOS_HAKEUTUMINEN"

export type NivelvaiheenHakutilanneViewProps = NivelvaiheenHakutilanneViewRouteProps

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
  (props: NivelvaiheenHakutilanneViewProps) => {
    const basePath = useBasePath()
    const history = useHistory()
    const organisaatiotJaKäyttöoikeusroolit = useOrganisaatiotJaKäyttöoikeusroolit()
    const organisaatiot = useMemo(
      () =>
        getOrganisaatiot(
          organisaatiotJaKäyttöoikeusroolit,
          organisaatioHakuRooli,
          organisaatioTyyppi
        ),
      [organisaatiotJaKäyttöoikeusroolit]
    )

    const organisaatioOid = props.match.params.organisaatioOid!

    const changeOrganisaatio = useCallback(
      (oid?: Oid) => {
        if (oid) {
          history.push(
            createNivelvaiheenHakutilannePathWithOrg(basePath, {
              organisaatioOid: oid,
            })
          )
        }
      },
      [basePath, history]
    )

    return (
      <Page>
        <OrganisaatioValitsin
          organisaatioTyyppi={organisaatioTyyppi}
          organisaatioHierarkia={organisaatiot}
          valittuOrganisaatioOid={organisaatioOid}
          label={t("Oppilaitos")}
          onChange={changeOrganisaatio}
        />
        <HakutilanneNavigation selectedOrganisaatio={organisaatioOid} />
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
