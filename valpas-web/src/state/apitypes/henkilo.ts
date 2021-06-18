import { ISODate, Oid } from "../common"

export interface HenkilöTiedot {
  oid: Oid
  syntymäaika?: ISODate
  etunimet: string
  sukunimi: string
}

export type HenkilöLaajatTiedot = HenkilöTiedot & {
  hetu?: string
  turvakielto: boolean
}

export type HenkilöSuppeatTiedot = HenkilöTiedot
