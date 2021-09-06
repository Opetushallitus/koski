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
import { createOppijaPath } from "../../state/paths"
import { formatNullableDate } from "../../utils/date"
import { OppijaViewBackNavProps } from "../oppija/OppijaView"

export type KunnalleIlmoitetutTableProps = {
  data: OppijaHakutilanteillaSuppeatTiedot[]
  organisaatioOid: Oid
  backRefName: keyof OppijaViewBackNavProps
} & Pick<DataTableProps, "onCountChange">

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

  const basePath = useBasePath()
  const data = toTableData(
    props.data,
    props.organisaatioOid,
    basePath,
    props.backRefName
  )

  return (
    <DataTable
      columns={columns}
      data={data}
      onCountChange={props.onCountChange}
    />
  )
}

const toTableData = (
  data: OppijaHakutilanteillaSuppeatTiedot[],
  organisaatioOid: Oid,
  basePath: string,
  backRefName: keyof OppijaViewBackNavProps
): Datum[] =>
  A.chain(oppijaToTableData(organisaatioOid, basePath, backRefName))(data)

const oppijaToTableData = (
  organisaatioOid: Oid,
  basePath: string,
  backRefName: keyof OppijaViewBackNavProps
) => (oppija: OppijaHakutilanteillaSuppeatTiedot): Datum[] =>
  oppija.kuntailmoitukset.map((kuntailmoitus) => ({
    key: [oppija.oppija.henkilö.oid, kuntailmoitus.id || ""],
    values: [
      oppijanNimi(oppija, organisaatioOid, basePath, backRefName),
      syntymäaika(oppija),
      ilmoitettuKunnalle(kuntailmoitus),
      ilmoituksenTekopäivä(kuntailmoitus),
      muuHaku(oppija),
    ],
  }))

const oppijanNimi = (
  oppija: OppijaHakutilanteillaSuppeatTiedot,
  organisaatioOid: Oid,
  basePath: string,
  backRefName: keyof OppijaViewBackNavProps
): Value => {
  const value = `${oppija.oppija.henkilö.sukunimi} ${oppija.oppija.henkilö.etunimet}`
  const linkTo = createOppijaPath(basePath, {
    oppijaOid: oppija.oppija.henkilö.oid,
    [backRefName]: organisaatioOid,
  })

  return {
    value,
    display: <Link to={linkTo}>{value}</Link>,
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

const muuHaku = (oppija: OppijaHakutilanteillaSuppeatTiedot): Value => ({
  value:
    oppija.lisätiedot.find((t) => t.muuHaku) !== undefined
      ? t("Kyllä")
      : t("Ei"),
})
