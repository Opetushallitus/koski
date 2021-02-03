import {
  OppilaitosnumeroKoodistoviite,
  PaikkakuntaKoodistoviite,
} from "./koodistot"

export type Oid = `1.${number}.${number}.${number}.${number}.${number}.${number}`
export type ISODate = `${number}-${number}-${number}`

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

export type Organisaatio = {
  oid: Oid
  nimi: LocalizedString
  aktiivinen: boolean
  organisaatiotyypit: string[] // TODO: tyypitä tarkemmin
  oppilaitosnumero?: OppilaitosnumeroKoodistoviite
  kotipaikka?: PaikkakuntaKoodistoviite
  children: Organisaatio[]
}
