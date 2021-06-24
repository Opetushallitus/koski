import { ISODate, Oid } from "../common"

export type UusiOppivelvollisuudenKeskeytys = {
  oppijaOid: Oid
  tekijäOrganisaatioOid: Oid
  alku: ISODate
  loppu?: ISODate
}

export type OppivelvollisuudenKeskeytys = {
  alku: ISODate
  loppu?: ISODate
  voimassa: boolean
  tulevaisuudessa: boolean
}

export const isKeskeytysToistaiseksi = (
  keskeytys: OppivelvollisuudenKeskeytys
): boolean => keskeytys.loppu === undefined
