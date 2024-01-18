import React, { useMemo, useState } from "react"
import { useHistory } from "react-router-dom"
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
import { t, T } from "../../i18n/i18n"
import { useOrganisaatiotJaKäyttöoikeusroolit } from "../../state/accessRights"
import { useBasePath } from "../../state/basePath"
import { Kayttooikeusrooli, Oid } from "../../state/common"
import { UseOppijatDataApi } from "../hakutilanne/useOppijatData"
import { OppijaViewBackNavProps } from "../oppija/OppijaView"
import { KunnalleIlmoitetutTable } from "./KunnalleIlmoitetutTable"

export type KunnalleIlmoitetutViewProps = {
  organisaatioOid: Oid
  organisaatioTyyppi: string
  organisaatioHakuRooli: Kayttooikeusrooli
  dataFetcher: (organisaatioOid?: Oid) => UseOppijatDataApi
  backRefName: keyof OppijaViewBackNavProps
  storageName: string
  navigation?: React.ReactNode
  linkCreator: (basePath: string, props: { organisaatioOid: Oid }) => string
}

export const KunnalleIlmoitetutView = (props: KunnalleIlmoitetutViewProps) => {
  const basePath = useBasePath()
  const organisaatiotJaKäyttöoikeusroolit =
    useOrganisaatiotJaKäyttöoikeusroolit()
  const organisaatiot = useMemo(
    () =>
      getOrganisaatiot(
        organisaatiotJaKäyttöoikeusroolit,
        props.organisaatioHakuRooli,
        props.organisaatioTyyppi,
      ),
    [
      organisaatiotJaKäyttöoikeusroolit,
      props.organisaatioHakuRooli,
      props.organisaatioTyyppi,
    ],
  )

  const history = useHistory()
  const changeOrganisaatio = (oid?: Oid) => {
    if (oid) {
      history.push(props.linkCreator(basePath, { organisaatioOid: oid }))
    }
  }

  const organisaatioOid = props.organisaatioOid
  const { data, isLoading, errors } = props.dataFetcher(organisaatioOid)

  const [counters, setCounters] = useState<DataTableCountChangeEvent>({
    filteredRowCount: 0,
    unfilteredRowCount: 0,
  })

  return (
    <Page>
      <OrganisaatioValitsin
        organisaatioTyyppi={props.organisaatioTyyppi}
        organisaatioHierarkia={organisaatiot}
        valittuOrganisaatioOid={props.organisaatioOid}
        label={t("Oppilaitos")}
        onChange={changeOrganisaatio}
      />
      {props.navigation}
      <Card>
        <CardHeader>
          <T id="kunnalleilmoitetut_otsikko" />{" "}
          {data && (
            <NumberCounter
              value={counters.filteredRowCount}
              max={counters.unfilteredRowCount}
            />
          )}
        </CardHeader>
        <ConstrainedCardBody>
          {isLoading && <Spinner />}
          {data && (
            <KunnalleIlmoitetutTable
              data={data}
              organisaatioOid={organisaatioOid}
              backRefName={props.backRefName}
              onCountChange={setCounters}
              storageName={`${props.storageName}-${organisaatioOid}`}
            />
          )}
          {errors !== undefined && <ApiErrors errors={errors} />}
        </ConstrainedCardBody>
      </Card>
    </Page>
  )
}
