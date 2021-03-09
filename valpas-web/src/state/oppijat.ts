import * as A from "fp-ts/Array"
import * as Ord from "fp-ts/Ord"
import * as string from "fp-ts/string"
import { Language } from "../i18n/i18n"
import {
  KoodistoKoodiviite,
  OpiskeluoikeudenTila,
  Opiskeluoikeudentyyppi,
} from "./koodistot"
import { ISODate, LocalizedString, Oid } from "./types"

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
  oppijaOid: string
  hakuOid: string
  hakemusOid: string
  hakutapa: KoodistoKoodiviite
  hakutyyppi: KoodistoKoodiviite
  muokattu: string
  hakuNimi: LocalizedString
  email: string
  osoite: string
  matkapuhelin: string
  huoltajanNimi: string
  huoltajanPuhelinnumero: string
  huoltajanSahkoposti: string
  hakutoiveet: Hakutoive[]
}

export type Hakutoive = {
  hakukohdeOid: string
  hakukohdeNimi: LocalizedString
  hakutoivenumero: number
  koulutusNimi: LocalizedString
  hakukohdeOrganisaatio: string
  pisteet: number
  valintatila: string // TODO: Lisää enum Valintatila,
  vastaanottotieto: string // TODO: Lisää enum Vastaanottotieto,
  ilmoittautumistila: string // TODO: Lisää enum Ilmoittautumistila,
  koulutusOid: String
  harkinnanvaraisuus: String // TODO: Arvot?
  hakukohdeKoulutuskoodi: String // TODO: Arvot?
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
