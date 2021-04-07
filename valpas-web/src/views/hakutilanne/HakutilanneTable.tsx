import * as A from "fp-ts/lib/Array"
import React, { useMemo } from "react"
import { Link } from "react-router-dom"
import { ExternalLink } from "../../components/navigation/ExternalLink"
import { DataTable, Datum, Value } from "../../components/tables/DataTable"
import { NotImplemented } from "../../components/typography/NoDataMessage"
import { T, t, Translation } from "../../i18n/i18n"
import { useBasePath } from "../../state/basePath"
import {
  HakuSuppeatTiedot,
  OppijaHakutilanteillaSuppeatTiedot,
} from "../../state/oppijat"
import { createOppijaPath } from "../../state/paths"
import { formatNullableDate } from "../../utils/date"

export type HakutilanneTableProps = {
  data: OppijaHakutilanteillaSuppeatTiedot[]
  organisaatioOid: string
}

export const HakutilanneTable = (props: HakutilanneTableProps) => {
  const basePath = useBasePath()
  const data = useMemo(
    () =>
      A.flatten(
        props.data.map(oppijaToTableData(basePath, props.organisaatioOid))
      ),
    [props.organisaatioOid, props.data]
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

const oppijaToTableData = (basePath: string, organisaatioOid: string) => (
  oppija: OppijaHakutilanteillaSuppeatTiedot
): Array<Datum> => {
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
          <Link
            to={createOppijaPath(basePath, {
              organisaatioOid,
              oppijaOid: henkilö.oid,
            })}
          >
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

const hakemuksenTila = (
  hakutilanteet: HakuSuppeatTiedot[],
  hakutilanneError?: string
): Value => {
  const hakemuksenTilaValue = hakemuksenTilaT(
    hakutilanteet.length,
    hakutilanneError
  )
  return {
    value: hakemuksenTilaValue,
    display: hakemuksenTilaDisplay(hakutilanteet, hakemuksenTilaValue),
  }
}

const hakemuksenTilaT = (
  hakemusCount: number,
  hakutilanneError?: string
): Translation => {
  if (hakutilanneError) return t("oppija__hakuhistoria_virhe")
  else if (hakemusCount == 0) return t("hakemuksentila__ei_hakemusta")
  else if (hakemusCount == 1) return t("hakemuksentila__hakenut")
  else return t("hakemuksentila__n_hakua", { lukumäärä: hakemusCount })
}

const hakemuksenTilaDisplay = (
  hakutilanteet: HakuSuppeatTiedot[],
  hakemuksenTilaValue: Translation
) => {
  if (hakutilanteet.length == 0) return null
  else if (hakutilanteet.length == 1 && hakutilanteet[0]) {
    return (
      <ExternalLink to={hakutilanteet[0].hakemusUrl}>
        {hakemuksenTilaValue}
      </ExternalLink>
    )
  } else return null
}
