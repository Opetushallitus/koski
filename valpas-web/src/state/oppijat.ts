import * as A from "fp-ts/Array"
import * as Ord from "fp-ts/Ord"
import * as string from "fp-ts/string"
import { Language } from "../i18n/i18n"
import { OpiskeluoikeudenTila, Opiskeluoikeudentyyppi } from "./koodistot"
import { ISODate, ISODateTime, LocalizedString, Oid } from "./types"

export type Oppija = {
  henkilö: Henkilö
  opiskeluoikeudet: Opiskeluoikeus[]
  tiedot: Lisätiedot
  haut?: Haku[]
}

export type Henkilö = {
  oid: Oid
  hetu: string
  syntymäaika?: ISODate
  etunimet: string
  sukunimi: string
}

export type Oppilaitos = {
  oid: Oid
  nimi: LocalizedString
}

export type Haku = {
  hakuOid: Oid
  hakuNimi: LocalizedString
  hakemusOid: Oid
  aktiivinen: boolean
  muokattu: ISODateTime
  hakutoiveet: Hakutoive[]
}

export type Hakutoive = {
  hakutoivenumero?: number
  hakukohdeNimi: LocalizedString
  pisteet: number
  hyväksytty?: boolean
}

export type Lisätiedot = {
  opiskelee: boolean
  oppivelvollisuusVoimassaAsti?: ISODate
}

// Opiskeluoikeus

export type Opiskeluoikeus = {
  oid: Oid
  tyyppi: Opiskeluoikeudentyyppi
  oppilaitos: Oppilaitos
  alkamispäivä?: ISODate
  arvioituPäättymispäivä?: ISODate
  päättymispäivä?: ISODate
  ryhmä?: string
  viimeisinTila?: OpiskeluoikeudenTila
}

const opiskeluoikeusDateOrd = (key: keyof Opiskeluoikeus) =>
  Ord.contramap((o: Opiskeluoikeus) => (o[key] as ISODate) || "0000-00-00")(
    string.Ord
  )

const alkamispäiväOrd = opiskeluoikeusDateOrd("alkamispäivä")
const päättymispäiväOrd = opiskeluoikeusDateOrd("päättymispäivä")
const tyyppiNimiOrd = (lang: Language) =>
  Ord.contramap((o: Opiskeluoikeus) => o.tyyppi.nimi?.[lang] || "")(string.Ord)

export const Opiskeluoikeus = {
  sort: (lang: Language) =>
    A.sortBy<Opiskeluoikeus>([
      Ord.reverse(alkamispäiväOrd),
      Ord.reverse(päättymispäiväOrd),
      tyyppiNimiOrd(lang),
    ]),
}
