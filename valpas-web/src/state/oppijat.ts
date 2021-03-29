import * as A from "fp-ts/Array"
import { pipe } from "fp-ts/lib/function"
import * as O from "fp-ts/Option"
import * as Ord from "fp-ts/Ord"
import * as string from "fp-ts/string"
import { Language } from "../i18n/i18n"
import {
  KoodistoKoodiviite,
  Opiskeluoikeudentyyppi,
  ValpasOpiskeluoikeudenTila,
} from "./koodistot"
import { ISODate, ISODateTime, LocalizedString, Oid } from "./types"

export type OppijaHakutilanteilla = {
  oppija: Oppija
  hakutilanteet: Haku[]
  hakutilanneError?: string
  yhteystiedot: Yhteystiedot<YhteystietojenAlkuperä>[]
}

export type Oppija = {
  henkilö: Henkilö
  opiskeluoikeudet: Opiskeluoikeus[]
  opiskelee: boolean
  oppivelvollisuusVoimassaAsti?: ISODate
  valvottavatOpiskeluoikeudet: Oid[]
}

export type Henkilö = {
  oid: Oid
  hetu?: string
  syntymäaika?: ISODate
  etunimet: string
  sukunimi: string
  turvakielto: boolean
}

export type Yhteystiedot<T extends YhteystietojenAlkuperä> = {
  alkuperä: T
  yhteystietoryhmänNimi: LocalizedString
  henkilönimi?: string
  sähköposti?: string
  puhelinnumero?: string
  matkapuhelinnumero?: string
  lähiosoite?: string
  kunta?: string
  postinumero?: string
  maa?: string
}

export type YhteystietojenAlkuperä = AlkuperäHakemukselta | AlkuperäRekisteristä

export type AlkuperäHakemukselta = {
  hakuNimi: LocalizedString
  haunAlkamispaivämäärä: ISODateTime
  hakuOid: Oid
  hakemusOid: Oid
}

export type AlkuperäRekisteristä = {
  alkuperä: KoodistoKoodiviite<"yhteystietojenalkupera">
  tyyppi: KoodistoKoodiviite<"yhteystietotyypit">
}

export type Oppilaitos = {
  oid: Oid
  nimi: LocalizedString
}

export type Haku = {
  hakuOid: Oid
  hakuNimi?: LocalizedString
  hakemusOid: Oid
  aktiivinen: boolean
  muokattu: ISODateTime
  hakutoiveet: Hakutoive[]
  osoite: string
  puhelinnumero: string
  sähköposti: string
  huoltajanNimi?: string
  huoltajanPuhelinnumero?: string
  huoltajanSähköposti?: string
}

export type Hakutoive = {
  hakutoivenumero?: number
  hakukohdeNimi?: LocalizedString
  koulutusNimi?: LocalizedString
  pisteet?: number
  minValintapisteet?: number
  hyväksytty?: boolean
}

export type Opiskeluoikeus = {
  oid: Oid
  tyyppi: Opiskeluoikeudentyyppi
  oppilaitos: Oppilaitos
  alkamispäivä?: ISODate
  arvioituPäättymispäivä?: ISODate
  päättymispäivä?: ISODate
  ryhmä?: string
  tarkastelupäivänTila: ValpasOpiskeluoikeudenTila
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

const muokkausOrd = Ord.contramap((haku: Haku) => haku.muokattu)(string.Ord)

export const Haku = {
  latest: (haut: Haku[]) =>
    pipe(haut, A.sortBy([muokkausOrd]), A.head, O.toNullable),
}

export const Yhteystiedot = {
  isIlmoitettu: (
    yhteystiedot: Yhteystiedot<YhteystietojenAlkuperä>
  ): yhteystiedot is Yhteystiedot<AlkuperäHakemukselta> => {
    const a = yhteystiedot.alkuperä as AlkuperäHakemukselta
    return (
      a.hakuNimi !== undefined &&
      a.haunAlkamispaivämäärä !== undefined &&
      a.hakuOid !== undefined &&
      a.hakemusOid !== undefined
    )
  },

  isVirallinen: (
    yhteystiedot: Yhteystiedot<YhteystietojenAlkuperä>
  ): yhteystiedot is Yhteystiedot<AlkuperäRekisteristä> => {
    const a = yhteystiedot.alkuperä as AlkuperäRekisteristä
    return a.alkuperä !== undefined && a.tyyppi !== undefined
  },
}
