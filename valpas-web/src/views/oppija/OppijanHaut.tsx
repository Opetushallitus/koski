import React from "react"
import { IconSection } from "../../components/containers/IconSection"
import { HakuIcon } from "../../components/icons/Icon"
import { Datum } from "../../components/tables/DataTable"
import { LeanTable } from "../../components/tables/LeanTable"
import { TertiaryHeading } from "../../components/typography/headings"
import { formatFixedNumber, getLocalized, t } from "../../i18n/i18n"
import { ValintatietotilaKoodistoviite } from "../../state/koodistot"
import { Haku, Oppija, Valintatieto } from "../../state/oppijat"

export type OppijanHautProps = {
  oppija: Oppija
}

export const OppijanHaut = (props: OppijanHautProps) => (
  <div>
    {props.oppija.haut.map((haku) => (
      <HakuTable key={haku.luotu} haku={haku} />
    ))}
  </div>
)

type HakuTableProps = {
  haku: Haku
}

const HakuTable = (props: HakuTableProps) => (
  <IconSection icon={<HakuIcon color="gray" />}>
    <TertiaryHeading>{getLocalized(props.haku.nimi)}</TertiaryHeading>
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
      data={props.haku.valintatiedot.map(valintatietoToTableValue)}
    />
  </IconSection>
)

const valintatietoToTableValue = (
  valinta: Valintatieto,
  index: number
): Datum => ({
  key: index.toString(),
  values: [
    {
      value: `${valinta.hakukohdenumero}. ${getLocalized(
        valinta.hakukohde.nimi
      )}`,
    },
    {
      value:
        valinta.tila &&
        (ValintatietotilaKoodistoviite.isHyväksytty(valinta.tila)
          ? t("oppija__haut_hyvaksytty")
          : valinta.tila.koodiarvo === "hylätty"
          ? t("oppija__haut_hylatty")
          : null),
    },
    { value: valinta.pisteet ? formatFixedNumber(valinta.pisteet, 2) : "" },
    {
      value: valinta.alinPistemäärä
        ? formatFixedNumber(valinta.alinPistemäärä, 2)
        : "",
    },
  ],
})
