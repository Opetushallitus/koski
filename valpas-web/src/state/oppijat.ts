import * as A from "fp-ts/Array"
import * as Ord from "fp-ts/Ord"
import * as string from "fp-ts/string"
import { Language } from "../i18n/i18n"
import {
  HakemuksentilaKoodistoviite,
  OpiskeluoikeudenTila,
  Opiskeluoikeudentyyppi,
  ValintatietotilaKoodistoviite,
} from "./koodistot"
import { ISODate, LocalizedString, Oid } from "./types"

export type Oppija = {
  henkilö: Henkilö
  opiskeluoikeudet: Opiskeluoikeus[]
  haut?: Haku[]
}

export type Henkilö = {
  oid: Oid
  hetu: string
  syntymäaika: ISODate
  etunimet: string
  sukunimi: string
}

export type Oppilaitos = {
  oid: Oid
  nimi: LocalizedString
}

export type Haku = {
  nimi: LocalizedString
  luotu: ISODate
  tila: HakemuksentilaKoodistoviite
  valintatiedot: Valintatieto[]
}

export type Valintatieto = {
  hakukohdenumero?: number
  hakukohde: Oppilaitos
  tila?: ValintatietotilaKoodistoviite
  pisteet?: number
  alinPistemäärä?: number
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
