import * as A from "fp-ts/lib/Array"
import { pipe } from "fp-ts/lib/function"
import { isNonEmpty, NonEmptyArray } from "fp-ts/lib/NonEmptyArray"
import * as O from "fp-ts/lib/Option"
import React, { useMemo } from "react"
import { Link } from "react-router-dom"
import { WarningIcon } from "../../components/icons/Icon"
import { ExternalLink } from "../../components/navigation/ExternalLink"
import { DataTable, Datum, Value } from "../../components/tables/DataTable"
import { NotImplemented } from "../../components/typography/NoDataMessage"
import { getLocalized, T, t, Translation } from "../../i18n/i18n"
import { useBasePath } from "../../state/basePath"
import {
  HakuSuppeatTiedot,
  Hakutoive,
  OppijaHakutilanteillaSuppeatTiedot,
  valvottavatOpiskeluoikeudet,
} from "../../state/oppijat"
import { createOppijaPath } from "../../state/paths"
import { Oid } from "../../state/types"
import { nonEmptyEvery } from "../../utils/arrays"
import { formatNullableDate } from "../../utils/date"

export type HakutilanneTableProps = {
  data: OppijaHakutilanteillaSuppeatTiedot[]
  organisaatioOid: string
}

export const HakutilanneTable = (props: HakutilanneTableProps) => {
  const basePath = useBasePath()
  const data = useMemo(
    () =>
      A.flatten(
        props.data.map(oppijaToTableData(basePath, props.organisaatioOid))
      ),
    [props.organisaatioOid, props.data]
  )

  return (
    <DataTable
      className="hakutilanne"
      columns={[
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
        },
        {
          label: t("hakutilanne__taulu_voimassaolevia_opiskeluoikeuksia"),
          filter: "dropdown",
        },
      ]}
      data={data}
    />
  )
}

const oppijaToTableData = (basePath: string, organisaatioOid: string) => (
  oppija: OppijaHakutilanteillaSuppeatTiedot
): Array<Datum> => {
  const henkilö = oppija.oppija.henkilö

  return valvottavatOpiskeluoikeudet(
    organisaatioOid,
    oppija.oppija.opiskeluoikeudet
  ).map((opiskeluoikeus) => ({
    key: opiskeluoikeus.oid,
    values: [
      {
        value: `${henkilö.sukunimi} ${henkilö.etunimet}`,
        display: (
          <Link
            to={createOppijaPath(basePath, {
              organisaatioOid,
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
      {
        value: opiskeluoikeus?.ryhmä,
      },
      hakemuksenTila(oppija, basePath),
      fromNullableValue(valintatila(oppija.hakutilanteet)),
      {
        value: t("hakutilanne__taulu_data_ei_toteutettu"),
        display: (
          <NotImplemented>
            <T id="hakutilanne__taulu_data_ei_toteutettu" />
          </NotImplemented>
        ),
      },
      {
        value: t("hakutilanne__taulu_data_ei_toteutettu"),
        display: (
          <NotImplemented>
            <T id="hakutilanne__taulu_data_ei_toteutettu" />
          </NotImplemented>
        ),
      },
    ],
  }))
}

const hakemuksenTila = (
  oppija: OppijaHakutilanteillaSuppeatTiedot,
  basePath: string
): Value => {
  const { hakutilanteet, hakutilanneError } = oppija
  const oppijaOid = oppija.oppija.henkilö.oid

  const hakemuksenTilaValue = hakemuksenTilaT(
    hakutilanteet.length,
    hakutilanneError
  )
  return {
    value: hakemuksenTilaValue,
    display: hakemuksenTilaDisplay(
      hakutilanteet,
      hakemuksenTilaValue,
      oppijaOid,
      basePath
    ),
  }
}

const hakemuksenTilaT = (
  hakemusCount: number,
  hakutilanneError?: string
): Translation => {
  if (hakutilanneError) return t("oppija__hakuhistoria_virhe")
  else if (hakemusCount == 0) return t("hakemuksentila__ei_hakemusta")
  else if (hakemusCount == 1) return t("hakemuksentila__hakenut")
  else return t("hakemuksentila__n_hakua", { lukumäärä: hakemusCount })
}

const hakemuksenTilaDisplay = (
  hakutilanteet: HakuSuppeatTiedot[],
  hakemuksenTilaValue: Translation,
  oppijaOid: Oid,
  basePath: string
) =>
  pipe(
    A.head(hakutilanteet),
    O.map((hakutilanne) =>
      hakutilanteet.length == 1 ? (
        <ExternalLink to={hakutilanne.hakemusUrl}>
          {hakemuksenTilaValue}
        </ExternalLink>
      ) : (
        <Link to={createOppijaPath(basePath, { oppijaOid })}>
          {hakemuksenTilaValue}
        </Link>
      )
    ),
    O.toNullable
  )

const fromNullableValue = (value: Value | null): Value =>
  value || {
    value: "–",
  }

const valintatila = (haut: HakuSuppeatTiedot[]): Value | null => {
  const hakutoiveetBy = (predicate: (hakutoive: Hakutoive) => boolean) =>
    A.chain((haku: HakuSuppeatTiedot) => haku.hakutoiveet.filter(predicate))(
      haut
    )

  const hyväksytytHakutoiveet = hakutoiveetBy(Hakutoive.isHyväksytty)
  if (isNonEmpty(hyväksytytHakutoiveet)) {
    return hyväksyttyValintatila(hyväksytytHakutoiveet)
  }

  const [varasija] = hakutoiveetBy(Hakutoive.isVarasijalla)
  if (varasija) {
    return {
      value: t("valintatieto__varasija_hakukohde", {
        hakukohde: getLocalized(varasija.organisaatioNimi) || "?",
      }),
    }
  }

  if (
    nonEmptyEvery(haut, (haku) =>
      nonEmptyEvery(haku.hakutoiveet, Hakutoive.isEiPaikkaa)
    )
  ) {
    return {
      value: t("valintatieto__ei_opiskelupaikkaa"),
      icon: <WarningIcon />,
    }
  }

  return null
}

const hyväksyttyValintatila = (
  hyväksytytHakutoiveet: NonEmptyArray<Hakutoive>
): Value => {
  const buildHyväksyttyValue = (hakutoive: Hakutoive) => {
    return {
      value: t("valintatieto__hyväksytty", {
        hakukohde: orderedHakukohde(
          hakutoive.hakutoivenumero,
          t("valintatieto__hakukohde_lc")
        ),
      }),
      display: orderedHakukohde(
        hakutoive.hakutoivenumero,
        getLocalized(hakutoive.organisaatioNimi) || "?"
      ),
    }
  }

  if (hyväksytytHakutoiveet.length === 1) {
    return buildHyväksyttyValue(hyväksytytHakutoiveet[0])
  }

  return {
    value: t("valintatieto__hyväksytty_n_hakutoivetta", {
      lukumäärä: hyväksytytHakutoiveet.length,
    }),
    filterValues: hyväksytytHakutoiveet.map(
      (hakutoive) => buildHyväksyttyValue(hakutoive).value
    ),
  }
}

const orderedHakukohde = (
  hakutoivenumero: number | undefined,
  hakukohde: string
) => (hakutoivenumero ? `${hakutoivenumero}. ${hakukohde}` : hakukohde)
