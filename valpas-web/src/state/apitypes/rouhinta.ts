import { ISODate, Language, LocalizedString, Oid } from "../common"
import { KoodistoKoodiviite } from "./koodistot"
import { OppivelvollisuudenKeskeytys } from "./oppivelvollisuudenkeskeytys"
import { Suorituksentyyppi } from "./suorituksentyyppi"

export type HetuhakuInput = {
  hetut: string[]
  lang?: Language
  password?: string
}

export type KuntarouhintaInput = {
  kunta: string
  lang?: Language
  password?: string
}

export type KuntarouhinnanTulos = {
  eiOppivelvollisuuttaSuorittavat: RouhintaOppivelvollinen[]
}

export type RouhintaOppivelvollinen = {
  oppijanumero: Oid
  etunimet: string
  sukunimi: string
  syntymäaika?: ISODate
  hetu?: string
  viimeisinOppivelvollisuudenSuorittamiseenKelpaavaOpiskeluoikeus?: RouhintaOpiskeluoikeus
  oppivelvollisuudenKeskeytys: OppivelvollisuudenKeskeytys[]
  vainOppijanumerorekisterissä: boolean
}

export type RouhintaOpiskeluoikeus = {
  suorituksenTyyppi: Suorituksentyyppi
  päättymispäivä?: ISODate
  viimeisinTila: KoodistoKoodiviite
  toimipiste: LocalizedString
}
