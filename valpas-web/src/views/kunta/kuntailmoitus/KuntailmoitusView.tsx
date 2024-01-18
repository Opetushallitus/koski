import bem from "bem-ts"
import * as A from "fp-ts/Array"
import { pipe } from "fp-ts/lib/function"
import React, { useMemo, useState } from "react"
import { Redirect, RouteComponentProps, useHistory } from "react-router-dom"
import {
  fetchKuntailmoitukset,
  fetchKuntailmoituksetCache,
} from "../../../api/api"
import { useApiWithParams } from "../../../api/apiHooks"
import { isLoading, isSuccess } from "../../../api/apiUtils"
import {
  Card,
  CardBody,
  CardHeader,
} from "../../../components/containers/cards"
import { Page } from "../../../components/containers/Page"
import { LabeledCheckbox } from "../../../components/forms/Checkbox"
import { Spinner } from "../../../components/icons/Spinner"
import {
  getOrganisaatiot,
  OrganisaatioValitsin,
  useStoredOrgState,
} from "../../../components/shared/OrganisaatioValitsin"
import { DataTableCountChangeEvent } from "../../../components/tables/DataTable"
import { Counter } from "../../../components/typography/Counter"
import { NoDataMessage } from "../../../components/typography/NoDataMessage"
import { t, T } from "../../../i18n/i18n"
import {
  useOrganisaatiotJaKäyttöoikeusroolit,
  withRequiresKuntavalvonta,
} from "../../../state/accessRights"
import { getNäytettävätIlmoitukset } from "../../../state/apitypes/kuntailmoitus"
import { OppijaKuntailmoituksillaSuppeatTiedot } from "../../../state/apitypes/oppija"
import { useBasePath } from "../../../state/basePath"
import { Oid } from "../../../state/common"
import { kuntailmoitusPathWithOrg } from "../../../state/paths"
import { ErrorView } from "../../ErrorView"
import { KuntaNavigation } from "../KuntaNavigation"
import { KuntailmoitusTable } from "./KuntailmoitusTable"
import "./KuntailmoitusView.less"

const b = bem("kuntailmoitusview")

const organisaatioTyyppi = "KUNTA"
const organisaatioHakuRooli = "KUNTA"

export const KuntailmoitusViewWithoutOrgOid = withRequiresKuntavalvonta(() => {
  const basePath = useBasePath()
  const organisaatiotJaKäyttöoikeusroolit =
    useOrganisaatiotJaKäyttöoikeusroolit()
  const organisaatiot = useMemo(
    () =>
      getOrganisaatiot(
        organisaatiotJaKäyttöoikeusroolit,
        organisaatioHakuRooli,
        organisaatioTyyppi,
        false,
      ),
    [organisaatiotJaKäyttöoikeusroolit],
  )
  const [storedOrFallbackOrg] = useStoredOrgState(
    organisaatioTyyppi,
    organisaatiot,
  )
  return storedOrFallbackOrg ? (
    <Redirect
      to={kuntailmoitusPathWithOrg.href(basePath, storedOrFallbackOrg)}
    />
  ) : (
    <OrganisaatioMissingView />
  )
})

export type KuntailmoitusViewProps = RouteComponentProps<{
  organisaatioOid?: string
}>

export const KuntailmoitusView = withRequiresKuntavalvonta(
  (props: KuntailmoitusViewProps) => {
    const history = useHistory()

    const [counters, setCounters] = useState<DataTableCountChangeEvent>({
      filteredRowCount: 0,
      unfilteredRowCount: 0,
    })

    const organisaatiotJaKäyttöoikeusroolit =
      useOrganisaatiotJaKäyttöoikeusroolit()
    const organisaatiot = useMemo(
      () =>
        getOrganisaatiot(
          organisaatiotJaKäyttöoikeusroolit,
          organisaatioHakuRooli,
          organisaatioTyyppi,
          false,
        ),
      [organisaatiotJaKäyttöoikeusroolit],
    )

    const changeOrganisaatio = (oid?: Oid) => {
      if (oid) {
        history.push(oid)
      }
    }

    const organisaatioOid = props.match.params.organisaatioOid!

    const fetch = useApiWithParams(
      fetchKuntailmoitukset,
      [organisaatioOid],
      fetchKuntailmoituksetCache,
    )

    const [näytäAiemminTehdytIlmoitukset, setNäytäAiemminTehdytIlmoitukset] =
      useState(false)
    const [data, aiemminTehdytIlmoituksetLkm] = useMemo(() => {
      const arr = isSuccess(fetch) ? fetch.data : []
      const arrIlmanVanhoja = arr.map(poistaAiemminTehdytIlmoitukset)
      return [
        näytäAiemminTehdytIlmoitukset ? arr : arrIlmanVanhoja,
        kuntailmoitustenMäärä(arr) - kuntailmoitustenMäärä(arrIlmanVanhoja),
      ]
    }, [fetch, näytäAiemminTehdytIlmoitukset])

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
            <T id="kuntailmoitusnäkymä__ilmoitetut__otsikko" />
            {isSuccess(fetch) && (
              <Counter>
                {counters.filteredRowCount === counters.unfilteredRowCount
                  ? counters.filteredRowCount
                  : `${counters.filteredRowCount} / ${counters.unfilteredRowCount}`}
              </Counter>
            )}
            <LabeledCheckbox
              inline
              label={
                t("kuntailmoitusnäkymä__näytä_aiemmin_tehdyt_ilmoitukset") +
                ` (${aiemminTehdytIlmoituksetLkm})`
              }
              value={näytäAiemminTehdytIlmoitukset}
              onChange={setNäytäAiemminTehdytIlmoitukset}
              className={b("vanhatilmoituksetcb")}
              testId="arkistoidutcb"
            />
          </CardHeader>
          <CardBody>
            {isLoading(fetch) && <Spinner />}
            {isSuccess(fetch) && fetch.data.length === 0 && (
              <NoDataMessage>
                <T id="kuntailmoitusnäkymä__ei_ilmoituksia" />
              </NoDataMessage>
            )}
            {isSuccess(fetch) && data.length > 0 && (
              <KuntailmoitusTable
                data={data}
                organisaatioOid={organisaatioOid}
                onCountChange={setCounters}
              />
            )}
          </CardBody>
        </Card>
      </Page>
    )
  },
)

const OrganisaatioMissingView = () => (
  <ErrorView
    title={t("kuntailmoitusnäkymä__ei_oikeuksia_title")}
    message={t("kuntailmoitusnäkymä__ei_oikeuksia_teksti")}
    head={<KuntaNavigation />}
  />
)

const poistaAiemminTehdytIlmoitukset = (
  tiedot: OppijaKuntailmoituksillaSuppeatTiedot,
) => {
  const näytettävätIlmoitukset = getNäytettävätIlmoitukset(tiedot).map(
    (i) => i.id,
  )

  return {
    ...tiedot,
    kuntailmoitukset: tiedot.kuntailmoitukset.filter((i) =>
      näytettävätIlmoitukset.includes(i.id),
    ),
  }
}

const kuntailmoitustenMäärä = (
  tiedot: OppijaKuntailmoituksillaSuppeatTiedot[],
): number =>
  pipe(
    tiedot,
    A.map((t) => t.kuntailmoitukset.length),
    A.reduce(0, (a, b) => a + b),
  )
