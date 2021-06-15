import * as A from "fp-ts/lib/Array"
import React, { useMemo } from "react"
import { Link } from "react-router-dom"
import { FutureSuccessIcon, SuccessIcon } from "../../components/icons/Icon"
import {
  Column,
  DataTable,
  DataTableCountChangeEvent,
  Datum,
  Value,
} from "../../components/tables/DataTable"
import { getLocalized, t } from "../../i18n/i18n"
import {
  opiskeluoikeusSarakkeessaNäytettäväOpiskeluoikeus,
  OpiskeluoikeusSuppeatTiedot,
} from "../../state/apitypes/opiskeluoikeus"
import { OppijaKuntailmoituksillaSuppeatTiedot } from "../../state/apitypes/oppija"
import {
  isVoimassa,
  isVoimassaTulevaisuudessa,
} from "../../state/apitypes/valpasopiskeluoikeudentila"
import { useBasePath } from "../../state/basePath"
import { Oid } from "../../state/common"
import { createOppijaPath } from "../../state/paths"
import { nonNull } from "../../utils/arrays"
import { formatDate, formatNullableDate } from "../../utils/date"

export type KuntailmoitusTableProps = {
  data: OppijaKuntailmoituksillaSuppeatTiedot[]
  onCountChange: (event: DataTableCountChangeEvent) => void
  organisaatioOid: string
}

const useIlmoitusData = (
  organisaatioOid: Oid,
  data: OppijaKuntailmoituksillaSuppeatTiedot[]
) => {
  const basePath = useBasePath()
  return useMemo(
    () => A.flatten(data.map(ilmoitusToTableData(basePath, organisaatioOid))),
    [data, basePath, organisaatioOid]
  )
}

export const KuntailmoitusTable = (props: KuntailmoitusTableProps) => {
  const data = useIlmoitusData(props.organisaatioOid, props.data)

  const columns: Column[] = useMemo(
    () => [
      {
        label: t("kuntailmoitus__taulu_nimi"),
        filter: "freetext",
        size: "large",
      },
      {
        label: t("kuntailmoitus__taulu_ilmoituspäivä"),
        size: "small",
      },
      {
        label: t("kuntailmoitus__taulu_ilmoittaja"),
        size: "small",
      },
      {
        label: t("kuntailmoitus__taulu_syntymäaika"),
        size: "small",
      },
      {
        label: t("kuntailmoitus__taulu_opiskeluoikeuksia"),
        tooltip: t("kuntailmoitus__taulu_opiskeluoikeuksia_tooltip"),
        filter: "dropdown",
        indicatorSpace: "auto",
      },
    ],
    []
  )

  return (
    <DataTable
      key={props.organisaatioOid}
      storageName={`kuntailmoitustaulu-${props.organisaatioOid}`}
      className="kuntailmoitus"
      columns={columns}
      data={data}
      onCountChange={props.onCountChange}
    />
  )
}

const ilmoitusToTableData = (basePath: string, organisaatioOid: string) => (
  tiedot: OppijaKuntailmoituksillaSuppeatTiedot
): Array<Datum> => {
  const henkilö = tiedot.oppija.henkilö

  return tiedot.kuntailmoitukset.map((ilmoitus) => ({
    key: [ilmoitus.id],
    values: [
      {
        value: `${henkilö.sukunimi} ${henkilö.etunimet}`,
        display: (
          <Link
            to={createOppijaPath(basePath, {
              kuntailmoitusRef: organisaatioOid,
              oppijaOid: henkilö.oid,
            })}
          >
            {henkilö.sukunimi} {henkilö.etunimet}
          </Link>
        ),
      },
      {
        value: ilmoitus.aikaleima,
        display: formatNullableDate(ilmoitus.aikaleima),
      },
      {
        value: getLocalized(ilmoitus.tekijä.organisaatio.nimi),
      },
      {
        value: henkilö.syntymäaika,
        display: formatNullableDate(henkilö.syntymäaika),
      },
      opiskeluoikeustiedot(tiedot.oppija.opiskeluoikeudet),
    ],
  }))
}

// TODO: Copypaste HakutilanneTablesta - poista toisteisuus jos pysyy samankaltaisena
const opiskeluoikeustiedot = (
  opiskeluoikeudet: OpiskeluoikeusSuppeatTiedot[]
): Value => {
  const oos = opiskeluoikeudet.filter(
    opiskeluoikeusSarakkeessaNäytettäväOpiskeluoikeus
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
      return { value: "-" }
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
