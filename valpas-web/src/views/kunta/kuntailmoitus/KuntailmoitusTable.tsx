import * as A from "fp-ts/lib/Array"
import React, { useMemo } from "react"
import { Link } from "react-router-dom"
import { OpiskeluhistoriaTapahtumaIcon } from "../../../components/icons/Icon"
import {
  Column,
  DataTable,
  DataTableCountChangeEvent,
  Datum,
  fromNullableValue,
} from "../../../components/tables/DataTable"
import { t } from "../../../i18n/i18n"
import { getNäytettävätIlmoitukset } from "../../../state/apitypes/kuntailmoitus"
import { OppijaKuntailmoituksillaSuppeatTiedot } from "../../../state/apitypes/oppija"
import { organisaatioNimi } from "../../../state/apitypes/organisaatiot"
import { useBasePath } from "../../../state/basePath"
import { Oid } from "../../../state/common"
import { perusopetuksenJälkeisetOpiskeluoikeustiedot } from "../../../state/opiskeluoikeustiedot"
import { oppijaPath } from "../../../state/paths"
import { formatNullableDate } from "../../../utils/date"

export type KuntailmoitusTableProps = {
  data: OppijaKuntailmoituksillaSuppeatTiedot[]
  onCountChange: (event: DataTableCountChangeEvent) => void
  organisaatioOid: string
}

const useIlmoitusData = (
  organisaatioOid: Oid,
  data: OppijaKuntailmoituksillaSuppeatTiedot[],
) => {
  const basePath = useBasePath()
  return useMemo(
    () => A.flatten(data.map(ilmoitusToTableData(basePath, organisaatioOid))),
    [data, basePath, organisaatioOid],
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
        indicatorSpace: "auto",
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
    [],
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

const ilmoitusToTableData =
  (basePath: string, organisaatioOid: string) =>
  (tiedot: OppijaKuntailmoituksillaSuppeatTiedot): Array<Datum> => {
    const henkilö = tiedot.oppija.henkilö
    const ainaNäytetävätIlmoitukset = getNäytettävätIlmoitukset(tiedot).map(
      (i) => i.id,
    )

    return tiedot.kuntailmoitukset.map((ilmoitus) => ({
      key: [ilmoitus.id],
      values: [
        {
          value: `${henkilö.sukunimi} ${henkilö.etunimet}`,
          display: (
            <Link
              to={oppijaPath.href(basePath, {
                kuntailmoitusRef: organisaatioOid,
                oppijaOid: henkilö.oid,
              })}
            >
              {henkilö.sukunimi} {henkilö.etunimet}
            </Link>
          ),
          icon: !ainaNäytetävätIlmoitukset.includes(ilmoitus.id) ? (
            <OpiskeluhistoriaTapahtumaIcon color="blue" />
          ) : null,
        },
        {
          value: ilmoitus.aikaleima,
          display: formatNullableDate(ilmoitus.aikaleima),
        },
        {
          value: organisaatioNimi(ilmoitus.tekijä.organisaatio),
        },
        {
          value: henkilö.syntymäaika,
          display: formatNullableDate(henkilö.syntymäaika),
        },
        fromNullableValue(
          perusopetuksenJälkeisetOpiskeluoikeustiedot(
            tiedot.oppija.opiskeluoikeudet,
          ),
        ),
      ],
    }))
  }
