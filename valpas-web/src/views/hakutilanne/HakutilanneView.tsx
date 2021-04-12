import bem from "bem-ts"
import * as A from "fp-ts/Array"
import * as Eq from "fp-ts/Eq"
import { pipe } from "fp-ts/lib/function"
import React from "react"
import { Redirect, useHistory } from "react-router"
import { fetchOppijat, fetchOppijatCache } from "../../api/api"
import { useApiWithParams } from "../../api/apiHooks"
import { isLoading, isSuccess } from "../../api/apiUtils"
import { Card, CardBody, CardHeader } from "../../components/containers/cards"
import { Dropdown } from "../../components/forms/Dropdown"
import { Spinner } from "../../components/icons/Spinner"
import { Counter } from "../../components/typography/Counter"
import { getLocalized, t, T } from "../../i18n/i18n"
import { useBasePath } from "../../state/basePath"
import { valvottavatOpiskeluoikeudet } from "../../state/oppijat"
import {
  createHakutilannePathWithOrg,
  HakutilanneViewRouteProps,
} from "../../state/paths"
import {
  Oid,
  OrganisaatioHierarkia,
  OrganisaatioJaKayttooikeusrooli,
} from "../../state/types"
import { ErrorView } from "../ErrorView"
import { HakutilanneTable } from "./HakutilanneTable"
import "./HakutilanneView.less"
import { VirkailijaNavigation } from "./VirkailijaNavigation"

const b = bem("hakutilanneview")

export type HakutilanneViewProps = HakutilanneViewRouteProps & {
  kayttooikeusroolit: OrganisaatioJaKayttooikeusrooli[]
}

export const HakutilanneViewWithoutOrgOid = (props: HakutilanneViewProps) => {
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

export const HakutilanneView = (props: HakutilanneViewProps) => {
  const history = useHistory()
  const organisaatiot = getOrganisaatiot(props.kayttooikeusroolit)
  const organisaatioOid =
    props.match.params.organisaatioOid || organisaatiot[0]?.oid
  const oppijatFetch = useApiWithParams(
    fetchOppijat,
    organisaatioOid ? [organisaatioOid] : undefined,
    fetchOppijatCache
  )

  const orgOptions = getOrgOptions(organisaatiot)

  const changeOrganisaatio = (oid?: Oid) => {
    if (oid) {
      history.push(oid)
    }
  }

  return organisaatioOid ? (
    <>
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
              {
                A.flatten(
                  oppijatFetch.data.map((d) =>
                    valvottavatOpiskeluoikeudet(
                      organisaatioOid,
                      d.oppija.opiskeluoikeudet
                    )
                  )
                ).length
              }
            </Counter>
          )}
        </CardHeader>
        <CardBody>
          {isLoading(oppijatFetch) && <Spinner />}
          {isSuccess(oppijatFetch) && (
            <HakutilanneTable
              data={oppijatFetch.data}
              organisaatioOid={organisaatioOid}
            />
          )}
        </CardBody>
      </Card>
    </>
  ) : (
    <OrganisaatioMissingView />
  )
}

const getOrganisaatiot = (
  kayttooikeusroolit: OrganisaatioJaKayttooikeusrooli[]
) =>
  kayttooikeusroolit.map((kayttooikeus) => kayttooikeus.organisaatioHierarkia)

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
