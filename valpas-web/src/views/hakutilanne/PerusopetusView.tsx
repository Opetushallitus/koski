import React from "react"
import { fetchOppijat, fetchOppijatCache } from "../../api/api"
import { useApiWithParams } from "../../api/apiHooks"
import { isSuccess } from "../../api/apiUtils"
import { Card, CardBody, CardHeader } from "../../components/containers/cards"
import { Counter } from "../../components/typography/Counter"
import { T } from "../../i18n/i18n"
import { OrganisaatioJaKayttooikeusrooli } from "../../state/types"
import { currentYear } from "../../utils/date"
import { HakutilanneTable } from "./HakutilanneTable"

export type PerusopetusViewProps = {
  kayttooikeusroolit: OrganisaatioJaKayttooikeusrooli[]
}

export const PerusopetusView = (props: PerusopetusViewProps) => {
  const organisaatiot = props.kayttooikeusroolit.map(
    (kayttooikeus) => kayttooikeus.organisaatioHierarkia
  )
  const organisaatioOid = organisaatiot[0]?.oid

  const oppijatFetch = useApiWithParams(
    fetchOppijat,
    organisaatioOid ? [organisaatioOid] : undefined,
    fetchOppijatCache
  )

  return (
    <Card>
      <CardHeader>
        <T id="perusopetusnäkymä__otsikko" params={{ vuosi: currentYear() }} />
        {isSuccess(oppijatFetch) && (
          <Counter>{oppijatFetch.data.length}</Counter>
        )}
      </CardHeader>
      <CardBody>
        {isSuccess(oppijatFetch) && (
          <HakutilanneTable data={oppijatFetch.data} />
        )}
      </CardBody>
    </Card>
  )
}
