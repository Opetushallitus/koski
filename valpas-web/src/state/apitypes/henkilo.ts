import { ISODate, Oid } from "../common"
import { Kunta } from "./koodistot"

export interface HenkilöTiedot {
  oid: Oid
  syntymäaika?: ISODate
  etunimet: string
  sukunimi: string
}

export type HenkilöLaajatTiedot = HenkilöTiedot & {
  hetu?: string
  turvakielto: boolean
  kotikunta?: Kunta
}

export type HenkilöSuppeatTiedot = HenkilöTiedot
