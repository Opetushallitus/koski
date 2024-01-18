import * as A from "fp-ts/Array"
import React, { useMemo } from "react"
import { Link } from "react-router-dom"
import {
  Column,
  DataTable,
  DataTableProps,
  Datum,
  Value,
} from "../../components/tables/DataTable"
import { getLocalizedMaybe, t } from "../../i18n/i18n"
import { KuntailmoitusSuppeatTiedot } from "../../state/apitypes/kuntailmoitus"
import { OppijaHakutilanteillaSuppeatTiedot } from "../../state/apitypes/oppija"
import { useBasePath } from "../../state/basePath"
import { Oid } from "../../state/common"
import { oppijaPath } from "../../state/paths"
import { formatNullableDate } from "../../utils/date"
import { OppijaViewBackNavProps } from "../oppija/OppijaView"

export type KunnalleIlmoitetutTableProps = {
  data: OppijaHakutilanteillaSuppeatTiedot[]
  organisaatioOid: Oid
  backRefName: keyof OppijaViewBackNavProps
  storageName: string
} & Pick<DataTableProps, "onCountChange">

export const KunnalleIlmoitetutTable = (
  props: KunnalleIlmoitetutTableProps,
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
        tooltip: t("kunnalleilmoitetut_muu_haku_tooltip"),
      },
    ],
    [],
  )

  const basePath = useBasePath()
  const data = toTableData(
    props.data,
    props.organisaatioOid,
    basePath,
    props.backRefName,
  )

  return (
    <DataTable
      columns={columns}
      data={data}
      onCountChange={props.onCountChange}
      storageName={props.storageName}
      className="kuntailmoitukset"
    />
  )
}

const toTableData = (
  data: OppijaHakutilanteillaSuppeatTiedot[],
  organisaatioOid: Oid,
  basePath: string,
  backRefName: keyof OppijaViewBackNavProps,
): Datum[] =>
  A.chain(oppijaToTableData(organisaatioOid, basePath, backRefName))(data)

const oppijaToTableData =
  (
    organisaatioOid: Oid,
    basePath: string,
    backRefName: keyof OppijaViewBackNavProps,
  ) =>
  (oppija: OppijaHakutilanteillaSuppeatTiedot): Datum[] =>
    oppija.kuntailmoitukset.map((kuntailmoitus) => ({
      key: [oppija.oppija.henkilö.oid, kuntailmoitus.id || ""],
      values: [
        oppijanNimi(oppija, organisaatioOid, basePath, backRefName),
        syntymäaika(oppija),
        ilmoitettuKunnalle(kuntailmoitus),
        ilmoituksenTekopäivä(kuntailmoitus),
        muuHaku(kuntailmoitus),
      ],
    }))

const oppijanNimi = (
  oppija: OppijaHakutilanteillaSuppeatTiedot,
  organisaatioOid: Oid,
  basePath: string,
  backRefName: keyof OppijaViewBackNavProps,
): Value => {
  const value = `${oppija.oppija.henkilö.sukunimi} ${oppija.oppija.henkilö.etunimet}`
  const linkTo =
    oppija.oppija.henkilö.oid !== ""
      ? oppijaPath.href(basePath, {
          oppijaOid: oppija.oppija.henkilö.oid,
          [backRefName]: organisaatioOid,
        })
      : undefined

  return {
    value,
    display: linkTo && <Link to={linkTo}>{value}</Link>,
  }
}

const syntymäaika = (oppija: OppijaHakutilanteillaSuppeatTiedot): Value => ({
  value: oppija.oppija.henkilö.syntymäaika,
  display: formatNullableDate(oppija.oppija.henkilö.syntymäaika),
})

const ilmoitettuKunnalle = (ilmoitus: KuntailmoitusSuppeatTiedot): Value => ({
  value: getLocalizedMaybe(ilmoitus.kunta.nimi) || ilmoitus.kunta.oid,
})

const ilmoituksenTekopäivä = (ilmoitus: KuntailmoitusSuppeatTiedot): Value => ({
  value: ilmoitus.aikaleima,
  display: formatNullableDate(ilmoitus.aikaleima),
})

const muuHaku = (ilmoitus: KuntailmoitusSuppeatTiedot): Value => ({
  value:
    ilmoitus.hakenutMuualle === undefined
      ? "–"
      : ilmoitus.hakenutMuualle
        ? t("Kyllä")
        : t("Ei"),
})
