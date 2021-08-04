import bem from "bem-ts"
import * as A from "fp-ts/Array"
import * as Eq from "fp-ts/Eq"
import { pipe } from "fp-ts/function"
import * as Ord from "fp-ts/Ord"
import * as string from "fp-ts/string"
import React, { useMemo, useState } from "react"
import { Redirect, RouteComponentProps, useHistory } from "react-router"
import { Counter } from "~components/typography/Counter"
import { isFeatureFlagEnabled } from "~state/featureFlags"
import { createSuorittaminenPathWithOrg } from "~state/paths"
import { SuorittaminenHetuhaku } from "~views/suorittaminen/hetuhaku/SuorittaminenHetuhaku"
import { SuorittaminenOppivelvollisetTable } from "~views/suorittaminen/oppivelvolliset/SuorittaminenOppivelvollisetTable"
import { SuorittaminenNavigation } from "~views/suorittaminen/SuorittaminenNavigation"
import {
  fetchOppijatSuorittaminen,
  fetchOppijatSuorittaminenCache,
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
import { NoDataMessage } from "../../../components/typography/NoDataMessage"
import { getLocalized, t, T } from "../../../i18n/i18n"
import {
  useOrganisaatiotJaKäyttöoikeusroolit,
  withRequiresSuorittamisenValvonta,
} from "../../../state/accessRights"
import { useBasePath } from "../../../state/basePath"
import {
  Oid,
  OrganisaatioHierarkia,
  OrganisaatioJaKayttooikeusrooli,
} from "../../../state/common"
import { ErrorView } from "../../ErrorView"

const b = bem("suorittaminenoppivelvollisetview")

export const SuorittaminenOppivelvollisetViewWithoutOrgOid = withRequiresSuorittamisenValvonta(
  () => {
    const basePath = useBasePath()

    const organisaatiotJaKäyttöoikeusroolit = useOrganisaatiotJaKäyttöoikeusroolit()
    const organisaatiot = useMemo(
      () => getOrganisaatiot(organisaatiotJaKäyttöoikeusroolit),
      [organisaatiotJaKäyttöoikeusroolit]
    )
    const organisaatio = organisaatiot[0]

    return (
      <>
        {isFeatureFlagEnabled("suorittamisenvalvontalista") ? (
          organisaatio ? (
            <Redirect
              to={createSuorittaminenPathWithOrg(basePath, organisaatio.oid)}
            />
          ) : (
            <OrganisaatioMissingView />
          )
        ) : (
          <SuorittaminenHetuhaku />
        )}
      </>
    )
  }
)

export type SuorittaminenOppivelvollisetViewProps = RouteComponentProps<{
  organisaatioOid?: string
}>

export const SuorittaminenOppivelvollisetView = withRequiresSuorittamisenValvonta(
  (props: SuorittaminenOppivelvollisetViewProps) => {
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

    // TODO, käytä dummy dataa eikä väärästä routesta haettua
    const fetch = useApiWithParams(
      fetchOppijatSuorittaminen,
      [organisaatioOid],
      fetchOppijatSuorittaminenCache
    )

    return (
      <>
        {isFeatureFlagEnabled("suorittamisenvalvontalista") ? (
          <Page>
            <Dropdown
              selectorId="organisaatiovalitsin"
              containerClassName={b("organisaatiovalitsin")}
              label={t("Oppilaitos")}
              options={orgOptions}
              value={organisaatioOid}
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
              <CardBody>
                {isLoading(fetch) && <Spinner />}
                {isSuccess(fetch) && fetch.data.length == 0 && (
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
              </CardBody>
            </Card>
          </Page>
        ) : (
          <SuorittaminenHetuhaku />
        )}
      </>
    )
  }
)

const OrganisaatioMissingView = () => (
  <ErrorView
    title={t("suorittaminennäkymä__ei_oikeuksia_title")}
    message={t("suorittaminennäkymä__ei_oikeuksia_teksti")}
    head={<SuorittaminenNavigation />}
  />
)

// TODO: Copypastea - korvataan paremmalla organisaatiovalitsimella myöhemmin
const getOrganisaatiot = (
  kayttooikeusroolit: OrganisaatioJaKayttooikeusrooli[]
): OrganisaatioHierarkia[] => {
  const suorittaminenKäyttöoikeusRoolit = kayttooikeusroolit.filter(
    (kayttooikeusrooli) =>
      kayttooikeusrooli.kayttooikeusrooli == "OPPILAITOS_SUORITTAMINEN"
  )
  const kaikki = pipe(
    suorittaminenKäyttöoikeusRoolit,
    A.map((käyttöoikeus) =>
      getOrganisaatiotHierarkiastaRecur([käyttöoikeus.organisaatioHierarkia])
    ),
    A.flatten
  )

  return pipe(
    kaikki,
    A.filter(
      (organisaatioHierarkia) =>
        organisaatioHierarkia.organisaatiotyypit.includes("OPPILAITOS") &&
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
