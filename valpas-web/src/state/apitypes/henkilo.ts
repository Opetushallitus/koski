import { ISODate, Oid } from "../common"

export type HenkilöLaajatTiedot = {
  oid: Oid
  hetu?: string
  syntymäaika?: ISODate
  etunimet: string
  sukunimi: string
  turvakielto: boolean
}

export type HenkilöSuppeatTiedot = {
  oid: Oid
  syntymäaika?: ISODate
  etunimet: string
  sukunimi: string
}
