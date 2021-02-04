import React, { useMemo } from "react"
import { DataTable, Datum, Value } from "../../components/tables/DataTable"
import { getLocalized, t } from "../../i18n/i18n"
import { ValintatietotilaKoodistoviite } from "../../state/koodistot"
import { Haku, Oppija, Valintatieto } from "../../state/oppijat"
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
          label: t("hakutilanne__taulu_ryhma"),
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

const oppijaToTableData = (oppija: Oppija): Datum => {
  const hakemus = oppija.haut[0] // TODO: Tähän tarvittaneen parempi logiikka...

  return {
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
        value: oppija.ryhmä,
      },
      {
        value: hakemuksentilaValue(hakemus),
      },
      valintatietoValue(hakemus),
      oppilaitosValue(hakemus, (valinta) =>
        Boolean(
          valinta.tila &&
            ValintatietotilaKoodistoviite.isVastaanotettu(valinta.tila)
        )
      ),
      oppilaitosValue(hakemus, (valinta) =>
        Boolean(
          valinta.tila && ValintatietotilaKoodistoviite.isLäsnä(valinta.tila)
        )
      ),
    ],
  }
}

const hakemuksentilaValue = (hakemus?: Haku): string => {
  if (!hakemus) {
    return t("hakemuksentila__ei_hakemusta")
  }
  switch (hakemus.tila.koodiarvo) {
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

const valintatietoValue = (hakemus?: Haku): Value => {
  const valintatieto = hakemus?.valintatiedot[0] // TODO: valitse valintatieto fiksummin, esim. pienimmällä numerolla oleva
  return valintatieto?.tila &&
    ValintatietotilaKoodistoviite.isHyväksytty(valintatieto.tila)
    ? {
        value: formatHyvaksyttyValintatietoValue(
          valintatieto.hakukohdenumero,
          t("valintatieto__hakukohde_lc")
        ),
        display: formatHyvaksyttyValintatietoValue(
          valintatieto.hakukohdenumero,
          getLocalized(valintatieto.hakukohde.nimi)
        ),
      }
    : {
        value: "-",
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

const oppilaitosValue = (
  hakemus: Haku | undefined,
  predicate: (valintatieto: Valintatieto) => boolean
): Value => {
  const nullValue = { value: t("Ei"), display: "-" }
  if (!hakemus) {
    return nullValue
  }
  const valintatiedot = hakemus.valintatiedot.filter(predicate)
  switch (valintatiedot.length) {
    case 0:
      return nullValue
    case 1:
      const valinta = valintatiedot[0]!!
      return {
        value: t("Kyllä"),
        display: `${
          valinta.hakukohdenumero ? `${valinta.hakukohdenumero}. ` : ""
        }${getLocalized(valinta.hakukohde.nimi)}`,
      }
    default:
      return {
        value: t("Kyllä"),
        display: t("vastaanotettu__n_paikkaa", {
          lukumäärä: valintatiedot.length,
        }),
      }
  }
}
