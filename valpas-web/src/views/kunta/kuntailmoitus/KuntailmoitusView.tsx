import bem from "bem-ts"
import * as A from "fp-ts/Array"
import * as Eq from "fp-ts/Eq"
import { pipe } from "fp-ts/function"
import * as Ord from "fp-ts/Ord"
import * as string from "fp-ts/string"
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
import { Dropdown } from "../../../components/forms/Dropdown"
import { Spinner } from "../../../components/icons/Spinner"
import { DataTableCountChangeEvent } from "../../../components/tables/DataTable"
import { Counter } from "../../../components/typography/Counter"
import { NoDataMessage } from "../../../components/typography/NoDataMessage"
import { getLocalized, t, T } from "../../../i18n/i18n"
import {
  useOrganisaatiotJaKäyttöoikeusroolit,
  withRequiresKuntavalvonta,
} from "../../../state/accessRights"
import { useBasePath } from "../../../state/basePath"
import {
  Oid,
  OrganisaatioHierarkia,
  OrganisaatioJaKayttooikeusrooli,
} from "../../../state/common"
import { createKuntailmoitusPathWithOrg } from "../../../state/paths"
import { ErrorView } from "../../ErrorView"
import { KuntaNavigation } from "../KuntaNavigation"
import { KuntailmoitusTable } from "./KuntailmoitusTable"

const b = bem("kuntailmoitusview")

export const KuntailmoitusViewWithoutOrgOid = withRequiresKuntavalvonta(() => {
  const basePath = useBasePath()

  const organisaatiotJaKäyttöoikeusroolit = useOrganisaatiotJaKäyttöoikeusroolit()
  const organisaatiot = useMemo(
    () => getOrganisaatiot(organisaatiotJaKäyttöoikeusroolit),
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
      () => getOrganisaatiot(organisaatiotJaKäyttöoikeusroolit),
      [organisaatiotJaKäyttöoikeusroolit]
    )

    const orgOptions = getOrgOptions(organisaatiot)

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
        <Dropdown
          selectorId="organisaatiovalitsin"
          containerClassName={b("organisaatiovalitsin")}
          label={t("Kunta")}
          options={orgOptions}
          value={organisaatioOid}
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

// TODO: Copypastea - korvataan paremmalla organisaatiovalitsimella myöhemmin
const getOrganisaatiot = (
  kayttooikeusroolit: OrganisaatioJaKayttooikeusrooli[]
): OrganisaatioHierarkia[] => {
  const kuntaKäyttöoikeusRoolit = kayttooikeusroolit.filter(
    (kayttooikeusrooli) => kayttooikeusrooli.kayttooikeusrooli == "KUNTA"
  )
  const kaikki = pipe(
    kuntaKäyttöoikeusRoolit,
    A.map((käyttöoikeus) =>
      getOrganisaatiotHierarkiastaRecur([käyttöoikeus.organisaatioHierarkia])
    ),
    A.flatten
  )

  return pipe(
    kaikki,
    A.filter(
      (organisaatioHierarkia) =>
        organisaatioHierarkia.organisaatiotyypit.includes("KUNTA") &&
        organisaatioHierarkia.aktiivinen
    ),
    A.sortBy([byLocalizedNimi])
  )
}
const byLocalizedNimi = pipe(
  string.Ord,
  Ord.contramap(
    (organisaatioHierarkia: OrganisaatioHierarkia) =>
      `${getLocalized(organisaatioHierarkia.nimi)}`
  )
)
const getOrganisaatiotHierarkiastaRecur = (
  organisaatioHierarkiat: OrganisaatioHierarkia[]
): OrganisaatioHierarkia[] => {
  if (!organisaatioHierarkiat.length) {
    return []
  } else {
    const lapset = pipe(
      organisaatioHierarkiat,
      A.map((organisaatioHierarkia) =>
        getOrganisaatiotHierarkiastaRecur(organisaatioHierarkia.children)
      ),
      A.flatten,
      A.map(removeChildren)
    )

    return organisaatioHierarkiat.map(removeChildren).concat(lapset)
  }
}
const removeChildren = (
  organisaatioHierarkia: OrganisaatioHierarkia
): OrganisaatioHierarkia => ({
  ...organisaatioHierarkia,
  children: [],
})

const eqOrgs = Eq.fromEquals(
  (a: OrganisaatioHierarkia, b: OrganisaatioHierarkia) => a.oid === b.oid
)

const getOrgOptions = (orgs: OrganisaatioHierarkia[]) =>
  pipe(
    orgs,
    A.uniq(eqOrgs),
    A.map((org: OrganisaatioHierarkia) => ({
      value: org.oid,
      display: `${getLocalized(org.nimi)} (${org.oid})`,
    }))
  )
