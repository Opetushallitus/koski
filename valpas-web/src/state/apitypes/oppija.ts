import { ISODate, Oid } from "../common"
import { HakuLaajatTiedot, HakuSuppeatTiedot } from "./haku"
import { HenkilöLaajatTiedot, HenkilöSuppeatTiedot } from "./henkilo"
import {
  KuntailmoitusLaajatTiedotLisätiedoilla,
  KuntailmoitusSuppeatTiedot,
  LuotuKuntailmoitusSuppeatTiedot,
} from "./kuntailmoitus"
import {
  OpiskeluoikeusLaajatTiedot,
  OpiskeluoikeusSuppeatTiedot,
} from "./opiskeluoikeus"
import { OppivelvollisuudenKeskeytys } from "./oppivelvollisuudenkeskeytys"
import {
  onOppivelvollisuudestaVapautettu,
  OppivelvollisuudestaVapautus,
} from "./oppivelvollisuudestavapautus"
import { Yhteystiedot, YhteystietojenAlkuperä } from "./yhteystiedot"

export type OppijaHakutilanteillaLaajatTiedot = {
  oppija: OppijaLaajatTiedot
  hakutilanteet: HakuLaajatTiedot[]
  hakutilanneError?: string
  yhteystiedot: Yhteystiedot<YhteystietojenAlkuperä>[]
  kuntailmoitukset: KuntailmoitusLaajatTiedotLisätiedoilla[]
  oppivelvollisuudenKeskeytykset: OppivelvollisuudenKeskeytys[]
  onOikeusTehdäKuntailmoitus?: boolean
}

export type OppijaHakutilanteillaSuppeatTiedot = {
  oppija: OppijaSuppeatTiedot
  hakutilanteet: HakuSuppeatTiedot[]
  hakutilanneError?: string
  kuntailmoitukset: KuntailmoitusSuppeatTiedot[]
  oppivelvollisuudenKeskeytykset: OppivelvollisuudenKeskeytys[]
  lisätiedot: OpiskeluoikeusLisätiedot[]
  isLoadingHakutilanteet?: boolean // Frontendin käyttämä apuproperty
}

export type OppijaLaajatTiedot = {
  henkilö: HenkilöLaajatTiedot
  opiskeluoikeudet: OpiskeluoikeusLaajatTiedot[]
  opiskelee: boolean
  oppivelvollisuusVoimassaAsti: ISODate
  oikeusKoulutuksenMaksuttomuuteenVoimassaAsti: ISODate
  hakeutumisvalvovatOppilaitokset: Oid[]
  oppivelvollisuudestaVapautus?: OppivelvollisuudestaVapautus
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

export const lisätietoMatches =
  (oppijaOid: Oid, opiskeluoikeusOid: Oid, oppilaitosOid: Oid) =>
  (lisätiedot: OpiskeluoikeusLisätiedot) =>
    lisätiedot.oppijaOid === oppijaOid &&
    lisätiedot.opiskeluoikeusOid === opiskeluoikeusOid &&
    oppilaitosOid === oppilaitosOid

export type OppijaKuntailmoituksillaSuppeatTiedot = {
  oppija: OppijaSuppeatTiedot
  kuntailmoitukset: LuotuKuntailmoitusSuppeatTiedot[]
}

export const oppijaOnOppivelvollisuudestaVapautettu = (
  oppija: OppijaLaajatTiedot
): boolean =>
  onOppivelvollisuudestaVapautettu(oppija.oppivelvollisuudestaVapautus)
