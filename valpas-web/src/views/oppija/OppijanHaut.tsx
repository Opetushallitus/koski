import * as A from "fp-ts/Array"
import React from "react"
import { IconSection } from "../../components/containers/IconSection"
import { HakuIcon } from "../../components/icons/Icon"
import { Datum } from "../../components/tables/DataTable"
import { LeanTable } from "../../components/tables/LeanTable"
import { TertiaryHeading } from "../../components/typography/headings"
import { NoDataMessage } from "../../components/typography/NoDataMessage"
import { formatFixedNumber, getLocalized, t, T } from "../../i18n/i18n"
import { Haku, Hakutoive, Oppija } from "../../state/oppijat"

export type OppijanHautProps = {
  oppija: Oppija
}

export const OppijanHaut = (props: OppijanHautProps) => {
  const haut = props.oppija.haut || []
  return A.isNonEmpty(haut) ? (
    <div>
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
  haku: Haku
}

const HakuTable = (props: HakuTableProps) => (
  <IconSection icon={<HakuIcon color="gray" />}>
    <TertiaryHeading>{getLocalized(props.haku.hakuNimi)}</TertiaryHeading>
    <LeanTable
      columns={[
        {
          label: "Oppilaitos",
        },
        {
          label: "Valinta",
        },
        {
          label: "Pisteet",
        },
        {
          label: "Alin pistemäärä",
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
        getLocalized(hakutoive.hakukohdeNimi),
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
    {
      value: "", // TODO: Alin pistemäärä
    },
  ],
})

const formatOrderNumber = (n?: number): string =>
  n !== undefined ? `${n + 1}. ` : ""
