import React from "react"
import { NoDataMessage } from "../../components/typography/NoDataMessage"
import { T } from "../../i18n/i18n"
import { Haku, Oppija } from "../../state/oppijat"

export type OppijanHautProps = {
  oppija: Oppija
}

export const OppijanHaut = (props: OppijanHautProps) =>
  props.oppija.haut && props.oppija.haut.length > 0 ? (
    <div>
      {props.oppija.haut.map(
        (_haku) =>
          // <HakuTable key={haku.luotu} haku={haku} />
          "TODO"
      )}
    </div>
  ) : (
    <NoDataMessage>
      <T id="oppija__ei_hakuhistoriaa" />
    </NoDataMessage>
  )

type HakuTableProps = {
  haku: Haku
}

// const HakuTable = (props: HakuTableProps) => (
//   <IconSection icon={<HakuIcon color="gray" />}>
//     <TertiaryHeading>{getLocalized(props.haku.nimi)}</TertiaryHeading>
//     <LeanTable
//       columns={[
//         {
//           label: "Oppilaitos",
//         },
//         {
//           label: "Valinta",
//         },
//         {
//           label: "Pisteet",
//         },
//         {
//           label: "Alin pistemäärä",
//         },
//       ]}
//       data={props.haku.valintatiedot.map(valintatietoToTableValue)}
//     />
//   </IconSection>
// )

// const valintatietoToTableValue = (
//   valinta: Valintatieto,
//   index: number
// ): Datum => ({
//   key: index.toString(),
//   values: [
//     {
//       value: `${valinta.hakukohdenumero}. ${getLocalized(
//         valinta.hakukohde.nimi
//       )}`,
//     },
//     {
//       value:
//         valinta.tila &&
//         (ValintatietotilaKoodistoviite.isHyväksytty(valinta.tila)
//           ? t("oppija__haut_hyvaksytty")
//           : valinta.tila.koodiarvo === "hylätty"
//           ? t("oppija__haut_hylatty")
//           : null),
//     },
//     { value: valinta.pisteet ? formatFixedNumber(valinta.pisteet, 2) : "" },
//     {
//       value: valinta.alinPistemäärä
//         ? formatFixedNumber(valinta.alinPistemäärä, 2)
//         : "",
//     },
//   ],
// })
