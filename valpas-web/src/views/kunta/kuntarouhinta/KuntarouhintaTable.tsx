import * as A from "fp-ts/Array"
import { flow, pipe } from "fp-ts/lib/function"
import * as O from "fp-ts/Option"
import React, { useMemo } from "react"
import {
  Datum,
  PaginatedDataTable,
  Value,
} from "../../../components/tables/DataTable"
import { Column } from "../../../components/tables/useDataTableState"
import {
  getLocalizedMaybe,
  koodiviiteToShortString,
  t,
} from "../../../i18n/i18n"
import { KuntailmoitusSuppeatTiedot } from "../../../state/apitypes/kuntailmoitus"
import { OppivelvollisuudenKeskeytys } from "../../../state/apitypes/oppivelvollisuudenkeskeytys"
import {
  KuntarouhinnanTulos,
  RouhintaOpiskeluoikeus,
  RouhintaOppivelvollinen,
} from "../../../state/apitypes/rouhinta"
import { useBasePath } from "../../../state/basePath"
import { Oid } from "../../../state/common"
import { nullableJoinToString } from "../../../utils/arrays"
import { formatDateRange, formatNullableDate } from "../../../utils/date"
import { pluck } from "../../../utils/objects"
import {
  dateValue,
  nonNullableValue,
  nullableDateValue,
  nullableKoulutustyyppiValue,
  nullableValue,
  oppijanNimiValue,
} from "../../../utils/tableDataFormatters/commonFormatters"

const PAGINATION_SIZE = 500

export type KuntarouhintaTableProps = {
  data: KuntarouhinnanTulos
  organisaatioOid: Oid
}

export const KuntarouhintaTable = (props: KuntarouhintaTableProps) => {
  const columns: Column[] = useMemo(
    () => [
      {
        label: t("rouhinta_nimi"),
        filter: "freetext",
      },
      {
        label: t("rouhinta_syntymäpäivä"),
      },
      {
        label: t("rouhinta_oppijanumero"),
        filter: "freetext",
      },
      {
        label: t("rouhinta_hetu"),
        filter: "freetext",
      },
      {
        label: t("rouhinta_oo_päättymispäivä"),
        tooltip: t("rouhinta_oo_päättymispäivä_comment"),
      },
      {
        label: t("rouhinta_viimeisin_tila"),
        tooltip: t("rouhinta_viimeisin_tila_comment"),
      },
      {
        label: t("rouhinta_koulutusmuoto"),
        tooltip: t("rouhinta_koulutusmuoto_comment"),
      },
      {
        label: t("rouhinta_toimipiste"),
        tooltip: t("rouhinta_toimipiste_comment"),
      },
      {
        label: t("rouhinta_ov_keskeytys"),
        tooltip: t("rouhinta_ov_keskeytys_comment"),
      },
      {
        label: t("rouhinta_kuntailmoitus_kohde"),
        tooltip: t("rouhinta_kuntailmoitus_kohde_comment"),
      },
      {
        label: t("rouhinta_kuntailmoitus_pvm"),
        tooltip: t("rouhinta_kuntailmoitus_pvm_comment"),
      },
    ],
    []
  )

  const basePath = useBasePath()
  const data = useMemo(
    () =>
      kuntarouhinnanTulosToTableData(
        props.data,
        props.organisaatioOid,
        basePath
      ),
    [basePath, props.data, props.organisaatioOid]
  )

  return (
    <PaginatedDataTable
      className="kuntarouhintatable"
      columns={columns}
      data={data}
      paginationSize={PAGINATION_SIZE}
    />
  )
}

const kuntarouhinnanTulosToTableData = (
  data: KuntarouhinnanTulos,
  organisaatioOid: Oid,
  basePath: string
): Datum[] =>
  data.eiOppivelvollisuuttaSuorittavat.map(
    oppijaToTableData(organisaatioOid, basePath)
  )

const oppijaToTableData =
  (organisaatioOid: Oid, basePath: string) =>
  (oppija: RouhintaOppivelvollinen): Datum => {
    const oo =
      oppija.viimeisinOppivelvollisuudenSuorittamiseenKelpaavaOpiskeluoikeus
    return {
      key: [oppija.oppijanumero],
      values: [
        // Oppija
        nimiValue(oppija, organisaatioOid, basePath),
        nullableDateValue(oppija.syntymäaika),
        nonNullableValue(oppija.oppijanumero),
        nullableValue(oppija.hetu),
        // Viimeisin oppivelvollisuuden suorittamiseen kelpaava opiskeluoikeus
        päättymispäiväValue(oo),
        viimeisinTilaValue(oo),
        nullableKoulutustyyppiValue(oo?.suorituksenTyyppi),
        nullableValue(getLocalizedMaybe(oo?.toimipiste)),
        // Oppivelvollisuuden keskeytys
        oppivelvollisuudenKeskeytysValue(oppija.oppivelvollisuudenKeskeytys),
        // Aktiivisen kuntailmoituksen kohde ja päivämäärä
        ilmoitettuKunnalleKotipaikka(oppija.aktiivinenKuntailmoitus),
        ilmoituksenTekopäivä(oppija.aktiivinenKuntailmoitus),
      ],
    }
  }

const nimiValue = (
  oppija: RouhintaOppivelvollinen,
  organisaatioOid: Oid,
  basePath: string
): Value =>
  oppijanNimiValue("kuntaRef")(
    { ...oppija, oid: oppija.oppijanumero },
    organisaatioOid,
    basePath
  )

const päättymispäiväValue = (opiskeluoikeus?: RouhintaOpiskeluoikeus): Value =>
  pipe(
    O.fromNullable(opiskeluoikeus),
    O.map(
      flow(
        pluck("päättymispäivä"),
        O.fromNullable,
        O.map(dateValue),
        O.getOrElse(tValue("rouhinta_ei_päättynyt"))
      )
    ),
    O.getOrElse(tValue("rouhinta_ei_opiskeluoikeutta"))
  )

const viimeisinTilaValue = (opiskeluoikeus?: RouhintaOpiskeluoikeus): Value =>
  pipe(
    O.fromNullable(opiskeluoikeus),
    O.map(
      flow(pluck("viimeisinTila"), koodiviiteToShortString, nonNullableValue)
    ),
    O.getOrElse(() => nullableValue(null))
  )

const tValue = (key: string) => () => nonNullableValue(t(key))

const oppivelvollisuudenKeskeytysValue = (
  keskeytykset: OppivelvollisuudenKeskeytys[]
) =>
  pipe(
    keskeytykset,
    A.map((k) => formatDateRange(k.alku, k.loppu)),
    nullableJoinToString(", "),
    nullableValue
  )

const ilmoitettuKunnalleKotipaikka = (
  ilmoitus?: KuntailmoitusSuppeatTiedot
): Value => ({
  value: ilmoitus
    ? getLocalizedMaybe(ilmoitus.kunta.kotipaikka?.nimi) || ilmoitus.kunta.oid
    : "–",
})

const ilmoituksenTekopäivä = (
  ilmoitus?: KuntailmoitusSuppeatTiedot
): Value => ({
  value: ilmoitus ? ilmoitus.aikaleima : "–",
  display: ilmoitus ? formatNullableDate(ilmoitus.aikaleima) : undefined,
})
