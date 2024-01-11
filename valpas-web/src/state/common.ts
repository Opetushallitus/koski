import { Eq } from "fp-ts/lib/Eq"
import * as string from "fp-ts/string"
import {
  OppilaitosnumeroKoodistoviite,
  PaikkakuntaKoodistoviite,
} from "./apitypes/koodistot"

// Tempate literal -tyypitykset aiheuttavat stack overflow'n linttausvaiheessa.
// Korjattaneen Typescriptin versiossa 4.2, väliaikaisesti mennään yksinkertaisemmalla tyypityksellä.
// export type Oid = `1.${number}.${number}.${number}.${number}.${number}.${number}`
// export type ISODate = `${number}-${number}-${number}`
export type Oid = string
export type ISODate = string
export type ISODateTime = string
export type Hetu = string

export type Language = "fi" | "sv" | "en"
export type LocalizedString = Partial<Record<Language, string>>

export type User = {
  oid: string
  username: string
  name: string
  serviceTicket: string
  kansalainen: boolean
  huollettava: boolean
}

export type OrganisaatioHierarkia = {
  oid: Oid
  nimi: LocalizedString
  aktiivinen: boolean
  organisaatiotyypit: string[] // TODO: tyypitä tarkemmin
  oppilaitosnumero?: OppilaitosnumeroKoodistoviite
  kotipaikka?: PaikkakuntaKoodistoviite
  children: OrganisaatioHierarkia[]
}

export type Kayttooikeusrooli =
  | "OPPILAITOS_HAKEUTUMINEN"
  | "OPPILAITOS_SUORITTAMINEN"
  | "OPPILAITOS_MAKSUTTOMUUS"
  | "KUNTA"

export const käyttöoikeusrooliEq: Eq<Kayttooikeusrooli> = string.Eq

export type OrganisaatioJaKayttooikeusrooli = {
  organisaatioHierarkia: OrganisaatioHierarkia
  kayttooikeusrooli: Kayttooikeusrooli
}

export const oppilaitosroolit: Kayttooikeusrooli[] = [
  "OPPILAITOS_HAKEUTUMINEN",
  "OPPILAITOS_SUORITTAMINEN",
]
