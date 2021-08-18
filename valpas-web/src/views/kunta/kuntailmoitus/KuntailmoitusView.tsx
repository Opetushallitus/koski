import bem from "bem-ts"
import React, { useMemo, useState } from "react"
import { Redirect, RouteComponentProps, useHistory } from "react-router"
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
import { Spinner } from "../../../components/icons/Spinner"
import {
  getOrganisaatiot,
  OrganisaatioValitsin,
} from "../../../components/shared/OrganisaatioValitsin"
import { DataTableCountChangeEvent } from "../../../components/tables/DataTable"
import { Counter } from "../../../components/typography/Counter"
import { NoDataMessage } from "../../../components/typography/NoDataMessage"
import { t, T } from "../../../i18n/i18n"
import {
  useOrganisaatiotJaKäyttöoikeusroolit,
  withRequiresKuntavalvonta,
} from "../../../state/accessRights"
import { useBasePath } from "../../../state/basePath"
import { Oid } from "../../../state/common"
import { createKuntailmoitusPathWithOrg } from "../../../state/paths"
import { ErrorView } from "../../ErrorView"
import { KuntaNavigation } from "../KuntaNavigation"
import { KuntailmoitusTable } from "./KuntailmoitusTable"

const b = bem("kuntailmoitusview")

const organisaatioHakuRooli = "KUNTA"

export const KuntailmoitusViewWithoutOrgOid = withRequiresKuntavalvonta(() => {
  const basePath = useBasePath()

  const organisaatiotJaKäyttöoikeusroolit = useOrganisaatiotJaKäyttöoikeusroolit()
  const organisaatiot = useMemo(
    () =>
      getOrganisaatiot(
        organisaatiotJaKäyttöoikeusroolit,
        organisaatioHakuRooli,
        "KUNTA",
        false
      ),
    [organisaatiotJaKäyttöoikeusroolit]
  )
  const organisaatio = organisaatiot[0]

  return organisaatio ? (
    <Redirect to={createKuntailmoitusPathWithOrg(basePath, organisaatio.oid)} />
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

    const organisaatiotJaKäyttöoikeusroolit = useOrganisaatiotJaKäyttöoikeusroolit()
    const organisaatiot = useMemo(
      () =>
        getOrganisaatiot(
          organisaatiotJaKäyttöoikeusroolit,
          organisaatioHakuRooli,
          "KUNTA",
          false
        ),
      [organisaatiotJaKäyttöoikeusroolit]
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
      fetchKuntailmoituksetCache
    )

    return (
      <Page>
        <OrganisaatioValitsin
          containerClassName={b("organisaatiovalitsin")}
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
          </CardHeader>
          <CardBody>
            {isLoading(fetch) && <Spinner />}
            {isSuccess(fetch) && fetch.data.length == 0 && (
              <NoDataMessage>
                <T id="kuntailmoitusnäkymä__ei_ilmoituksia" />
              </NoDataMessage>
            )}
            {isSuccess(fetch) && fetch.data.length > 0 && (
              <KuntailmoitusTable
                data={fetch.data}
                organisaatioOid={organisaatioOid}
                onCountChange={setCounters}
              />
            )}
          </CardBody>
        </Card>
      </Page>
    )
  }
)

const OrganisaatioMissingView = () => (
  <ErrorView
    title={t("kuntailmoitusnäkymä__ei_oikeuksia_title")}
    message={t("kuntailmoitusnäkymä__ei_oikeuksia_teksti")}
    head={<KuntaNavigation />}
  />
)
