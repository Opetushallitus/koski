import * as A from "fp-ts/Array"
import React, { useCallback, useMemo, useState } from "react"
import { FlatButton } from "../../components/buttons/FlatButton"
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
import { Oid } from "../../state/common"
import { formatNullableDate } from "../../utils/date"
import { KunnalleIlmoitettuModal } from "./KunnalleIlmoitettuModal"

export type KunnalleIlmoitetutTableProps = {
  data: OppijaHakutilanteillaSuppeatTiedot[]
  organisaatioOid: Oid
  storageName: string
} & Pick<DataTableProps, "onCountChange">

type ShownKuntailmoitus = {
  kuntailmoitusId: string
  oppijaId: string
}

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

  const [kuntailmoitus, openIlmoitus] = useState<ShownKuntailmoitus>()
  const closeIlmoitus = useCallback(() => openIlmoitus(undefined), [])

  const data = toTableData(props.data, (oppija, ilmoitus) =>
    openIlmoitus(
      ilmoitus.id !== undefined
        ? {
            oppijaId: oppija.oppija.henkilö.oid,
            kuntailmoitusId: ilmoitus.id,
          }
        : undefined,
    ),
  )

  return (
    <>
      <DataTable
        columns={columns}
        data={data}
        onCountChange={props.onCountChange}
        storageName={props.storageName}
        className="kuntailmoitukset"
      />
      {kuntailmoitus && (
        <KunnalleIlmoitettuModal
          organisaatioOid={props.organisaatioOid}
          oppijaOid={kuntailmoitus.oppijaId}
          kuntailmoitusId={kuntailmoitus.kuntailmoitusId}
          onClose={closeIlmoitus}
        />
      )}
    </>
  )
}

const toTableData = (
  data: OppijaHakutilanteillaSuppeatTiedot[],
  onShowIlmoitus: (
    oppija: OppijaHakutilanteillaSuppeatTiedot,
    ilmoitus: KuntailmoitusSuppeatTiedot,
  ) => void,
): Datum[] => A.chain(oppijaToTableData(onShowIlmoitus))(data)

const oppijaToTableData =
  (
    onShowIlmoitus: (
      oppija: OppijaHakutilanteillaSuppeatTiedot,
      ilmoitus: KuntailmoitusSuppeatTiedot,
    ) => void,
  ) =>
  (oppija: OppijaHakutilanteillaSuppeatTiedot): Datum[] =>
    oppija.kuntailmoitukset.map((kuntailmoitus) => ({
      key: [oppija.oppija.henkilö.oid, kuntailmoitus.id || ""],
      values: [
        oppijanNimi(oppija, () => onShowIlmoitus(oppija, kuntailmoitus)),
        syntymäaika(oppija),
        ilmoitettuKunnalle(kuntailmoitus),
        ilmoituksenTekopäivä(kuntailmoitus),
        muuHaku(kuntailmoitus),
      ],
    }))

const oppijanNimi = (
  oppija: OppijaHakutilanteillaSuppeatTiedot,
  onClick: () => void,
): Value => {
  const value = `${oppija.oppija.henkilö.sukunimi} ${oppija.oppija.henkilö.etunimet}`
  return {
    value,
    display: <FlatButton onClick={onClick}>{value}</FlatButton>,
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
