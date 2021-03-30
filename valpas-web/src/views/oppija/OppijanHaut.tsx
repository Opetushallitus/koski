import bem from "bem-ts"
import * as A from "fp-ts/Array"
import React from "react"
import { IconSection } from "../../components/containers/IconSection"
import { HakuIcon } from "../../components/icons/Icon"
import { ExternalLink } from "../../components/navigation/ExternalLink"
import { Datum } from "../../components/tables/DataTable"
import { LeanTable } from "../../components/tables/LeanTable"
import { TertiaryHeading } from "../../components/typography/headings"
import { NoDataMessage } from "../../components/typography/NoDataMessage"
import { formatFixedNumber, getLocalized, t, T } from "../../i18n/i18n"
import {
  HakuLaajatTiedot,
  Hakutoive,
  OppijaHakutilanteillaLaajatTiedot,
} from "../../state/oppijat"
import "./OppijanHaut.less"

const b = bem("oppijanhaut")

export type OppijanHautProps = {
  oppija: OppijaHakutilanteillaLaajatTiedot
}

export const OppijanHaut = (props: OppijanHautProps) => {
  const haut = props.oppija.hakutilanteet
  const error = props.oppija.hakutilanneError
  return error ? (
    <NoDataMessage>
      <T id="oppija__hakuhistoria_virhe" />
    </NoDataMessage>
  ) : A.isNonEmpty(haut) ? (
    <div className={b()}>
      {haut.map((haku) => (
        <HakuTable key={haku.hakuOid} haku={haku} />
      ))}
    </div>
  ) : (
    <NoDataMessage>
      <T id="oppija__ei_hakuhistoriaa" />
    </NoDataMessage>
  )
}

type HakuTableProps = {
  haku: HakuLaajatTiedot
}

const HakuTable = (props: HakuTableProps) => (
  <IconSection icon={<HakuIcon color="gray" />}>
    <TertiaryHeading className={b("hakunimi")}>
      {getLocalized(props.haku.hakuNimi)}{" "}
      <ExternalLink to={props.haku.hakemusUrl}>
        <T
          id={
            props.haku
              ? "hakemuksentila__hakenut"
              : "hakemuksentila__ei_hakenut"
          }
        />
      </ExternalLink>
    </TertiaryHeading>
    <LeanTable
      columns={[
        {
          label: t("oppija__hakukohde"),
        },
        {
          label: t("oppija__valintatilanne"),
        },
        {
          label: t("oppija__pisteet"),
        },
        {
          label: t("oppija__alin_pistemäärä"),
        },
      ]}
      data={props.haku.hakutoiveet.map(hakutoiveToTableValue)}
    />
  </IconSection>
)

const hakutoiveToTableValue = (hakutoive: Hakutoive, index: number): Datum => ({
  key: index.toString(),
  values: [
    {
      value:
        formatOrderNumber(hakutoive.hakutoivenumero) +
        (getLocalized(hakutoive.hakukohdeNimi) || t("tieto_puuttuu")) +
        (hakutoive.koulutusNimi
          ? ", " + getLocalized(hakutoive.koulutusNimi)
          : ""),
      display: (
        <>
          <span>{formatOrderNumber(hakutoive.hakutoivenumero)}</span>
          {hakutoive.hakukohdeNimi ? (
            getLocalized(hakutoive.hakukohdeNimi)
          ) : (
            <NoDataMessage>
              <T id="tieto_puuttuu" />
            </NoDataMessage>
          )}
          {hakutoive.koulutusNimi &&
            ", " + getLocalized(hakutoive.koulutusNimi)}
        </>
      ),
    },
    {
      value:
        hakutoive.hyväksytty === undefined
          ? null
          : hakutoive.hyväksytty === true
          ? t("oppija__haut_hyvaksytty")
          : t("oppija__haut_hylatty"),
    },
    { value: formatFixedNumber(hakutoive.pisteet, 2) },
    { value: formatFixedNumber(hakutoive.minValintapisteet, 2) },
  ],
})

const formatOrderNumber = (n?: number): string =>
  n !== undefined ? `${n}. ` : ""
