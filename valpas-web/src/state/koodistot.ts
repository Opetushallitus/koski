import { LocalizedString } from "./types"

export type KoodistoKoodiviite<T extends string, S extends string> = {
  koodistoUri: T
  koodiarvo: S
  koodistoVersio?: number
  nimi?: LocalizedString
  lyhytNimi?: LocalizedString
}

export type OppilaitosnumeroKoodistoviite = KoodistoKoodiviite<
  "oppilaitosnumero",
  string
>

export type PaikkakuntaKoodistoviite = KoodistoKoodiviite<"paikkakunta", string>

export type HakemuksentilaKoodistoviite = KoodistoKoodiviite<
  "hakemuksentila",
  "aktiivinen" | "passiivinen" | "puutteellinen" | "luonnos"
>

export type ValintatietotilaKoodistoviite = KoodistoKoodiviite<
  "valintatietotila",
  "hyväksytty" | "varasija" | "hylätty" | "vastaanotettu" | "läsnä"
>

export const isHyväksytty = (
  valintatietotila: ValintatietotilaKoodistoviite
): boolean => {
  const arvo = valintatietotila.koodiarvo
  return arvo === "hyväksytty" || arvo === "vastaanotettu" || arvo === "läsnä"
}

export const isVastaanotettu = (
  valintatietotila: ValintatietotilaKoodistoviite
): boolean => {
  const arvo = valintatietotila.koodiarvo
  return arvo === "vastaanotettu" || arvo === "läsnä"
}

export const isLäsnä = (
  valintatietotila: ValintatietotilaKoodistoviite
): boolean => {
  const arvo = valintatietotila.koodiarvo
  return arvo === "läsnä"
}

export const koodistoviite = <T extends string>(koodistoUri: T) => <
  S extends string
>(
  koodiarvo: S
): KoodistoKoodiviite<T, S> => ({
  koodistoUri,
  koodiarvo,
})
