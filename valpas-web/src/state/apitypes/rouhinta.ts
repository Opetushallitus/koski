import { ISODate, Language, LocalizedString, Oid } from "../common"
import { KoodistoKoodiviite } from "./koodistot"
import { KuntailmoitusSuppeatTiedot } from "./kuntailmoitus"
import { OppivelvollisuudenKeskeytys } from "./oppivelvollisuudenkeskeytys"
import { Suorituksentyyppi } from "./suorituksentyyppi"

export type HetuhakuInput = {
  hetut: string[]
  lang?: Language
  password?: string
}

export type KuntarouhintaInput = {
  kuntaOid: string
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
  aktiivinenKuntailmoitus?: KuntailmoitusSuppeatTiedot
}

export type RouhintaOpiskeluoikeus = {
  suorituksenTyyppi: Suorituksentyyppi
  koulutusmoduulinTunniste: string
  päättymispäivä?: ISODate
  viimeisinTila: KoodistoKoodiviite
  toimipiste: LocalizedString
}
