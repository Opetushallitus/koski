import React, { useMemo } from "react"
import { Link } from "react-router-dom"
import { ExternalLink } from "../../components/navigation/ExternalLink"
import { DataTable, Datum } from "../../components/tables/DataTable"
import { NoDataMessage } from "../../components/typography/NoDataMessage"
import { T, t } from "../../i18n/i18n"
import { useBasePath } from "../../state/basePath"
import { externalHakemussivu } from "../../state/externalUrls"
import { Haku, OppijaHakutilanteilla } from "../../state/oppijat"
import { formatNullableDate } from "../../utils/date"

export type HakutilanneTableProps = {
  data: OppijaHakutilanteilla[]
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
          size: "large",
        },
        {
          label: t("hakutilanne__taulu_oppilaitos"),
          filter: "dropdown",
        },
        {
          label: t("hakutilanne__taulu_syntymäaika"),
          size: "small",
        },
        {
          label: t("hakutilanne__taulu_ryhma"),
          filter: "dropdown",
          size: "xsmall",
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
          label: t("hakutilanne__taulu_voimassaolevia_opiskeluoikeuksia"),
          filter: "dropdown",
        },
      ]}
      data={data}
    />
  )
}

const oppijaToTableData = (basePath: string) => (
  oppija: OppijaHakutilanteilla
): Datum => {
  // TODO: Näihin molempiin tarvitaaan rautaisempi logiikka
  const hakemus = oppija?.haut?.[0]
  const opiskeluoikeudet = oppija.oppija.opiskeluoikeudet[0]
  const henkilö = oppija.oppija.henkilö

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
        display: hakemus && (
          <ExternalLink to={externalHakemussivu(hakemus.hakemusOid)}>
            {hakemuksentilaValue(hakemus)}
          </ExternalLink>
        ),
      },
      {
        value: t("hakutilanne__taulu_data_ei_toteutettu"),
        display: (
          <NoDataMessage>
            <T id="hakutilanne__taulu_data_ei_toteutettu" />
          </NoDataMessage>
        ),
      },
      {
        value: t("hakutilanne__taulu_data_ei_toteutettu"),
        display: (
          <NoDataMessage>
            <T id="hakutilanne__taulu_data_ei_toteutettu" />
          </NoDataMessage>
        ),
      },
      {
        value: t("hakutilanne__taulu_data_ei_toteutettu"),
        display: (
          <NoDataMessage>
            <T id="hakutilanne__taulu_data_ei_toteutettu" />
          </NoDataMessage>
        ),
      },
    ],
  }
}

const hakemuksentilaValue = (hakemus?: Haku): string => {
  return t(
    hakemus && hakemus.aktiivinen
      ? "hakemuksentila__hakenut"
      : "hakemuksentila__ei_hakemusta"
  )
}
