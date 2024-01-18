import { useMemo, useState } from "react"
import { OppijaHakutilanteillaSuppeatTiedot } from "../../state/apitypes/oppija"
import { Oid } from "../../state/common"
import { nonNull } from "../../utils/arrays"

export const useOppijaSelect = (
  data: OppijaHakutilanteillaSuppeatTiedot[] | null,
) => {
  const [selectedOppijaOids, setSelectedOppijaOids] = useState<Oid[]>([])
  const selectedOppijat = useMemo(
    () =>
      data
        ? selectedOppijaOids
            .map((oid) => data.find((o) => o.oppija.henkilö.oid === oid))
            .filter(nonNull)
        : [],
    [data, selectedOppijaOids],
  )

  const result = useMemo(
    () => ({ setSelectedOppijaOids, selectedOppijat }),
    [selectedOppijat],
  )

  return result
}
