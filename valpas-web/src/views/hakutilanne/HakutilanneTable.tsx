import React, { useMemo } from "react"
import { WarningIcon } from "../../components/icons/Icon"
import { DataTable, Datum, Value } from "../../components/tables/DataTable"
import { getLocalized, t } from "../../i18n/i18n"
import {
  Hakemuksentila,
  Oppija,
  Oppilaitos,
  Valintatieto,
} from "../../state/oppijat"
import { formatDate } from "../../utils/date"

export type HakutilanneTableProps = {
  data: Oppija[]
}

export const HakutilanneTable = (props: HakutilanneTableProps) => {
  const data = useMemo(() => props.data.map(oppijaToTableData), [props.data])

  return (
    <DataTable
      className="hakutilanne"
      columns={[
        {
          label: t("hakutilanne__taulu_nimi"),
          filter: "freetext",
        },
        {
          label: t("hakutilanne__taulu_oppilaitos"),
          filter: "dropdown",
        },
        {
          label: t("hakutilanne__taulu_syntymäaika"),
        },
        {
          label: t("hakutilanne__taulu_luokka"),
          filter: "dropdown",
        },
        {
          label: t("hakutilanne__taulu_hakemuksen_tila"),
          filter: "dropdown",
        },
        {
          label: t("hakutilanne__taulu_valintatieto"),
          filter: "dropdown",
        },
        {
          label: t("hakutilanne__taulu_opiskelupaikka_vastaanotettu"),
          filter: "dropdown",
        },
        {
          label: t("hakutilanne__taulu_läsnä_oppilaitoksessa"),
          filter: "dropdown",
        },
      ]}
      data={data}
    />
  )
}

const oppijaToTableData = (oppija: Oppija): Datum => ({
  key: oppija.oid,
  values: [
    {
      value: oppija.nimi,
    },
    {
      value: getLocalized(oppija.oppilaitos.nimi),
    },
    {
      value: oppija.syntymaaika,
      display: formatDate(oppija.syntymaaika),
    },
    {
      value: oppija.luokka,
    },
    {
      value: hakemuksentilaValue(oppija.hakemuksentila),
    },
    valintatietoValue(oppija.valintatiedot),
    oppilaitosValue(oppija.vastaanotetut),
    oppilaitosValue(oppija.lasna),
  ],
})

const hakemuksentilaValue = (hakemuksentila: Hakemuksentila): string => {
  switch (hakemuksentila.tila) {
    case "ei":
      return t("hakemuksentila__ei_hakemusta")
    case "aktiivinen":
      return t("hakemuksentila__aktiivinen")
    case "passiivinen":
      return t("hakemuksentila__passiivinen")
    case "luonnos":
      return t("hakemuksentila__luonnos")
    case "puutteellinen":
      return t("hakemuksentila__puutteellinen")
  }
}

const valintatietoValue = (valintatiedot: Valintatieto[]): Value => {
  if (valintatiedot.length === 0) {
    return {
      value: t("valintatieto__ei_opiskelupaikkaa"),
      icon: <WarningIcon />,
    }
  }
  if (valintatiedot.every((vt) => vt.tila === "varasija")) {
    return {
      value: t("valintatieto__varasija"),
    }
  }
  const shownValintatieto = valintatiedot[0]!! // TODO: sort?
  return {
    value: formatHyvaksyttyValintatietoValue(shownValintatieto.hakukohdenumero),
    display: formatHyvaksyttyValintatietoValue(
      shownValintatieto.hakukohdenumero,
      getLocalized(shownValintatieto.hakukohde.nimi)
    ),
  }
}

const formatHyvaksyttyValintatietoValue = (
  hakukohdenumero?: number,
  nimi?: string
) =>
  t("valintatieto__hyväksytty", {
    hakukohde:
      (hakukohdenumero ? `${hakukohdenumero}. ` : "") +
      (nimi || t("valintatieto__hakukohde_lc")),
  })

const oppilaitosValue = (vastaanotetut: Oppilaitos[]): Value => {
  switch (vastaanotetut.length) {
    case 0:
      return { value: t("Ei"), display: "-" }
    case 1:
      return {
        value: t("Kyllä"),
        display: getLocalized(vastaanotetut[0]!!.nimi),
      }
    default:
      return {
        value: t("Kyllä"),
        display: t("vastaanotettu__n_paikkaa", {
          lukumäärä: vastaanotetut.length,
        }),
      }
  }
}
