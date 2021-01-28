import React from "react"
import { fetchOppijat } from "../../api/api"
import { isSuccessful, useApiOnce } from "../../api/apiHooks"
import { Card, CardBody, CardHeader } from "../../components/containers/cards"
import { Counter } from "../../components/typography/Counter"
import { T } from "../../i18n/i18n"
import { currentYear } from "../../utils/date"
import { HakutilanneTable } from "./HakutilanneTable"

export type PerusopetusViewProps = {}

export const PerusopetusView = (_props: PerusopetusViewProps) => {
  const oppijatFetch = useApiOnce(fetchOppijat)

  return (
    <Card>
      <CardHeader>
        <T id="perusopetusnäkymä__otsikko" params={{ vuosi: currentYear() }} />
        {isSuccessful(oppijatFetch) && (
          <Counter>{oppijatFetch.data.length}</Counter>
        )}
      </CardHeader>
      <CardBody>
        {isSuccessful(oppijatFetch) && (
          <HakutilanneTable data={oppijatFetch.data} />
        )}
      </CardBody>
    </Card>
  )
}
