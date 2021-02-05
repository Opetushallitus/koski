import React from "react"
import { fetchOppijat, fetchOppijatCache } from "../../api/api"
import { useApiOnce } from "../../api/apiHooks"
import { isSuccess } from "../../api/apiUtils"
import { Card, CardBody, CardHeader } from "../../components/containers/cards"
import { Counter } from "../../components/typography/Counter"
import { T } from "../../i18n/i18n"
import { currentYear } from "../../utils/date"
import { HakutilanneTable } from "./HakutilanneTable"

export type PerusopetusViewProps = {}

export const PerusopetusView = (_props: PerusopetusViewProps) => {
  const oppijatFetch = useApiOnce(fetchOppijat, fetchOppijatCache)

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
