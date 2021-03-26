import * as A from "fp-ts/lib/Array"
import React, { useMemo } from "react"
import { Link } from "react-router-dom"
import { ExternalLink } from "../../components/navigation/ExternalLink"
import { DataTable, Datum } from "../../components/tables/DataTable"
import { NotImplemented } from "../../components/typography/NoDataMessage"
import { T, t } from "../../i18n/i18n"
import { useBasePath } from "../../state/basePath"
import { externalHakemussivu } from "../../state/externalUrls"
import { Haku, OppijaHakutilanteilla } from "../../state/oppijat"
import { formatNullableDate } from "../../utils/date"

export type HakutilanneTableProps = {
  data: OppijaHakutilanteilla[]
  organisaatioOid: string | undefined
}

export const HakutilanneTable = (props: HakutilanneTableProps) => {
  const basePath = useBasePath()
  const data = useMemo(
    () =>
      A.flatten(
        props.data.map(oppijaToTableData(basePath, props.organisaatioOid))
      ),
    [props.data]
  )

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

const oppijaToTableData = (
  basePath: string,
  organisaatioOid: string | undefined
) => (oppija: OppijaHakutilanteilla): Array<Datum> => {
  // TODO: Hakemuksen valintaan tarvitaan rautaisempi logiikka
  const hakemus = oppija.hakutilanteet[0]
  const hakemuksenTila = hakemuksentilaValue(hakemus, oppija.hakutilanneError)

  const valvottavatOpiskeluoikeudet = oppija.oppija.opiskeluoikeudet.filter(
    (oo) =>
      oppija.oppija.valvottavatOpiskeluoikeudet.includes(oo.oid) &&
      oo.oppilaitos.oid == organisaatioOid
  )

  const henkilö = oppija.oppija.henkilö

  return valvottavatOpiskeluoikeudet.map((opiskeluoikeus) => ({
    key: opiskeluoikeus.oid,
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
        value: henkilö.syntymäaika,
        display: formatNullableDate(henkilö.syntymäaika),
      },
      {
        value: opiskeluoikeus?.ryhmä,
      },
      {
        value: hakemuksenTila,
        display: hakemus && (
          <ExternalLink to={externalHakemussivu(hakemus.hakemusOid)}>
            {hakemuksenTila}
          </ExternalLink>
        ),
      },
      {
        value: t("hakutilanne__taulu_data_ei_toteutettu"),
        display: (
          <NotImplemented>
            <T id="hakutilanne__taulu_data_ei_toteutettu" />
          </NotImplemented>
        ),
      },
      {
        value: t("hakutilanne__taulu_data_ei_toteutettu"),
        display: (
          <NotImplemented>
            <T id="hakutilanne__taulu_data_ei_toteutettu" />
          </NotImplemented>
        ),
      },
      {
        value: t("hakutilanne__taulu_data_ei_toteutettu"),
        display: (
          <NotImplemented>
            <T id="hakutilanne__taulu_data_ei_toteutettu" />
          </NotImplemented>
        ),
      },
    ],
  }))
}

const hakemuksentilaValue = (
  hakemus?: Haku,
  hakutilanneError?: string
): string => {
  return t(
    hakutilanneError
      ? "oppija__hakuhistoria_virhe"
      : hakemus && hakemus.aktiivinen
      ? "hakemuksentila__hakenut"
      : "hakemuksentila__ei_hakemusta"
  )
}
