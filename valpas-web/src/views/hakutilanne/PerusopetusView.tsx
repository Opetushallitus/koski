import bem from "bem-ts"
import * as A from "fp-ts/Array"
import * as Eq from "fp-ts/Eq"
import { pipe } from "fp-ts/lib/function"
import React, { useState } from "react"
import { fetchOppijat, fetchOppijatCache } from "../../api/api"
import { useApiWithParams } from "../../api/apiHooks"
import { isLoading, isSuccess } from "../../api/apiUtils"
import { Card, CardBody, CardHeader } from "../../components/containers/cards"
import { Dropdown } from "../../components/forms/Dropdown"
import { Spinner } from "../../components/icons/Spinner"
import { Counter } from "../../components/typography/Counter"
import { getLocalized, t, T } from "../../i18n/i18n"
import {
  OrganisaatioHierarkia,
  OrganisaatioJaKayttooikeusrooli,
} from "../../state/types"
import { currentYear } from "../../utils/date"
import { HakutilanneTable } from "./HakutilanneTable"
import "./PerusopetusView.less"
import { VirkailijaNavigation } from "./VirkailijaNavigation"

const b = bem("perusopetusview")

export type PerusopetusViewProps = {
  kayttooikeusroolit: OrganisaatioJaKayttooikeusrooli[]
}

export const PerusopetusView = (props: PerusopetusViewProps) => {
  const organisaatiot = props.kayttooikeusroolit.map(
    (kayttooikeus) => kayttooikeus.organisaatioHierarkia
  )
  const [organisaatioOid, setOrganisaatioOid] = useState(organisaatiot[0]?.oid)
  const oppijatFetch = useApiWithParams(
    fetchOppijat,
    organisaatioOid ? [organisaatioOid] : undefined,
    fetchOppijatCache
  )

  const orgOptions = getOrgOptions(organisaatiot)

  return (
    <>
      <Dropdown
        selectorId="organisaatiovalitsin"
        containerClassName={b("organisaatiovalitsin")}
        label={t("Oppilaitos")}
        options={orgOptions}
        value={organisaatioOid || ""}
        onChange={setOrganisaatioOid}
      />
      <VirkailijaNavigation />
      <Card>
        <CardHeader>
          <T
            id="perusopetusnäkymä__otsikko"
            params={{ vuosi: currentYear() }}
          />
          {isSuccess(oppijatFetch) && (
            <Counter>
              {
                A.flatten(
                  oppijatFetch.data.map(
                    (d) => d.oppija.valvottavatOpiskeluoikeudet
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
  )
}

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
