import React from "react"
import { SelectableDataTableProps } from "../../components/tables/SelectableDataTable"
import { OppijaHakutilanteillaSuppeatTiedot } from "../../state/apitypes/oppija"
import { Oid } from "../../state/common"
import { SetMuuHakuCallback } from "./HakutilanneTable"

export type NivelvaiheenHakutilanneTableProps = {
  data: OppijaHakutilanteillaSuppeatTiedot[]
  organisaatioOid: string
  onSelect: (oppijaOids: Oid[]) => void
  onSetMuuHaku: SetMuuHakuCallback
} & Pick<SelectableDataTableProps, "onCountChange">

export const NivelvaiheenHakutilanneTable = (
  props: NivelvaiheenHakutilanneTableProps
) => <div>TODO: Hakutilanne table ({props.data.length} rows)</div>
