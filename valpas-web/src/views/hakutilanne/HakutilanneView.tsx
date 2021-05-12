import bem from "bem-ts"
import * as A from "fp-ts/Array"
import * as Eq from "fp-ts/Eq"
import { pipe } from "fp-ts/lib/function"
import * as Ord from "fp-ts/Ord"
import * as string from "fp-ts/string"
import React, { useMemo, useState } from "react"
import { Redirect, useHistory } from "react-router"
import { fetchOppijat, fetchOppijatCache } from "../../api/api"
import { useApiWithParams } from "../../api/apiHooks"
import { isLoading, isSuccess } from "../../api/apiUtils"
import { Card, CardBody, CardHeader } from "../../components/containers/cards"
import { Dropdown } from "../../components/forms/Dropdown"
import { Spinner } from "../../components/icons/Spinner"
import { DataTableCountChangeEvent } from "../../components/tables/DataTable"
import { Counter } from "../../components/typography/Counter"
import { getLocalized, t, T } from "../../i18n/i18n"
import { withRequiresHakeutumisenValvonta } from "../../state/accessRights"
import { useBasePath } from "../../state/basePath"
import {
  Oid,
  OrganisaatioHierarkia,
  OrganisaatioJaKayttooikeusrooli,
} from "../../state/common"
import { isFeatureFlagEnabled } from "../../state/featureFlags"
import {
  createHakutilannePathWithOrg,
  HakutilanneViewRouteProps,
} from "../../state/paths"
import { nonNull } from "../../utils/arrays"
import { ErrorView } from "../ErrorView"
import { HakutilanneDrawer } from "./HakutilanneDrawer"
import { HakutilanneTable } from "./HakutilanneTable"
import "./HakutilanneView.less"
import { VirkailijaNavigation } from "./VirkailijaNavigation"

const b = bem("hakutilanneview")

export type HakutilanneViewProps = HakutilanneViewRouteProps & {
  kayttooikeusroolit: OrganisaatioJaKayttooikeusrooli[]
}

export const HakutilanneViewWithoutOrgOid = withRequiresHakeutumisenValvonta(
  (props: HakutilanneViewProps) => {
    const basePath = useBasePath()
    const organisaatio = getOrganisaatiot(props.kayttooikeusroolit)[0]

    return organisaatio ? (
      <Redirect
        to={createHakutilannePathWithOrg(basePath, {
          organisaatioOid: organisaatio.oid,
        })}
      />
    ) : (
      <OrganisaatioMissingView />
    )
  }
)

export const HakutilanneView = withRequiresHakeutumisenValvonta(
  (props: HakutilanneViewProps) => {
    const history = useHistory()
    const organisaatiot = useMemo(
      () => getOrganisaatiot(props.kayttooikeusroolit),
      [props.kayttooikeusroolit]
    )

    const organisaatioOid =
      props.match.params.organisaatioOid || organisaatiot[0]?.oid
    const oppijatFetch = useApiWithParams(
      fetchOppijat,
      organisaatioOid ? [organisaatioOid] : undefined,
      fetchOppijatCache
    )
    const [counters, setCounters] = useState<DataTableCountChangeEvent>({
      filteredRowCount: 0,
      unfilteredRowCount: 0,
    })
    const [selectedOppijaOids, setSelectedOppijaOids] = useState<Oid[]>([])

    const orgOptions = getOrgOptions(organisaatiot)

    const changeOrganisaatio = (oid?: Oid) => {
      if (oid) {
        history.push(oid)
      }
    }

    const selectedOppijat = useMemo(
      () =>
        isSuccess(oppijatFetch)
          ? selectedOppijaOids
              .map((oid) =>
                oppijatFetch.data.find((o) => o.oppija.henkilö.oid === oid)
              )
              .filter(nonNull)
          : [],
      [oppijatFetch, selectedOppijaOids]
    )

    return organisaatioOid ? (
      <div className={b("view")}>
        <Dropdown
          selectorId="organisaatiovalitsin"
          containerClassName={b("organisaatiovalitsin")}
          label={t("Oppilaitos")}
          options={orgOptions}
          value={organisaatioOid || ""}
          onChange={changeOrganisaatio}
        />
        <VirkailijaNavigation />
        <Card>
          <CardHeader>
            <T id="hakutilannenäkymä__otsikko" />
            {isSuccess(oppijatFetch) && (
              <Counter>
                {counters.filteredRowCount === counters.unfilteredRowCount
                  ? counters.filteredRowCount
                  : `${counters.filteredRowCount} / ${counters.unfilteredRowCount}`}
              </Counter>
            )}
          </CardHeader>
          <CardBody>
            {isLoading(oppijatFetch) && <Spinner />}
            {isSuccess(oppijatFetch) && (
              <HakutilanneTable
                data={oppijatFetch.data}
                organisaatioOid={organisaatioOid}
                onCountChange={setCounters}
                onSelect={setSelectedOppijaOids}
              />
            )}
          </CardBody>
        </Card>
        {isFeatureFlagEnabled("ilmoittaminen") ? (
          <HakutilanneDrawer
            selectedOppijat={selectedOppijat}
            tekijäOrganisaatio={organisaatioOid}
          />
        ) : null}
      </div>
    ) : (
      <OrganisaatioMissingView />
    )
  }
)

const getOrganisaatiot = (
  kayttooikeusroolit: OrganisaatioJaKayttooikeusrooli[]
): OrganisaatioHierarkia[] => {
  const hakeutumisKayttooikeusroolit = kayttooikeusroolit.filter(
    (kayttooikeusrooli) =>
      kayttooikeusrooli.kayttooikeusrooli == "OPPILAITOS_HAKEUTUMINEN"
  )
  const kaikki = pipe(
    hakeutumisKayttooikeusroolit,
    A.map((kayttooikeus) =>
      getOrganisaatiotHierarkiastaRecur([kayttooikeus.organisaatioHierarkia])
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

const OrganisaatioMissingView = () => (
  <ErrorView
    title={t("hakutilanne__ei_oikeuksia_title")}
    message={t("hakutilanne__ei_oikeuksia_teksti")}
  />
)
