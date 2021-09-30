import React, { useCallback, useMemo, useState } from "react"
import { useHistory } from "react-router-dom"
import { setMuuHaku } from "../../api/api"
import {
  Card,
  CardHeader,
  ConstrainedCardBody,
} from "../../components/containers/cards"
import { Page } from "../../components/containers/Page"
import { Spinner } from "../../components/icons/Spinner"
import {
  getOrganisaatiot,
  OrganisaatioValitsin,
} from "../../components/shared/OrganisaatioValitsin"
import { DataTableCountChangeEvent } from "../../components/tables/DataTable"
import { NumberCounter } from "../../components/typography/Counter"
import { ApiErrors } from "../../components/typography/error"
import { T, t } from "../../i18n/i18n"
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
import { NivelvaiheenHakutilanneTable } from "./NivelvaiheenHakutilanneTable"
import { useNivelvaiheenOppijatData } from "./useOppijatData"

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

    const { data, isLoading, errors } = useNivelvaiheenOppijatData(
      organisaatioOid
    )
    const [counters, setCounters] = useState<DataTableCountChangeEvent>({
      filteredRowCount: 0,
      unfilteredRowCount: 0,
    })

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

        <Card>
          <CardHeader>
            <T id="hakutilannenäkymä__otsikko" />
            {data && (
              <NumberCounter
                value={counters.filteredRowCount}
                max={counters.unfilteredRowCount}
              />
            )}
          </CardHeader>{" "}
          <ConstrainedCardBody>
            {isLoading && <Spinner />}
            {data && (
              <NivelvaiheenHakutilanneTable
                data={data}
                organisaatioOid={organisaatioOid}
                onCountChange={setCounters}
                onSelect={() => {} /*setSelectedOppijaOids*/}
                onSetMuuHaku={setMuuHaku}
              />
            )}
            {errors !== undefined && <ApiErrors errors={errors} />}
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
