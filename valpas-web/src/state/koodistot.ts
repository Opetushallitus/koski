import { LocalizedString } from "./types"

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

const koodiarvoCondition = <K extends KoodistoKoodiviite, S extends string>(
  uri: string,
  truthyValues: S[]
) => (koodiviite: K) =>
  uri === koodiviite.koodistoUri &&
  truthyValues.includes(koodiviite.koodiarvo as S)

export type Opiskeluoikeudentyyppi = KoodistoKoodiviite<"opiskeluoikeudentyyppi">

// TODO: Omakeksittyjä koodistoja. Pitää tsekata mitä löytyy valmiina.
export type OppilaitosnumeroKoodistoviite = KoodistoKoodiviite<"oppilaitosnumero">
export type PaikkakuntaKoodistoviite = KoodistoKoodiviite<"paikkakunta">

export type OpiskeluoikeudenTila = KoodistoKoodiviite<"koskiopiskeluoikeudentila">
