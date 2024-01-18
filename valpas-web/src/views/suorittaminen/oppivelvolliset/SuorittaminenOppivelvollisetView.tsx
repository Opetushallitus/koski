import React, { useMemo, useState } from "react"
import { Redirect, RouteComponentProps, useHistory } from "react-router-dom"
import {
  fetchOppijatSuorittaminen,
  fetchOppijatSuorittaminenCache,
} from "../../../api/api"
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
  useStoredOrgState,
} from "../../../components/shared/OrganisaatioValitsin"
import { DataTableCountChangeEvent } from "../../../components/tables/DataTable"
import { Counter } from "../../../components/typography/Counter"
import { ApiErrors } from "../../../components/typography/error"
import { NoDataMessage } from "../../../components/typography/NoDataMessage"
import { t, T } from "../../../i18n/i18n"
import {
  useOrganisaatiotJaKäyttöoikeusroolit,
  withRequiresSuorittamisenValvonta,
} from "../../../state/accessRights"
import { useBasePath } from "../../../state/basePath"
import { Oid } from "../../../state/common"
import { suorittaminenPathWithOrg } from "../../../state/paths"
import { SuorittaminenOppivelvollisetTable } from "../../../views/suorittaminen/oppivelvolliset/SuorittaminenOppivelvollisetTable"
import { SuorittaminenNavigation } from "../../../views/suorittaminen/SuorittaminenNavigation"
import { ErrorView } from "../../ErrorView"
import "./SuorittaminenOppivelvollisetView.less"

const organisaatioTyyppi = "OPPILAITOS"
const organisaatioHakuRooli = "OPPILAITOS_SUORITTAMINEN"

export const SuorittaminenOppivelvollisetViewWithoutOrgOid =
  withRequiresSuorittamisenValvonta(() => {
    const basePath = useBasePath()
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
    const [storedOrFallbackOrg] = useStoredOrgState(
      organisaatioTyyppi,
      organisaatiot,
    )
    return storedOrFallbackOrg ? (
      <Redirect
        to={suorittaminenPathWithOrg.href(basePath, storedOrFallbackOrg)}
      />
    ) : (
      <OrganisaatioMissingView />
    )
  })

export type SuorittaminenOppivelvollisetViewProps = RouteComponentProps<{
  organisaatioOid?: string
}>

export const SuorittaminenOppivelvollisetView =
  withRequiresSuorittamisenValvonta(
    (props: SuorittaminenOppivelvollisetViewProps) => {
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
        fetchOppijatSuorittaminen,
        [organisaatioOid],
        fetchOppijatSuorittaminenCache,
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
          <SuorittaminenNavigation selectedOrganisaatio={organisaatioOid} />
          <Card>
            <CardHeader>
              <T id="suorittaminennäkymä__oppivelvolliset__otsikko" />
              {isSuccess(fetch) && (
                <Counter>
                  {counters.filteredRowCount === counters.unfilteredRowCount
                    ? counters.filteredRowCount
                    : `${counters.filteredRowCount} / ${counters.unfilteredRowCount}`}
                </Counter>
              )}
            </CardHeader>
            <ConstrainedCardBody>
              {isLoading(fetch) && <Spinner />}
              {isSuccess(fetch) && fetch.data.length === 0 && (
                <NoDataMessage>
                  <T id="suorittaminennäkymä__ei_oppivelvollisia" />
                </NoDataMessage>
              )}
              {isSuccess(fetch) && fetch.data.length > 0 && (
                <SuorittaminenOppivelvollisetTable
                  data={fetch.data}
                  organisaatioOid={organisaatioOid}
                  onCountChange={setCounters}
                />
              )}
              {isError(fetch) && <ApiErrors errors={fetch.errors} />}
            </ConstrainedCardBody>
          </Card>
        </Page>
      )
    },
  )

const OrganisaatioMissingView = () => (
  <ErrorView
    title={t("suorittaminennäkymä__ei_oikeuksia_title")}
    message={t("suorittaminennäkymä__ei_oikeuksia_teksti")}
    head={<SuorittaminenNavigation />}
  />
)
