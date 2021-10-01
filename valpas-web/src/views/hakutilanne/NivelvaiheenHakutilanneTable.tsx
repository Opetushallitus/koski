import * as A from "fp-ts/Array"
import { pipe } from "fp-ts/lib/function"
import * as NEA from "fp-ts/NonEmptyArray"
import * as string from "fp-ts/string"
import React, { useCallback, useMemo } from "react"
import { Datum, DatumKey, Value } from "../../components/tables/DataTable"
import {
  SelectableDataTable,
  SelectableDataTableProps,
} from "../../components/tables/SelectableDataTable"
import { Column } from "../../components/tables/useDataTableState"
import { getLocalized, getLocalizedMaybe, t } from "../../i18n/i18n"
import {
  isHakeutumisvalvottavaOpiskeluoikeus,
  OpiskeluoikeusSuppeatTiedot,
  voimassaolevaTaiTulevaPeruskoulunJälkeinenMuunaOpintonaNäytettäväOpiskeluoikeus,
} from "../../state/apitypes/opiskeluoikeus"
import { OppijaHakutilanteillaSuppeatTiedot } from "../../state/apitypes/oppija"
import { useBasePath } from "../../state/basePath"
import { Oid } from "../../state/common"
import { nonNull } from "../../utils/arrays"
import {
  nullableDateValue,
  nullableKoulutustyyppiValue,
  nullableValue,
  oppijanNimiValue,
} from "../../utils/tableDataFormatters/commonFormatters"
import { hakemuksenTilaValue } from "../../utils/tableDataFormatters/hakemuksentila"
import { opiskelupaikanVastaanottotietoValue } from "../../utils/tableDataFormatters/opiskelupaikanVastaanotto"
import { valintatilaValue } from "../../utils/tableDataFormatters/valintatila"
import { SetMuuHakuCallback } from "./HakutilanneTable"

export type NivelvaiheenHakutilanneTableProps = {
  data: OppijaHakutilanteillaSuppeatTiedot[]
  organisaatioOid: string
  onSelect: (oppijaOids: Oid[]) => void
  onSetMuuHaku: SetMuuHakuCallback
} & Pick<SelectableDataTableProps, "onCountChange">

export const NivelvaiheenHakutilanneTable = (
  props: NivelvaiheenHakutilanneTableProps
) => {
  const columns: Column[] = useMemo(
    () => [
      {
        label: t("nivelvaihehakutilanne__taulu_nimi"),
        filter: "freetext",
        size: "large",
      },
      {
        label: t("nivelvaihehakutilanne__taulu_syntymäaika"),
        size: "small",
      },
      {
        label: t("nivelvaihehakutilanne__taulu_koulutustyyppi"),
        filter: "dropdown",
        size: "small",
      },
      {
        label: t("nivelvaihehakutilanne__taulu_opinnot_alkaneet"),
      },
      {
        label: t("nivelvaihehakutilanne__taulu_opinnot_päättyneet"),
      },
      {
        label: t("nivelvaihehakutilanne__taulu_hakeutumisen_tila"),
        filter: "dropdown",
      },
      {
        label: t("nivelvaihehakutilanne__taulu_valintojen_tulokset"),
        filter: "dropdown",
        indicatorSpace: "auto",
      },
      {
        label: t("nivelvaihehakutilanne__taulu_vastaanotettu_opiskelupaikka"),
        filter: "dropdown",
        indicatorSpace: "auto",
      },
      {
        label: t("nivelvaihehakutilanne__taulu_toisen_asteen_opiskeluoikeus"),
        tooltip: t(
          "nivelvaihehakutilanne__taulu_toisen_asteen_opiskeluoikeus_tooltip"
        ),
        filter: "dropdown",
        indicatorSpace: "auto",
      },
      {
        label: t("nivelvaihehakutilanne__taulu_muu_haku"),
        tooltip: t("nivelvaihehakutilanne__taulu_muu_haku_tooltip"),
        filter: "dropdown",
      },
    ],
    []
  )

  const basePath = useBasePath()
  const data = useMemo(
    () => dataToRows(props.data, props.organisaatioOid, basePath),
    [basePath, props.data, props.organisaatioOid]
  )

  const onSelect = useCallback(
    (keys: HakutilanneRowKey[]) =>
      pipe(keys, A.map(NEA.head), A.uniq(string.Eq), props.onSelect),
    [props.onSelect]
  )

  return (
    <SelectableDataTable
      storageName="nivelvaiheenhakutilanne"
      className="hakutilanne"
      columns={columns}
      data={data}
      onCountChange={props.onCountChange}
      peerEquality={oppijaOidsEqual}
      onSelect={onSelect}
    />
  )
}

const dataToRows = (
  data: OppijaHakutilanteillaSuppeatTiedot[],
  organisaatioOid: Oid,
  basePath: string
): Datum[] => A.chain(oppijatiedotToTableRow(organisaatioOid, basePath))(data)

const oppijatiedotToTableRow = (organisaatioOid: Oid, basePath: string) => (
  tiedot: OppijaHakutilanteillaSuppeatTiedot
): Datum[] =>
  pipe(
    tiedot.oppija.opiskeluoikeudet,
    A.filter(isHakeutumisvalvottavaOpiskeluoikeus(organisaatioOid)),
    A.map((oo) => ({
      key: hakutilanneRowKey(tiedot, oo),
      values: [
        oppijanNimi(tiedot.oppija.henkilö, organisaatioOid, basePath),
        nullableDateValue(tiedot.oppija.henkilö.syntymäaika),
        nullableKoulutustyyppiValue(
          oo.tarkasteltavaPäätasonSuoritus?.suorituksenTyyppi
        ),
        nullableDateValue(oo.perusopetuksenJälkeinenTiedot?.alkamispäivä),
        nullableDateValue(oo.perusopetuksenJälkeinenTiedot?.päättymispäivä),
        hakemuksenTilaValue(tiedot, basePath),
        valintatilaValue(tiedot.hakutilanteet),
        opiskelupaikanVastaanottotietoValue(tiedot.hakutilanteet),
        toisenAsteenOpiskeluoikeudetValue(tiedot, oo),
        nullableValue("TODO"),
      ],
    }))
  )

const oppijanNimi = oppijanNimiValue("hakutilanneNivelvaiheRef")

const toisenAsteenOpiskeluoikeudetValue = (
  tiedot: OppijaHakutilanteillaSuppeatTiedot,
  exclude: OpiskeluoikeusSuppeatTiedot
): Value => {
  const opiskeluoikeudet = pipe(
    tiedot.oppija.opiskeluoikeudet,
    A.filter(
      voimassaolevaTaiTulevaPeruskoulunJälkeinenMuunaOpintonaNäytettäväOpiskeluoikeus
    ),
    A.filter((oo) => oo.oid !== exclude.oid)
  )

  const oppilaitosNimet: string[] = pipe(
    opiskeluoikeudet,
    A.map((oo) => oo.oppilaitos.nimi),
    A.filter(nonNull),
    A.map(getLocalized)
  )

  switch (opiskeluoikeudet.length) {
    case 0:
      return nullableValue(null)
    case 1:
      return {
        value: getLocalizedMaybe(opiskeluoikeudet[0]?.oppilaitos.nimi),
        filterValues: oppilaitosNimet,
      }
    default:
      return {
        value: t("nivelvaihehakutilanne__taulu_useita_opiskeluoikeuksia"),
        filterValues: oppilaitosNimet,
        tooltip: oppilaitosNimet.join("\n"),
      }
  }
}

/** Tuple: [Oppijan oid, Opiskeluoikeuden oid] */
type HakutilanneRowKey = DatumKey

const hakutilanneRowKey = (
  tiedot: OppijaHakutilanteillaSuppeatTiedot,
  opiskeluoikeus: OpiskeluoikeusSuppeatTiedot
): HakutilanneRowKey => [tiedot.oppija.henkilö.oid, opiskeluoikeus.oid]

const oppijaOidsEqual = (a: HakutilanneRowKey) => (b: HakutilanneRowKey) =>
  a[0] === b[0]
