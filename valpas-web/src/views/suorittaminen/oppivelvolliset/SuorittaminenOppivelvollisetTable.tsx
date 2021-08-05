import * as A from "fp-ts/Array"
import React, { useMemo } from "react"
import { Link } from "react-router-dom"
import { FutureSuccessIcon, SuccessIcon } from "../../../components/icons/Icon"
import {
  Column,
  DataTable,
  Datum,
  Value,
} from "../../../components/tables/DataTable"
import { SelectableDataTableProps } from "../../../components/tables/SelectableDataTable"
import { getLocalized, t } from "../../../i18n/i18n"
import {
  OpiskeluoikeusSuppeatTiedot,
  suorittamisvalvottavatOpiskeluoikeudet,
} from "../../../state/apitypes/opiskeluoikeus"
import {
  OppijaHakutilanteillaSuppeatTiedot,
  OppijaSuppeatTiedot,
} from "../../../state/apitypes/oppija"
import {
  isVoimassa,
  isVoimassaTulevaisuudessa,
} from "../../../state/apitypes/valpasopiskeluoikeudentila"
import { useBasePath } from "../../../state/basePath"
import { Oid } from "../../../state/common"
import { createOppijaPath } from "../../../state/paths"
import { nonNull } from "../../../utils/arrays"
import { FilterableValue } from "../../../utils/conversions"
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
      },
      {
        label: t("suorittaminennäkymä__taulu_syntymäaika"),
        size: "small",
      },
      {
        label: t("suorittaminennäkymä__taulu_koulutustyyppi"),
        size: "small",
      },
      {
        label: t("suorittaminennäkymä__taulu_tila"),
        filter: "dropdown",
        size: "small",
      },
      {
        label: t("suorittaminennäkymä__taulu_toimipipste"),
        filter: "dropdown",
        size: "small",
      },
      {
        label: t("suorittaminennäkymä__taulu_alkamispäivä"),
        filter: "dropdown",
        size: "small",
      },
      {
        label: t("suorittaminennäkymä__taulu_päättymispäivä"),
        filter: "dropdown",
        size: "small",
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
        filter: "dropdown",
        size: "small",
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

const oppijaToTableData = (basePath: string, organisaatioOid: string) => (
  oppija: OppijaHakutilanteillaSuppeatTiedot
): Array<Datum> => {
  const henkilö = oppija.oppija.henkilö

  return suorittamisvalvottavatOpiskeluoikeudet(
    organisaatioOid,
    oppija.oppija.opiskeluoikeudet
  ).map((opiskeluoikeus) => {
    return {
      key: createSuorittaminenKey(oppija.oppija, opiskeluoikeus),
      values: [
        {
          value: `${henkilö.sukunimi} ${henkilö.etunimet}`,
          display: (
            <Link
              to={createOppijaPath(basePath, {
                suorittaminenRef: organisaatioOid,
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
        koulutustyyppi(opiskeluoikeus),
        tila(opiskeluoikeus),
        fromNullable(getLocalized(opiskeluoikeus.toimipiste?.nimi)),
        {
          value: opiskeluoikeus.alkamispäivä,
          display: formatNullableDate(opiskeluoikeus.alkamispäivä),
        },
        {
          value: opiskeluoikeus.päättymispäivä,
          display: formatNullableDate(opiskeluoikeus.päättymispäivä),
        },
        fromNullableValue(
          opiskeluoikeustiedot(oppija.oppija.opiskeluoikeudet, opiskeluoikeus)
        ),
        {
          // TODO: käsittele keskeytykset, ota mallia OppijanOppivelvollisuustiedot.tsx:stä. Tällä hetkellä dataa ei tule.
          value: oppija.oppija.oppivelvollisuusVoimassaAsti,
          display: formatNullableDate(
            oppija.oppija.oppivelvollisuusVoimassaAsti
          ),
        },
      ],
    }
  })
}

// TODO: Tästä vastauksena 2. aste/VALMA/TELMA/...
const koulutustyyppi = (oo: OpiskeluoikeusSuppeatTiedot): Value => {
  const koulutustyyppi = {
    value: `TODO ${oo.tyyppi.koodiarvo}`,
    filterValues: [`TODO ${oo.tyyppi.koodiarvo}`],
    display: `TODO ${oo.tyyppi.koodiarvo}`,
  }

  return koulutustyyppi
}

const tila = (oo: OpiskeluoikeusSuppeatTiedot): Value => {
  oo.tarkastelupäivänTila
  const koulutustyyppi = {
    value: tilaString(oo),
    filterValues: [tilaString(oo)],
    display: tilaString(oo),
  }

  return koulutustyyppi
}

const tilaString = (opiskeluoikeus: OpiskeluoikeusSuppeatTiedot): string => {
  const tila = opiskeluoikeus.tarkastelupäivänTila
  return getLocalized(tila.nimi) || tila.koodiarvo
}

const fromNullableValue = (
  value: Value | null | undefined,
  nullFilterValues?: Array<string | number>
): Value =>
  value || {
    value: "–",
    filterValues: nullFilterValues,
  }

const fromNullable = (value: FilterableValue | null | undefined): Value =>
  fromNullableValue({
    value: value ? value : "-",
  })

const opiskeluoikeustiedot = (
  opiskeluoikeudet: OpiskeluoikeusSuppeatTiedot[],
  käsiteltäväOpiskeluoikeus: OpiskeluoikeusSuppeatTiedot
): Value | null => {
  const oos = opiskeluoikeudet
    .filter((oo) => oo.oid != käsiteltäväOpiskeluoikeus.oid)
    .filter(
      suorittamisvalvonnanOpiskeluoikeusSarakkeessaNäytettäväOpiskeluoikeus
    )

  const toValue = (oo: OpiskeluoikeusSuppeatTiedot) => {
    const kohde = [
      getLocalized(oo.oppilaitos.nimi),
      getLocalized(oo.tyyppi.nimi),
    ]
      .filter(nonNull)
      .join(", ")

    return isVoimassa(oo.tarkastelupäivänTila)
      ? kohde
      : t("opiskeluoikeudet__pvm_alkaen_kohde", {
          päivämäärä: formatDate(oo.alkamispäivä),
          kohde,
        })
  }

  const icon = oos.some((oo) => isVoimassa(oo.tarkastelupäivänTila)) ? (
    <SuccessIcon />
  ) : oos.some((oo) => isVoimassaTulevaisuudessa(oo.tarkastelupäivänTila)) ? (
    <FutureSuccessIcon />
  ) : undefined

  switch (oos.length) {
    case 0:
      return null
    case 1:
      return { value: toValue(oos[0]!!), icon }
    default:
      const filterValues = oos.map(toValue).filter(nonNull)
      return {
        value: t("opiskeluoikeudet__n_opiskeluoikeutta", {
          lukumäärä: oos.length,
        }),
        filterValues,
        tooltip: filterValues.join("\n"),
        icon,
      }
  }
}

// TODO: Filtteröi pois opiskeluoikeus, jonka riviä ollaan näyttämässä
const suorittamisvalvonnanOpiskeluoikeusSarakkeessaNäytettäväOpiskeluoikeus = (
  opiskeluoikeus: OpiskeluoikeusSuppeatTiedot
): boolean => {
  const tila = opiskeluoikeus.tarkastelupäivänTila.koodiarvo
  return tila === "voimassa" || tila === "voimassatulevaisuudessa"
}
