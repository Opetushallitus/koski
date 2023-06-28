import { LocalizedString } from "../common"

export type KoodistoKoodiviite<
  T extends string = string,
  S extends string = string
> = {
  koodistoUri: T
  koodiarvo: S
  koodistoVersio?: number
  nimi?: LocalizedString
  lyhytNimi?: LocalizedString
}

// @ts-ignore
const koodiarvoCondition =
  <K extends KoodistoKoodiviite, S extends string>(
    uri: string,
    truthyValues: S[]
  ) =>
  (koodiviite: K) =>
    uri === koodiviite.koodistoUri &&
    truthyValues.includes(koodiviite.koodiarvo as S)

export type Opiskeluoikeudentyyppi = KoodistoKoodiviite<
  "opiskeluoikeudentyyppi",
  | "aikuistenperusopetus"
  | "ammatillinenkoulutus"
  | "diatutkinto"
  | "esiopetus"
  | "ibtutkinto"
  | "internationalschool"
  | "europeanschoolofhelsinki"
  | "korkeakoulutus"
  | "lukiokoulutus"
  | "luva"
  | "perusopetukseenvalmistavaopetus"
  | "perusopetuksenlisaopetus"
  | "perusopetus"
  | "tuva"
  | "vapaansivistystyonkoulutus"
  | "ylioppilastutkinto"
>

export type Kieli = KoodistoKoodiviite<"kieli", "FI" | "SV">
export type Maa = KoodistoKoodiviite<"maatjavaltiot2">
export type Kunta = KoodistoKoodiviite<"kunta">

// TODO: Omakeksittyjä koodistoja. Pitää tsekata mitä löytyy valmiina.
export type OppilaitosnumeroKoodistoviite =
  KoodistoKoodiviite<"oppilaitosnumero">
export type PaikkakuntaKoodistoviite = KoodistoKoodiviite<"paikkakunta">
