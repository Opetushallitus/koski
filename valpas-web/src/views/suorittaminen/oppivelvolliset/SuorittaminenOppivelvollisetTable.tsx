import * as A from "fp-ts/Array"
import React, { useMemo } from "react"
import { Link } from "react-router-dom"
import {
  OpiskeluhistoriaTapahtumaIcon,
  WarningIcon,
} from "../../../components/icons/Icon"
import {
  Column,
  DataTable,
  Datum,
  fromNullableValue,
  Value,
} from "../../../components/tables/DataTable"
import { SelectableDataTableProps } from "../../../components/tables/SelectableDataTable"
import { getLocalizedMaybe, t } from "../../../i18n/i18n"
import {
  isSuorittamisenValvonnassaIlmoitettavaTila,
  KoskiOpiskeluoikeudenTila,
} from "../../../state/apitypes/koskiopiskeluoikeudentila"
import {
  OpiskeluoikeusSuppeatTiedot,
  suorittamisvalvottavatOpiskeluoikeudet,
} from "../../../state/apitypes/opiskeluoikeus"
import {
  OppijaHakutilanteillaSuppeatTiedot,
  OppijaSuppeatTiedot,
} from "../../../state/apitypes/oppija"
import { OppivelvollisuudenKeskeytys } from "../../../state/apitypes/oppivelvollisuudenkeskeytys"
import { organisaatioNimi } from "../../../state/apitypes/organisaatiot"
import {
  Suorituksentyyppi,
  suorituksenTyyppiToKoulutustyyppi,
} from "../../../state/apitypes/suorituksentyyppi"
import { useBasePath } from "../../../state/basePath"
import { ISODate, Oid } from "../../../state/common"
import { perusopetuksenJälkeistäPreferoivatOpiskeluoikeustiedot } from "../../../state/opiskeluoikeustiedot"
import { oppijaPath } from "../../../state/paths"
import { formatDate, formatNullableDate } from "../../../utils/date"

export type SuorittaminenOppivelvollisetTableProps = {
  data: OppijaHakutilanteillaSuppeatTiedot[]
  organisaatioOid: string
} & Pick<SelectableDataTableProps, "onCountChange">

const useOppijaData = (
  organisaatioOid: Oid,
  data: OppijaHakutilanteillaSuppeatTiedot[]
) => {
  const basePath = useBasePath()
  return useMemo(
    () => A.flatten(data.map(oppijaToTableData(basePath, organisaatioOid))),
    [data, basePath, organisaatioOid]
  )
}

export const SuorittaminenOppivelvollisetTable = (
  props: SuorittaminenOppivelvollisetTableProps
) => {
  const data = useOppijaData(props.organisaatioOid, props.data)

  const columns: Column[] = useMemo(
    () => [
      {
        label: t("suorittaminennäkymä__taulu_nimi"),
        filter: "freetext",
        size: "large",
        indicatorSpace: "auto",
      },
      {
        label: t("suorittaminennäkymä__taulu_syntymäaika"),
        size: "small",
      },
      {
        label: t("suorittaminennäkymä__taulu_koulutustyyppi"),
        filter: "dropdown",
        size: "small",
      },
      {
        label: t("suorittaminennäkymä__taulu_tila"),
        filter: "dropdown",
        size: "small",
        indicatorSpace: "auto",
      },
      {
        label: t("suorittaminennäkymä__taulu_toimipipste"),
        filter: "dropdown",
        size: "large",
      },
      {
        label: t("suorittaminennäkymä__taulu_alkamispäivä"),
      },
      {
        label: t("suorittaminennäkymä__taulu_päättymispäivä"),
      },
      {
        label: t("suorittaminennäkymä__taulu_voimassaolevia_opiskeluoikeuksia"),
        tooltip: t(
          "suorittaminennäkymä__taulu_voimassaolevia_opiskeluoikeuksia_tooltip"
        ),
        filter: "dropdown",
        indicatorSpace: "auto",
      },
      {
        label: t("suorittaminennäkymä__taulu_oppivelvollisuus"),
      },
    ],
    []
  )

  return (
    <DataTable
      key={props.organisaatioOid}
      storageName={`suorittamistaulu-${props.organisaatioOid}`}
      className="suorittaminen"
      columns={columns}
      data={data}
      onCountChange={props.onCountChange}
    />
  )
}

/** Tuple: [Oppijan oid, Opiskeluoikeuden oid] */
type SuorittaminenKey = [Oid, Oid]

const createSuorittaminenKey = (
  oppija: OppijaSuppeatTiedot,
  opiskeluoikeus: OpiskeluoikeusSuppeatTiedot
): SuorittaminenKey => [oppija.henkilö.oid, opiskeluoikeus.oid]

const oppijaToTableData =
  (basePath: string, organisaatioOid: string) =>
  (oppija: OppijaHakutilanteillaSuppeatTiedot): Array<Datum> => {
    const henkilö = oppija.oppija.henkilö

    return suorittamisvalvottavatOpiskeluoikeudet(
      organisaatioOid,
      oppija.oppija.opiskeluoikeudet
    ).map((opiskeluoikeus) => {
      const tiedot = opiskeluoikeus.perusopetuksenJälkeinenTiedot!

      return {
        key: createSuorittaminenKey(oppija.oppija, opiskeluoikeus),
        values: [
          {
            value: `${henkilö.sukunimi} ${henkilö.etunimet}${opiskeluoikeus.onTehtyIlmoitus}`,
            display: (
              <Link
                to={oppijaPath.href(basePath, {
                  suorittaminenRef: organisaatioOid,
                  oppijaOid: henkilö.oid,
                })}
              >
                {henkilö.sukunimi} {henkilö.etunimet}
              </Link>
            ),
            icon: opiskeluoikeus.onTehtyIlmoitus ? (
              <OpiskeluhistoriaTapahtumaIcon color="blue" />
            ) : null,
          },
          {
            value: henkilö.syntymäaika,
            display: formatNullableDate(henkilö.syntymäaika),
          },
          {
            value: koulutustyyppi(
              opiskeluoikeus.tarkasteltavaPäätasonSuoritus?.suorituksenTyyppi
            ),
          },
          tila(tiedot.tarkastelupäivänKoskiTila),
          toimipiste(opiskeluoikeus),
          fromNullableValue(päivä(tiedot.alkamispäivä)),
          fromNullableValue(päivä(tiedot.päättymispäivä)),
          fromNullableValue(
            perusopetuksenJälkeistäPreferoivatOpiskeluoikeustiedot(
              oppija.oppija.opiskeluoikeudet,
              opiskeluoikeus
            )
          ),
          fromNullableValue(oppivelvollisuus(oppija)),
        ],
      }
    })
  }

const koulutustyyppi = (tyyppi?: Suorituksentyyppi): string =>
  tyyppi === undefined ? "" : suorituksenTyyppiToKoulutustyyppi(tyyppi)

const tila = (tila: KoskiOpiskeluoikeudenTila): Value => ({
  value: tilaString(tila),
  icon: isSuorittamisenValvonnassaIlmoitettavaTila(tila) ? (
    <WarningIcon />
  ) : undefined,
})

const tilaString = (tila: KoskiOpiskeluoikeudenTila): string => {
  return getLocalizedMaybe(tila.nimi) || tila.koodiarvo
}

const toimipiste = (opiskeluoikeus: OpiskeluoikeusSuppeatTiedot): Value => {
  const uniikit: string[] = Array.from(
    new Set(
      opiskeluoikeus.päätasonSuoritukset.map((pts) =>
        organisaatioNimi(pts.toimipiste)
      )
    )
  )
  return {
    value:
      uniikit.length > 1
        ? t("suorittaminennäkymä__taulu_useita_toimipisteitä")
        : opiskeluoikeus.tarkasteltavaPäätasonSuoritus
        ? organisaatioNimi(
            opiskeluoikeus.tarkasteltavaPäätasonSuoritus.toimipiste
          )
        : undefined,
    tooltip: uniikit.join("; "),
    filterValues: uniikit,
  }
}

const päivä = (date?: ISODate): Value | null => {
  return date
    ? {
        value: date,
        filterValues: [date],
        display: formatNullableDate(date),
      }
    : null
}

const oppivelvollisuus = (
  oppija: OppijaHakutilanteillaSuppeatTiedot
): Value | null => {
  const oppivelvollisuusVoimassaAsti =
    oppija.oppija.oppivelvollisuusVoimassaAsti

  return oppivelvollisuusVoimassaAsti
    ? {
        value: oppivelvollisuusVoimassaAsti,
        filterValues: [oppivelvollisuusVoimassaAsti],
        display: (
          <>
            {t("suorittaminennäkymä__oppivelvollisuus_voimassa_value", {
              date: formatDate(oppivelvollisuusVoimassaAsti),
            })}
            {keskeytysMerkintä(oppija.oppivelvollisuudenKeskeytykset)}
          </>
        ),
        tooltip: keskeytysTooltip(oppija.oppivelvollisuudenKeskeytykset),
      }
    : null
}

const keskeytysMerkintä = (
  oppivelvollisuudenKeskeytykset: OppivelvollisuudenKeskeytys[]
): string | null => (oppivelvollisuudenKeskeytykset.length ? "*" : null)

const keskeytysTooltip = (
  oppivelvollisuudenKeskeytykset: OppivelvollisuudenKeskeytys[]
): string | undefined =>
  oppivelvollisuudenKeskeytykset.length
    ? oppivelvollisuudenKeskeytykset
        .map((keskeytys) =>
          keskeytys.loppu
            ? t("suorittaminennäkymä__oppivelvollisuus_keskeytetty_value", {
                alkuPvm: formatDate(keskeytys.alku),
                loppuPvm: formatDate(keskeytys.loppu),
              })
            : t(
                "suorittaminennäkymä__oppivelvollisuus_keskeytetty_toistaiseksi_value",
                {
                  alkuPvm: formatDate(keskeytys.alku),
                }
              )
        )
        .join("\n")
    : undefined
