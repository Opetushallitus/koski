import * as A from "fp-ts/Array"
import { pipe } from "fp-ts/lib/function"
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
import { T, t } from "../../i18n/i18n"
import { useOrganisaatiotJaKäyttöoikeusroolit } from "../../state/accessRights"
import { useBasePath } from "../../state/basePath"
import { käyttöoikeusrooliEq, Oid, oppilaitosroolit } from "../../state/common"
import { kunnalleIlmoitetutPathWithOrg } from "../../state/paths"
import { containedIn } from "../../utils/arrays"
import { pluck } from "../../utils/objects"
import { useKunnalleTehdytIlmoitukset } from "../hakutilanne/useOppijatData"
import { KunnalleIlmoitetutTable } from "./KunnalleIlmoitetutTable"

export type KunnalleIlmoitetutViewProps = {
  organisaatioOid: Oid
  organisaatioTyyppi: string
}

export const KunnalleIlmoitetutView = (props: KunnalleIlmoitetutViewProps) => {
  const basePath = useBasePath()
  const organisaatiotJaKäyttöoikeusroolit =
    useOrganisaatiotJaKäyttöoikeusroolit()

  const roolit = useMemo(
    () =>
      pipe(
        organisaatiotJaKäyttöoikeusroolit,
        A.map(pluck("kayttooikeusrooli")),
        A.filter(containedIn(oppilaitosroolit)),
        A.uniq(käyttöoikeusrooliEq),
      ),
    [organisaatiotJaKäyttöoikeusroolit],
  )

  const organisaatiot = useMemo(
    () =>
      getOrganisaatiot(
        organisaatiotJaKäyttöoikeusroolit,
        roolit,
        props.organisaatioTyyppi,
      ),
    [organisaatiotJaKäyttöoikeusroolit, roolit, props.organisaatioTyyppi],
  )

  const history = useHistory()
  const changeOrganisaatio = (oid?: Oid) => {
    if (oid) {
      history.push(
        kunnalleIlmoitetutPathWithOrg.href(basePath, { organisaatioOid: oid }),
      )
    }
  }

  const organisaatioOid = props.organisaatioOid
  const { data, isLoading, errors } =
    useKunnalleTehdytIlmoitukset(organisaatioOid)

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
              onCountChange={setCounters}
              storageName={`kunnalleIlmoitetut-${organisaatioOid}`}
            />
          )}
          {errors !== undefined && <ApiErrors errors={errors} />}
        </ConstrainedCardBody>
      </Card>
    </Page>
  )
}
