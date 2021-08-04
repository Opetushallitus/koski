import React from "react"
import { DataTableCountChangeEvent } from "../../../components/tables/DataTable"
import { OppijaHakutilanteillaSuppeatTiedot } from "../../../state/apitypes/oppija"

// TODO: Oikea data
export type SuorittaminenOppivelvollisetTableProps = {
  data: OppijaHakutilanteillaSuppeatTiedot[]
  onCountChange: (event: DataTableCountChangeEvent) => void
  organisaatioOid: string
}

export const SuorittaminenOppivelvollisetTable = (
  props: SuorittaminenOppivelvollisetTableProps
) => {
  return (
    <p>TODO {props.data && props.data.length}</p>
    //<DataTable
    //  key={props.organisaatioOid}
    //  storageName={`kuntailmoitustaulu-${props.organisaatioOid}`}
    //  className="kuntailmoitus"
    //  columns={columns}
    //  data={data}
    //  onCountChange={props.onCountChange}
    ///>
  )
}
