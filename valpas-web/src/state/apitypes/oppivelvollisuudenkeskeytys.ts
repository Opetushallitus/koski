import { ISODate, Oid } from "../common"

export type UusiOppivelvollisuudenKeskeytys = {
  oppijaOid: Oid
  tekijäOrganisaatioOid: Oid
  alku: ISODate
  loppu?: ISODate
}
