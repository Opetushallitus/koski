import * as A from "fp-ts/Array"
import * as Ord from "fp-ts/Ord"
import * as string from "fp-ts/string"
import { ISODateTime, Oid } from "../common"
import { Kieli, Kunta, Maa } from "./koodistot"
import { oppijallaOnOpiskelupaikka } from "./opiskeluoikeus"
import { OppijaKuntailmoituksillaSuppeatTiedot } from "./oppija"
import { Organisaatio } from "./organisaatiot"

export type KuntailmoitusLaajatTiedot = {
  id?: string
  kunta: Organisaatio
  aikaleima?: ISODateTime
  tekijä: KuntailmoituksenTekijäLaajatTiedot
  yhteydenottokieli?: Kieli
  oppijanYhteystiedot?: KuntailmoituksenOppijanYhteystiedot
  hakenutMuualle?: boolean
}

export type KuntailmoitusLaajatTiedotLisätiedoilla = {
  kuntailmoitus: KuntailmoitusLaajatTiedot
  aktiivinen: boolean
}

export type KuntailmoituksenTekijäLaajatTiedot = {
  organisaatio: Organisaatio
  henkilö?: KuntailmoituksenTekijäHenkilö
}

export type KuntailmoitusSuppeatTiedot = {
  id?: string
  tekijä: KuntailmoituksenTekijäSuppeatTiedot
  kunta: Organisaatio
  aikaleima?: ISODateTime
  hakenutMuualle?: boolean
  onUudempiaIlmoituksiaMuihinKuntiin?: boolean
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
    kuntailmoitus.kuntailmoitus.aikaleima || "0000-00-00"
)(string.Ord)

export const sortKuntailmoitusLaajatTiedotLisätiedoilla = A.sort(
  Ord.reverse(aikaleimaOrd)
)

export const getNäytettävätIlmoitukset = (
  tiedot: OppijaKuntailmoituksillaSuppeatTiedot
): LuotuKuntailmoitusSuppeatTiedot[] => {
  const eiOpiskelupaikkaa = !oppijallaOnOpiskelupaikka(
    tiedot.oppija.opiskeluoikeudet
  )

  return tiedot.kuntailmoitukset.filter(
    (i) => eiOpiskelupaikkaa && !i.onUudempiaIlmoituksiaMuihinKuntiin
  )
}
