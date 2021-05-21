import { ISODate, Oid } from "../common"
import { HakuLaajatTiedot, HakuSuppeatTiedot } from "./haku"
import { HenkilöLaajatTiedot, HenkilöSuppeatTiedot } from "./henkilo"
import { KuntailmoitusLaajatTiedotLisätiedoilla } from "./kuntailmoitus"
import {
  OpiskeluoikeusLaajatTiedot,
  OpiskeluoikeusSuppeatTiedot,
} from "./opiskeluoikeus"
import { Yhteystiedot, YhteystietojenAlkuperä } from "./yhteystiedot"

export type OppijaHakutilanteillaLaajatTiedot = {
  oppija: OppijaLaajatTiedot
  hakutilanteet: HakuLaajatTiedot[]
  hakutilanneError?: string
  yhteystiedot: Yhteystiedot<YhteystietojenAlkuperä>[]
  kuntailmoitukset: KuntailmoitusLaajatTiedotLisätiedoilla[]
}

export type OppijaHakutilanteillaSuppeatTiedot = {
  oppija: OppijaSuppeatTiedot
  hakutilanteet: HakuSuppeatTiedot[]
  hakutilanneError?: string
  lisätiedot: OpiskeluoikeusLisätiedot[]
}

export type OppijaLaajatTiedot = {
  henkilö: HenkilöLaajatTiedot
  opiskeluoikeudet: OpiskeluoikeusLaajatTiedot[]
  opiskelee: boolean
  oppivelvollisuusVoimassaAsti?: ISODate
  oikeutetutOppilaitokset: Oid[]
}

export type OppijaSuppeatTiedot = {
  henkilö: HenkilöSuppeatTiedot
  opiskeluoikeudet: OpiskeluoikeusSuppeatTiedot[]
  opiskelee: boolean
  oppivelvollisuusVoimassaAsti?: ISODate
}

export type OpiskeluoikeusLisätiedot = {
  oppijaOid: Oid
  opiskeluoikeusOid: Oid
  oppilaitosOid: Oid
  muuHaku: boolean
}

export const lisätietoMatches = (
  oppijaOid: Oid,
  opiskeluoikeusOid: Oid,
  oppilaitosOid: Oid
) => (lisätiedot: OpiskeluoikeusLisätiedot) =>
  lisätiedot.oppijaOid === oppijaOid &&
  lisätiedot.opiskeluoikeusOid === opiskeluoikeusOid &&
  oppilaitosOid === oppilaitosOid
