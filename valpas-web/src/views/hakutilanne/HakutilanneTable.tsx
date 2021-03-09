import React, { useMemo } from "react"
import { Link } from "react-router-dom"
import { DataTable, Datum, Value } from "../../components/tables/DataTable"
import { getLocalized, t } from "../../i18n/i18n"
import { useBasePath } from "../../state/basePath"
import { ValintatietotilaKoodistoviite } from "../../state/koodistot"
import { Haku, Oppija, Valintatieto } from "../../state/oppijat"
import { formatNullableDate } from "../../utils/date"

export type HakutilanneTableProps = {
  data: Oppija[]
}

export const HakutilanneTable = (props: HakutilanneTableProps) => {
  const basePath = useBasePath()
  const data = useMemo(() => props.data.map(oppijaToTableData(basePath)), [
    props.data,
  ])

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

const oppijaToTableData = (basePath: string) => (oppija: Oppija): Datum => {
  // TODO: Näihin molempiin tarvitaaan rautaisempi logiikka
  const hakemus = oppija?.haut?.[0]
  const opiskeluoikeudet = oppija.opiskeluoikeudet[0]
  const henkilö = oppija.henkilö

  return {
    key: henkilö.oid,
    values: [
      {
        value: `${henkilö.sukunimi} ${henkilö.etunimet}`,
        display: (
          <Link to={`${basePath}/oppijat/${henkilö.oid}`}>
            {henkilö.sukunimi} {henkilö.etunimet}
          </Link>
        ),
      },
      {
        value:
          opiskeluoikeudet && getLocalized(opiskeluoikeudet.oppilaitos.nimi),
      },
      {
        value: henkilö.syntymäaika,
        display: formatNullableDate(henkilö.syntymäaika),
      },
      {
        value: opiskeluoikeudet?.ryhmä,
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
  return t(
    hakemus ? "hakemuksentila__aktiivinen" : "hakemuksentila__ei_hakemusta"
  )
}

const valintatietoValue = (hakemus?: Haku): Value => {
  // TODO: Oikea toteutus
  const valintatieto = hakemus?.hakutoiveet[0]
  return valintatieto
    ? {
        value: formatHyvaksyttyValintatietoValue(
          valintatieto.hakutoivenumero,
          t("valintatieto__hakukohde_lc")
        ),
        display: formatHyvaksyttyValintatietoValue(
          valintatieto.hakutoivenumero,
          getLocalized(valintatieto.hakukohdeNimi)
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
  _hakemus: Haku | undefined,
  _predicate: (valintatieto: Valintatieto) => boolean
): Value => {
  const nullValue = { value: t("Ei"), display: "-" }
  // if (!hakemus) {
  return nullValue
  // }
  // const valintatiedot = hakemus.valintatiedot.filter(predicate)
  // switch (valintatiedot.length) {
  //   case 0:
  //     return nullValue
  //   case 1:
  //     const valinta = valintatiedot[0]!!
  //     return {
  //       value: t("Kyllä"),
  //       display: `${
  //         valinta.hakukohdenumero ? `${valinta.hakukohdenumero}. ` : ""
  //       }${getLocalized(valinta.hakukohde.nimi)}`,
  //     }
  //   default:
  //     return {
  //       value: t("Kyllä"),
  //       display: t("vastaanotettu__n_paikkaa", {
  //         lukumäärä: valintatiedot.length,
  //       }),
  //     }
  // }
}
