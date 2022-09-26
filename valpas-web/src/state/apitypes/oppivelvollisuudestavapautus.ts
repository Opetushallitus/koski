import { ISODate, Oid } from "../common"
import { OrganisaatioWithOid } from "./organisaatiot"

export type OppivelvollisuudestaVapautus = {
  oppijaOid: Oid
  vapautettu: ISODate
  tulevaisuudessa: boolean
  kunta?: OrganisaatioWithOid
  mitätöitymässä: boolean
}

export type OppivelvollisuudestaVapautuksenPohjatiedot = {
  aikaisinPvm: ISODate
  kunnat: OrganisaatioWithOid[]
}

export type UusiOppivelvollisuudestaVapautus = {
  oppijaOid: Oid
  vapautettu: ISODate
  kuntakoodi: String
}

export type OppivelvollisuudestaVapautuksenMitätöinti = {
  oppijaOid: Oid
  kuntakoodi: String
}

export const onOppivelvollisuudestaVapautettu = (
  vapautus?: OppivelvollisuudestaVapautus
): vapautus is OppivelvollisuudestaVapautus =>
  vapautus?.tulevaisuudessa === false && !vapautus.mitätöitymässä
