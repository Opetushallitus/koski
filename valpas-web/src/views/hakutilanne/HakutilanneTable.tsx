import * as A from "fp-ts/lib/Array"
import React, { useMemo } from "react"
import { Link } from "react-router-dom"
import { ExternalLink } from "../../components/navigation/ExternalLink"
import { DataTable, Datum } from "../../components/tables/DataTable"
import { NotImplemented } from "../../components/typography/NoDataMessage"
import { T, t, Translation } from "../../i18n/i18n"
import { useBasePath } from "../../state/basePath"
import {
  HakuSuppeatTiedot,
  OppijaHakutilanteillaSuppeatTiedot,
} from "../../state/oppijat"
import { formatNullableDate } from "../../utils/date"

export type HakutilanneTableProps = {
  data: OppijaHakutilanteillaSuppeatTiedot[]
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
) => (oppija: OppijaHakutilanteillaSuppeatTiedot): Array<Datum> => {
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
      hakemuksenTila(oppija.hakutilanteet, oppija.hakutilanneError),
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

export const hakemuksenTila = (
  hakutilanteet: HakuSuppeatTiedot[],
  hakutilanneError?: string
) => {
  const hakemuksenTila = hakemuksenTilaT(hakutilanteet.length, hakutilanneError)
  const component = () => {
    if (hakutilanteet.length == 0) return null
    else if (hakutilanteet.length == 1 && hakutilanteet[0])
      return (
        <ExternalLink to={hakutilanteet[0].hakemusUrl}>
          {hakemuksenTila}
        </ExternalLink>
      )
    else return null
  }
  return {
    value: hakemuksenTila,
    display: component(),
  }
}

const hakemuksenTilaT = (
  hakemusCount: number,
  hakutilanneError?: string
): Translation => {
  if (hakutilanneError) return t("oppija__hakuhistoria_virhe")
  else if (hakemusCount == 0) return t("hakemuksentila__ei_hakemusta")
  else if (hakemusCount == 1) return t("hakemuksentila__hakenut")
  else return `${hakemusCount} ${t("hakemuksentila__n_hakua")}`
}
