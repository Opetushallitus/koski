import * as A from "fp-ts/Array"
import * as Ord from "fp-ts/Ord"
import * as string from "fp-ts/string"
import { getLocalizedMaybe } from "../../i18n/i18n"
import { ISODateTime, Oid } from "../common"
import { Kieli, Kunta, Maa } from "./koodistot"
import { OppijaKuntailmoituksillaSuppeatTiedot } from "./oppija"
import { Organisaatio } from "./organisaatiot"

export type KuntailmoitusLaajatTiedot = {
  id?: string
  kunta: KuntailmoitusKunta
  aikaleima?: ISODateTime
  tekijä: KuntailmoituksenTekijäLaajatTiedot
  yhteydenottokieli?: Kieli
  oppijanYhteystiedot?: KuntailmoituksenOppijanYhteystiedot
  hakenutMuualle?: boolean
  onUudempiaIlmoituksiaMuihinKuntiin?: boolean
  aktiivinen?: boolean
  tietojaKarsittu?: boolean
}

export type KuntailmoitusLaajatTiedotOppijaOidilla = KuntailmoitusLaajatTiedot & {
  oppijaOid: Oid
}

export type KuntailmoitusLaajatTiedotLisätiedoilla = KuntailmoitusLaajatTiedot & {
  aktiivinen: boolean
}

export type KuntailmoituksenTekijäLaajatTiedot = {
  organisaatio: Organisaatio
  henkilö?: KuntailmoituksenTekijäHenkilö
}

export type KuntailmoitusSuppeatTiedot = {
  id?: string
  tekijä: KuntailmoituksenTekijäSuppeatTiedot
  kunta: KuntailmoitusKunta
  aikaleima?: ISODateTime
  hakenutMuualle?: boolean
  onUudempiaIlmoituksiaMuihinKuntiin?: boolean
  aktiivinen?: boolean
}

export type LuotuKuntailmoitusSuppeatTiedot = KuntailmoitusSuppeatTiedot & {
  id: string
}

export type KuntailmoituksenTekijäSuppeatTiedot = {
  organisaatio: Organisaatio
}

export type KuntailmoituksenTekijäHenkilö = {
  oid?: Oid
  etunimet?: string
  sukunimi?: string
  kutsumanimi?: string
  email?: string
  puhelinnumero?: string
}

export type KuntailmoitusKunta = Organisaatio & {
  kotipaikka?: Kunta
}

export type KuntailmoituksenOppijanYhteystiedot = {
  puhelinnumero?: string
  email?: string
  lähiosoite?: string
  postinumero?: string
  postitoimipaikka?: string
  maa?: Maa
}

export const isAktiivinenKuntailmoitus = (
  kuntailmoitus: KuntailmoitusLaajatTiedotLisätiedoilla
): boolean => kuntailmoitus.aktiivinen

export const aikaleimaOrd = Ord.contramap(
  (kuntailmoitus: KuntailmoitusLaajatTiedotLisätiedoilla) =>
    kuntailmoitus.aikaleima || "0000-00-00"
)(string.Ord)

export const sortKuntailmoitusLaajatTiedotLisätiedoilla = A.sort(
  Ord.reverse(aikaleimaOrd)
)

export const getNäytettävätIlmoitukset = (
  tiedot: OppijaKuntailmoituksillaSuppeatTiedot
): LuotuKuntailmoitusSuppeatTiedot[] =>
  tiedot.kuntailmoitukset.filter(
    (i) => i.aktiivinen && !i.onUudempiaIlmoituksiaMuihinKuntiin
  )

export const kuntaKotipaikka = (kunta: KuntailmoitusKunta): string =>
  getLocalizedMaybe(kunta.kotipaikka?.nimi) || kunta.oid
