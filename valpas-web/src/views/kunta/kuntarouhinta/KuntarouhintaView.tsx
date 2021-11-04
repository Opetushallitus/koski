import React, { useMemo } from "react"
import { fetchKuntarouhinta, fetchKuntarouhintaCache } from "../../../api/api"
import { useApiMethod, useCacheWithParams } from "../../../api/apiHooks"
import { isError, isLoading, isSuccess } from "../../../api/apiUtils"
import { RaisedButton } from "../../../components/buttons/RaisedButton"
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
import { getLocalized, T, t } from "../../../i18n/i18n"
import {
  useOrganisaatiotJaKäyttöoikeusroolit,
  withRequiresKuntavalvonta,
} from "../../../state/accessRights"
import { KuntarouhintaInput } from "../../../state/apitypes/rouhinta"
import {
  kuntarouhintaPathWithOid,
  OrganisaatioOidRouteProps,
} from "../../../state/paths"
import { useRedirectToOrganisaatio } from "../../../state/useRedirect"
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

    const changeOrganisaatio = useRedirectToOrganisaatio(
      kuntarouhintaPathWithOid
    )

    const kunta = useMemo(
      () => organisaatiot.find((o) => o.oid === organisaatioOid)?.kotipaikka,
      [organisaatiot, organisaatioOid]
    )

    const rouhintaQuery: [KuntarouhintaInput] | undefined = useMemo(
      () => kunta && [createQuery(kunta.koodiarvo)],
      [kunta]
    )

    const rouhintaFetch = useApiMethod(
      fetchKuntarouhinta,
      fetchKuntarouhintaCache
    )
    const rouhintaData = useCacheWithParams(
      fetchKuntarouhintaCache,
      rouhintaQuery
    )

    const runQuery = () => {
      if (kunta?.koodiarvo) {
        rouhintaFetch.call(createQuery(kunta.koodiarvo))
      }
    }

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
            {!rouhintaData && (
              <FetchDataButton
                isLoading={isLoading(rouhintaFetch)}
                onClick={runQuery}
              />
            )}
            {isLoading(rouhintaFetch) && <Spinner />}
            {rouhintaData && (
              <KuntarouhintaTable
                data={rouhintaData}
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

type FetchDataButtonProps = {
  isLoading: boolean
  onClick: () => void
}

const FetchDataButton = (props: FetchDataButtonProps) => (
  <div>
    <p>
      <T id="rouhinta_kuntahaku_latausohje" />
    </p>
    <RaisedButton onClick={props.onClick} disabled={props.isLoading}>
      <T id="rouhinta_btn_näytä_selaimessa" />
    </RaisedButton>
  </div>
)

const OrganisaatioMissingView = () => (
  <ErrorView
    title={t("hakutilanne__ei_oikeuksia_title")}
    message={t("hakutilanne__ei_oikeuksia_teksti")}
  />
)

const createQuery = (kuntakoodi: string): KuntarouhintaInput => ({
  kunta: kuntakoodi,
})
