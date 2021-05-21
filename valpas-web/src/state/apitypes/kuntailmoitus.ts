import { ISODateTime, Oid } from "../common"
import { Kieli, Kunta, Maa } from "./koodistot"
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
