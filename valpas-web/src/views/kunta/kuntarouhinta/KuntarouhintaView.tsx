import bem from "bem-ts"
import React, { useCallback, useMemo } from "react"
import {
  downloadKuntarouhinta,
  fetchKuntarouhinta,
  fetchKuntarouhintaCache,
} from "../../../api/api"
import {
  ApiMethodHook,
  useApiMethod,
  useCacheWithParams,
} from "../../../api/apiHooks"
import { isError, isInitial, isLoading, isSuccess } from "../../../api/apiUtils"
import { ButtonGroup } from "../../../components/buttons/ButtonGroup"
import { RaisedButton } from "../../../components/buttons/RaisedButton"
import {
  Card,
  CardBody,
  CardHeader,
  ConstrainedCardBody,
} from "../../../components/containers/cards"
import { Page } from "../../../components/containers/Page"
import { Spinner } from "../../../components/icons/Spinner"
import { Password } from "../../../components/Password"
import {
  getOrganisaatiot,
  OrganisaatioValitsin,
} from "../../../components/shared/OrganisaatioValitsin"
import { Counter } from "../../../components/typography/Counter"
import { ApiErrors } from "../../../components/typography/error"
import { getLocalized, T, t, TParagraphs } from "../../../i18n/i18n"
import {
  useOrganisaatiotJaKäyttöoikeusroolit,
  withRequiresKuntavalvonta,
} from "../../../state/accessRights"
import { KuntarouhintaInput } from "../../../state/apitypes/rouhinta"
import { Oid } from "../../../state/common"
import { usePassword } from "../../../state/password"
import {
  kuntarouhintaPathWithOid,
  OrganisaatioOidRouteProps,
} from "../../../state/paths"
import { useRedirectToOrganisaatio } from "../../../state/useRedirect"
import { ErrorView } from "../../ErrorView"
import { OrganisaatioAutoRedirect } from "../../OrganisaatioAutoRedirect"
import { Rouhintaohje } from "../hetuhaku/Rouhintaohje"
import { KuntaNavigation } from "../KuntaNavigation"
import { KuntarouhintaTable } from "./KuntarouhintaTable"
import "./KuntarouhintaView.less"

const b = bem("kuntarouhintaview")

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
    const organisaatiotJaKäyttöoikeusroolit =
      useOrganisaatiotJaKäyttöoikeusroolit()
    const organisaatiot = useMemo(
      () =>
        getOrganisaatiot(
          organisaatiotJaKäyttöoikeusroolit,
          organisaatioHakuRooli,
          organisaatioTyyppi,
        ),
      [organisaatiotJaKäyttöoikeusroolit],
    )

    const changeOrganisaatio = useRedirectToOrganisaatio(
      kuntarouhintaPathWithOid,
    )

    const rouhintaQuery: [KuntarouhintaInput] = useMemo(
      () => [createQuery(organisaatioOid)],
      [organisaatioOid],
    )

    const rouhintaFetch = useApiMethod(
      fetchKuntarouhinta,
      fetchKuntarouhintaCache,
    )
    const rouhintaData = useCacheWithParams(
      fetchKuntarouhintaCache,
      rouhintaQuery,
    )

    const fetchTableData = useCallback(() => {
      rouhintaFetch.call(createQuery(organisaatioOid))
    }, [organisaatioOid, rouhintaFetch])

    const password = usePassword()
    const rouhintaDownload = useApiMethod(downloadKuntarouhinta)
    const downloadData = useCallback(() => {
      rouhintaDownload.call(createQuery(organisaatioOid, password))
    }, [organisaatioOid, password, rouhintaDownload])

    const kunta = useMemo(
      () => organisaatiot.find((o) => o.oid === organisaatioOid)?.kotipaikka,
      [organisaatiot, organisaatioOid],
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
        <Rouhintaohje className={b("ohje")}>
          <TParagraphs id="rouhinta_kuntahaku_ohje" />
        </Rouhintaohje>

        {!rouhintaData ? (
          <FetchDataConfirmation
            rouhintaFetch={rouhintaFetch}
            rouhintaDownload={rouhintaDownload}
            onFetchClick={fetchTableData}
            onDownloadClick={downloadData}
            password={password}
          />
        ) : (
          <>
            <Card>
              <CardHeader className={b("cardheader")}>
                <div className={b("cardheaderlabel")}>
                  {kunta?.nimi && `${getLocalized(kunta.nimi)}: `}
                  <T id="rouhinta_taulukon_otsikko" />
                  {isSuccess(rouhintaFetch) && (
                    <Counter>
                      {
                        rouhintaFetch.data.eiOppivelvollisuuttaSuorittavat
                          .length
                      }
                    </Counter>
                  )}
                </div>
                <div>
                  {isInitial(rouhintaDownload) || isError(rouhintaDownload) ? (
                    <RaisedButton
                      id="rouhinta-table-download-btn"
                      onClick={downloadData}
                    >
                      <T id="rouhinta_btn_lataa_tiedosto" />
                    </RaisedButton>
                  ) : (
                    <Password className={b("tablepassword")}>
                      {password}
                    </Password>
                  )}
                  {isLoading(rouhintaDownload) && <Spinner />}
                </div>
              </CardHeader>
              <ConstrainedCardBody>
                {isError(rouhintaDownload) && (
                  <ApiErrors errors={rouhintaDownload.errors} />
                )}
                {rouhintaData && (
                  <KuntarouhintaTable
                    data={rouhintaData}
                    organisaatioOid={organisaatioOid}
                  />
                )}
              </ConstrainedCardBody>
            </Card>
          </>
        )}
      </Page>
    )
  },
)

type FetchDataButtonProps = {
  rouhintaFetch: ApiMethodHook<any, any>
  rouhintaDownload: ApiMethodHook<any, any>
  onFetchClick: () => void
  onDownloadClick: () => void
  password: string
}

const FetchDataConfirmation = (props: FetchDataButtonProps) => {
  const loading =
    isLoading(props.rouhintaFetch) || isLoading(props.rouhintaDownload)
  return (
    <Card id="rouhinta-fetch-confirm-dialog">
      <CardBody>
        <p>
          <T id="rouhinta_kuntahaku_latausohje" />
        </p>
        <ButtonGroup>
          <RaisedButton
            id="confirm-rouhinta-fetch-btn"
            onClick={props.onFetchClick}
            disabled={loading}
          >
            <T id="rouhinta_btn_näytä_selaimessa" />
          </RaisedButton>
          <RaisedButton
            id="confirm-rouhinta-download-btn"
            onClick={props.onDownloadClick}
            disabled={loading}
          >
            <T id="rouhinta_btn_lataa_tiedosto" />
          </RaisedButton>
        </ButtonGroup>
        {!isInitial(props.rouhintaDownload) && (
          <Password className={b("confirmpassword")}>{props.password}</Password>
        )}
        {loading && <Spinner />}
        {isError(props.rouhintaFetch) && (
          <ApiErrors errors={props.rouhintaFetch.errors} />
        )}
        {isError(props.rouhintaDownload) && (
          <ApiErrors errors={props.rouhintaDownload.errors} />
        )}
      </CardBody>
    </Card>
  )
}

const OrganisaatioMissingView = () => (
  <ErrorView
    title={t("hakutilanne__ei_oikeuksia_title")}
    message={t("hakutilanne__ei_oikeuksia_teksti")}
  />
)

const createQuery = (kuntaOid: Oid, password?: string): KuntarouhintaInput => ({
  kuntaOid,
  password,
})
