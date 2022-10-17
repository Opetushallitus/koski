import bem from "bem-ts"
import * as A from "fp-ts/lib/Array"
import { flow } from "fp-ts/lib/function"
import * as string from "fp-ts/string"
import React, { useMemo } from "react"
import {
  Column,
  Datum,
  DatumKey,
  fromNullableValue,
  Value,
} from "../../components/tables/DataTable"
import {
  SelectableDataTable,
  SelectableDataTableProps,
} from "../../components/tables/SelectableDataTable"
import { t } from "../../i18n/i18n"
import {
  hakeutumisvalvottavatOpiskeluoikeudet,
  OpiskeluoikeusSuppeatTiedot,
} from "../../state/apitypes/opiskeluoikeus"
import {
  OppijaHakutilanteillaSuppeatTiedot,
  OppijaSuppeatTiedot,
} from "../../state/apitypes/oppija"
import { useBasePath } from "../../state/basePath"
import { Oid } from "../../state/common"
import { perusopetuksenJälkeisetOpiskeluoikeustiedot } from "../../state/opiskeluoikeustiedot"
import { formatDate } from "../../utils/date"
import {
  loadingValue,
  nullableDateValue,
  oppijanNimiValue,
} from "../../utils/tableDataFormatters/commonFormatters"
import { hakemuksenTilaValue } from "../../utils/tableDataFormatters/hakemuksentila"
import { muuHakuSwitchValue } from "../../utils/tableDataFormatters/muuHaku"
import { opiskelupaikanVastaanottotietoValue } from "../../utils/tableDataFormatters/opiskelupaikanVastaanotto"
import { valintatilaValue } from "../../utils/tableDataFormatters/valintatila"
import "./HakutilanneTable.less"

const b = bem("hakutilannetable")

export type HakutilanneTableProps = {
  data: OppijaHakutilanteillaSuppeatTiedot[]
  organisaatioOid: string
  onSelect: (oppijaOids: Oid[]) => void
  onSetMuuHaku: SetMuuHakuCallback
} & Pick<SelectableDataTableProps, "onCountChange">

export type SetMuuHakuCallback = (
  oppijaOid: Oid,
  opiskeluoikeus: OpiskeluoikeusSuppeatTiedot,
  state: boolean
) => void

const useOppijaData = (
  organisaatioOid: Oid,
  data: OppijaHakutilanteillaSuppeatTiedot[],
  onSetMuuHaku: SetMuuHakuCallback
) => {
  const basePath = useBasePath()
  return useMemo(
    () =>
      A.flatten(
        data.map(oppijaToTableData(basePath, organisaatioOid, onSetMuuHaku))
      ),
    [data, basePath, organisaatioOid, onSetMuuHaku]
  )
}

export const HakutilanneTable = (props: HakutilanneTableProps) => {
  const data = useOppijaData(
    props.organisaatioOid,
    props.data,
    props.onSetMuuHaku
  )

  const columns: Column[] = useMemo(
    () => [
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
        label: t("hakutilanne__taulu_perusopetus_suoritettu"),
        filter: "dropdown",
      },
      {
        label: t("hakutilanne__taulu_hakemuksen_tila"),
        filter: "dropdown",
      },
      {
        label: t("hakutilanne__taulu_valintatieto"),
        filter: "dropdown",
        indicatorSpace: "auto",
      },
      {
        label: t("hakutilanne__taulu_opiskelupaikka_vastaanotettu"),
        filter: "dropdown",
        indicatorSpace: "auto",
      },
      {
        label: t("hakutilanne__taulu_voimassaolevia_opiskeluoikeuksia"),
        tooltip: t(
          "hakutilanne__taulu_voimassaolevia_opiskeluoikeuksia_tooltip"
        ),
        filter: "dropdown",
        indicatorSpace: "auto",
      },
      {
        label: t("hakutilanne__taulu_muu_haku"),
        tooltip: t("hakutilanne__taulu_muu_haku_tooltip"),
        filter: "dropdown",
      },
    ],
    []
  )

  return (
    <SelectableDataTable
      key={props.organisaatioOid}
      storageName={`hakutilannetaulu-${props.organisaatioOid}`}
      className="hakutilanne"
      columns={columns}
      data={data}
      onCountChange={props.onCountChange}
      peerEquality={oppijaOidsEqual}
      onSelect={(keys) =>
        props.onSelect(hakutilanneKeysToOppijaOids(keys as HakutilanneKey[]))
      }
    />
  )
}

/** Tuple: [Oppijan oid, Opiskeluoikeuden oid] */
type HakutilanneKey = [Oid, Oid]

const createHakutilanneKey = (
  oppija: OppijaSuppeatTiedot,
  opiskeluoikeus: OpiskeluoikeusSuppeatTiedot
): HakutilanneKey => [oppija.henkilö.oid, opiskeluoikeus.oid]

const hakutilanneKeysToOppijaOids = flow(
  A.map((key: HakutilanneKey) => key[0]),
  A.uniq(string.Eq)
)

const oppijaOidsEqual = (a: DatumKey) => (b: DatumKey) => a[0] === b[0]

const oppijaToTableData =
  (
    basePath: string,
    organisaatioOid: string,
    onSetMuuHaku: SetMuuHakuCallback
  ) =>
  (oppija: OppijaHakutilanteillaSuppeatTiedot): Array<Datum> => {
    const henkilö = oppija.oppija.henkilö

    return hakeutumisvalvottavatOpiskeluoikeudet(
      organisaatioOid,
      oppija.oppija.opiskeluoikeudet
    ).map((opiskeluoikeus) => {
      return {
        key: createHakutilanneKey(oppija.oppija, opiskeluoikeus),
        values: [
          oppijanNimi(henkilö, organisaatioOid, basePath),
          nullableDateValue(henkilö.syntymäaika),
          ryhmä(opiskeluoikeus),
          perusopetusSuoritettu(opiskeluoikeus),
          oppija.isLoadingHakutilanteet
            ? loadingValue(true)
            : hakemuksenTilaValue(oppija, basePath),
          oppija.isLoadingHakutilanteet
            ? loadingValue(false)
            : valintatilaValue(oppija.hakutilanteet),
          oppija.isLoadingHakutilanteet
            ? loadingValue(false)
            : opiskelupaikanVastaanottotietoValue(oppija.hakutilanteet),
          fromNullableValue(
            perusopetuksenJälkeisetOpiskeluoikeustiedot(
              oppija.oppija.opiskeluoikeudet
            )
          ),
          muuHakuSwitchValue(oppija, opiskeluoikeus, onSetMuuHaku),
        ],
      }
    })
  }

const oppijanNimi = oppijanNimiValue("hakutilanneRef")

const ryhmä = (oo: OpiskeluoikeusSuppeatTiedot): Value => {
  const ryhmä = oo.tarkasteltavaPäätasonSuoritus?.ryhmä
  const ryhmäValue = ryhmä
    ? {
        value: ryhmä,
        filterValues: [ryhmä],
        display: ryhmä,
      }
    : {
        value: "–",
        filterValues: ["–"],
        display: ryhmä,
      }

  if (
    oo.perusopetusTiedot &&
    oo.perusopetusTiedot.vuosiluokkiinSitomatonOpetus
  ) {
    const vsop = t("hakutilanne__vsop")
    return {
      value: `${vsop} ${ryhmäValue.value}`,
      filterValues: [vsop].concat(ryhmäValue.filterValues),
      display: (
        <>
          <span className={b("vsop")}>{`${vsop}`}</span>
          {` ${ryhmäValue.display}`}
        </>
      ),
    }
  } else {
    if (
      oo.muuOpetusTiedot &&
      oo.tyyppi.koodiarvo == "perusopetukseenvalmistavaopetus"
    ) {
      const valo = t("hakutilanne__valo")
      return {
        value: `${valo}`,
        filterValues: [valo],
        display: (
          <>
            <span className={b("valo")}>{`${valo}`}</span>
          </>
        ),
      }
    } else {
      return ryhmäValue
    }
  }
}

const perusopetusSuoritettu = (oo: OpiskeluoikeusSuppeatTiedot): Value => {
  if (oo.perusopetusTiedot !== undefined) {
    const date = oo.perusopetusTiedot.päättymispäivä
    if (
      date !== undefined &&
      oo.perusopetusTiedot.valmistunutAiemminTaiLähitulevaisuudessa
    ) {
      return oo.perusopetusTiedot.päättymispäiväMerkittyTulevaisuuteen
        ? {
            value: date,
            display: t(
              "perusopetus_suoritettu__valmistuu_tulevaisuudessa_pvm",
              {
                päivämäärä: formatDate(date),
              }
            ),
            filterValues: [
              t("perusopetus_suoritettu__valmistuu_tulevaisuudessa"),
            ],
          }
        : {
            value: date,
            display: formatDate(date),
            filterValues: [t("Kyllä")],
          }
    }
  }
  return fromNullableValue(null, [t("Ei")])
}
