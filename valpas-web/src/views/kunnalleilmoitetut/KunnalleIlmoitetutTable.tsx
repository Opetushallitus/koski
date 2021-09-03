import React, { useMemo } from "react"
import {
  Column,
  DataTable,
  Datum,
  Value,
} from "../../components/tables/DataTable"
import { t } from "../../i18n/i18n"
import { OppijaHakutilanteillaSuppeatTiedot } from "../../state/apitypes/oppija"
import { formatNullableDate } from "../../utils/date"

export type KunnalleIlmoitetutTableProps = {
  data: OppijaHakutilanteillaSuppeatTiedot[]
}

export const KunnalleIlmoitetutTable = (
  props: KunnalleIlmoitetutTableProps
) => {
  const columns: Column[] = useMemo(
    () => [
      {
        label: t("kunnalleilmoitetut_nimi"),
        filter: "freetext",
      },
      {
        label: t("kunnalleilmoitetut_syntymäaika"),
      },
      {
        label: t("kunnalleilmoitetut_ilmoitettu_kunnalle"),
      },
      {
        label: t("kunnalleilmoitetut_ilmoituksen_tekopäivä"),
      },
      {
        label: t("kunnalleilmoitetut_muu_haku"),
      },
    ],
    []
  )

  const data = toTableData(props.data)

  return <DataTable columns={columns} data={data} />
}

const toTableData = (data: OppijaHakutilanteillaSuppeatTiedot[]): Datum[] =>
  data.map((d) => ({
    key: [d.oppija.henkilö.oid],
    values: [
      oppijanNimi(d),
      syntymäaika(d),
      ilmoitettuKunnalle(d),
      ilmoituksenTekopäivä(d),
      muuHaku(d),
    ],
  }))

const oppijanNimi = (oppija: OppijaHakutilanteillaSuppeatTiedot): Value => ({
  value: `${oppija.oppija.henkilö.sukunimi} ${oppija.oppija.henkilö.etunimet}`,
})

const syntymäaika = (oppija: OppijaHakutilanteillaSuppeatTiedot): Value => ({
  value: oppija.oppija.henkilö.syntymäaika,
  display: formatNullableDate(oppija.oppija.henkilö.syntymäaika),
})

const ilmoitettuKunnalle = (
  _oppija: OppijaHakutilanteillaSuppeatTiedot
): Value => ({
  value: "TODO", // TODO: Tarvitaan datatyypppi, jolla on ilmoitukset
})

const ilmoituksenTekopäivä = (
  _oppija: OppijaHakutilanteillaSuppeatTiedot
): Value => ({
  value: "TODO", // TODO: Tarvitaan datatyypppi, jolla on ilmoitukset
})

const muuHaku = (oppija: OppijaHakutilanteillaSuppeatTiedot): Value => ({
  value:
    oppija.lisätiedot.find((t) => t.muuHaku) !== undefined
      ? t("Kyllä")
      : t("Ei"),
})
