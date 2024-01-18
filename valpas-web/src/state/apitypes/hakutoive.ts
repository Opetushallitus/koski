import { LocalizedString } from "../common"
import { KoodistoKoodiviite } from "./koodistot"

export type Hakutoive = {
  hakukohdeNimi?: LocalizedString
  organisaatioNimi?: LocalizedString
  koulutusNimi?: LocalizedString
  hakutoivenumero?: number
  pisteet?: number
  alinHyvaksyttyPistemaara?: number
  valintatila?: KoodistoKoodiviite<
    "valpashaunvalintatila",
    HakutoiveValintatilakoodiarvo
  >
  varasijanumero?: number
  vastaanottotieto?: KoodistoKoodiviite<
    "valpasvastaanottotieto",
    HakutoiveVastaanottokoodiarvo
  >
  harkinnanvarainen: boolean
}

export type SuppeaHakutoive = Pick<
  Hakutoive,
  "organisaatioNimi" | "hakutoivenumero" | "valintatila" | "vastaanottotieto"
>

export type HakutoiveValintatilakoodiarvo =
  | "hyvaksytty"
  | "hylatty"
  | "varasijalla"
  | "peruuntunut"
  | "peruttu"
  | "peruutettu"
  | "kesken"

export type HakutoiveVastaanottokoodiarvo = "vastaanotettu" | "ehdollinen"

export const isHyväksytty = (toive: SuppeaHakutoive) =>
  toive.valintatila?.koodiarvo === "hyvaksytty"

export const isVarasijalla = (toive: SuppeaHakutoive) =>
  toive.valintatila?.koodiarvo === "varasijalla"

export const isEiPaikkaa = (toive: SuppeaHakutoive) =>
  toive.valintatila?.koodiarvo === undefined
    ? false
    : !["hyvaksytty", "varasijalla", "kesken"].includes(
        toive.valintatila.koodiarvo,
      )

export const isVastaanotettu = (toive: SuppeaHakutoive) =>
  toive.vastaanottotieto?.koodiarvo === "vastaanotettu" ||
  toive.vastaanottotieto?.koodiarvo === "ehdollinen"

export const isVastaanotettuEhdollisesti = (toive: SuppeaHakutoive) =>
  toive.vastaanottotieto?.koodiarvo === "ehdollinen"
