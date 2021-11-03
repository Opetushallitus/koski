import React, { useCallback, useMemo } from "react"
import { useHistory } from "react-router-dom"
import { fetchKuntarouhinta, fetchKuntarouhintaCache } from "../../../api/api"
import { useApiWithParams } from "../../../api/apiHooks"
import { isError, isLoading, isSuccess } from "../../../api/apiUtils"
import {
  Card,
  CardHeader,
  ConstrainedCardBody,
} from "../../../components/containers/cards"
import { Page } from "../../../components/containers/Page"
import { Spinner } from "../../../components/icons/Spinner"
import {
  getOrganisaatiot,
  OrganisaatioValitsin,
} from "../../../components/shared/OrganisaatioValitsin"
import { Counter } from "../../../components/typography/Counter"
import { ApiErrors } from "../../../components/typography/error"
import { getLanguage, getLocalized, T, t } from "../../../i18n/i18n"
import {
  useOrganisaatiotJaKäyttöoikeusroolit,
  withRequiresKuntavalvonta,
} from "../../../state/accessRights"
import { KuntarouhintaInput } from "../../../state/apitypes/rouhinta"
import { useBasePath } from "../../../state/basePath"
import { Oid } from "../../../state/common"
import {
  kuntarouhintaPathWithOid,
  OrganisaatioOidRouteProps,
} from "../../../state/paths"
import { ErrorView } from "../../ErrorView"
import { OrganisaatioAutoRedirect } from "../../OrganisaatioAutoRedirect"
import { KuntaNavigation } from "../KuntaNavigation"
import { KuntarouhintaTable } from "./KuntarouhintaTable"

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

    const history = useHistory()
    const basePath = useBasePath()
    const changeOrganisaatio = useCallback(
      (oid?: Oid) => {
        if (oid) {
          history.push(
            kuntarouhintaPathWithOid.href(basePath, { organisaatioOid: oid })
          )
        }
      },
      [basePath, history]
    )

    const kunta = useMemo(
      () => organisaatiot.find((o) => o.oid === organisaatioOid)?.kotipaikka,
      [organisaatiot, organisaatioOid]
    )

    const rouhintaQuery: [query: KuntarouhintaInput] | undefined = useMemo(
      () =>
        kunta?.koodiarvo
          ? [
              {
                kunta: kunta.koodiarvo,
                lang: getLanguage(),
              },
            ]
          : undefined,
      [kunta]
    )

    const rouhintaFetch = useApiWithParams(
      fetchKuntarouhinta,
      rouhintaQuery,
      fetchKuntarouhintaCache
    )

    return (
      <Page>
        <OrganisaatioValitsin
          organisaatioTyyppi={organisaatioTyyppi}
          organisaatioHierarkia={organisaatiot}
          valittuOrganisaatioOid={organisaatioOid}
          label={t("Kunta")}
          onChange={changeOrganisaatio}
        />
        <KuntaNavigation selectedOrganisaatio={organisaatioOid} />
        <Card>
          <CardHeader>
            {kunta?.nimi && `${getLocalized(kunta.nimi)}: `}
            <T id="rouhinta_taulukon_otsikko" />
            {isSuccess(rouhintaFetch) && (
              <Counter>
                {rouhintaFetch.data.eiOppivelvollisuuttaSuorittavat.length}
              </Counter>
            )}
          </CardHeader>
          <ConstrainedCardBody>
            {isLoading(rouhintaFetch) && <Spinner />}
            {isSuccess(rouhintaFetch) && (
              <KuntarouhintaTable
                data={rouhintaFetch.data}
                organisaatioOid={organisaatioOid}
              />
            )}
            {isError(rouhintaFetch) && (
              <ApiErrors errors={rouhintaFetch.errors} />
            )}
          </ConstrainedCardBody>
        </Card>
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
