import { ISODate, Oid } from "../common"
import { HakuLaajatTiedot } from "./haku"
import { HenkilöLaajatTiedot } from "./henkilo"
import { Kieli, Opiskeluoikeudentyyppi } from "./koodistot"
import {
  KuntailmoituksenOppijanYhteystiedot,
  KuntailmoitusKunta,
} from "./kuntailmoitus"
import {
  Maksuttomuus,
  MuuOpetusLaajatTiedot,
  OikeuttaMaksuttomuuteenPidennetty,
  PerusopetuksenJälkeinenLaajatTiedot,
  PerusopetusLaajatTiedot,
  PäätasonSuoritus,
} from "./opiskeluoikeus"
import { OpiskeluoikeusLisätiedot } from "./oppija"
import { OppivelvollisuudenKeskeytys } from "./oppivelvollisuudenkeskeytys"
import { OppivelvollisuudestaVapautus } from "./oppivelvollisuudestavapautus"
import { Oppilaitos, Organisaatio } from "./organisaatiot"
import { Yhteystiedot, YhteystietojenAlkuperä } from "./yhteystiedot"

export type KansalaisnäkymänTiedot = {
  omatTiedot?: KansalainenOppijatiedot
  huollettavat: KansalainenOppijatiedot[]
  huollettavatIlmanTietoja: KansalainenIlmanTietoja[]
}

export type KansalainenOppijatiedot = {
  oppija: KansalainenOppija
  hakutilanteet: HakuLaajatTiedot[]
  hakutilanneError?: string
  yhteystiedot: Yhteystiedot<YhteystietojenAlkuperä>[]
  kuntailmoitukset: KansalainenKuntailmoitus[]
  oppivelvollisuudenKeskeytykset: OppivelvollisuudenKeskeytys[]
  oppivelvollisuudestaVapautus?: OppivelvollisuudestaVapautus
  lisätiedot: OpiskeluoikeusLisätiedot[]
}

export type KansalainenOppija = {
  henkilö: HenkilöLaajatTiedot
  opiskeluoikeudet: KansalainenOpiskeluoikeus[]
  oppivelvollisuusVoimassaAsti: ISODate
  oikeusKoulutuksenMaksuttomuuteenVoimassaAsti: ISODate
  opiskelee: boolean
}

export type KansalainenOpiskeluoikeus = {
  oid: Oid
  tyyppi?: Opiskeluoikeudentyyppi
  oppilaitos?: Oppilaitos
  oppivelvollisuudenSuorittamiseenKelpaava: Boolean
  perusopetusTiedot?: PerusopetusLaajatTiedot
  perusopetuksenJälkeinenTiedot?: PerusopetuksenJälkeinenLaajatTiedot
  muuOpetusTiedot?: MuuOpetusLaajatTiedot
  päätasonSuoritukset: PäätasonSuoritus[]
  tarkasteltavaPäätasonSuoritus?: PäätasonSuoritus
  maksuttomuus?: Maksuttomuus[]
  oikeuttaMaksuttomuuteenPidennetty?: OikeuttaMaksuttomuuteenPidennetty[]
}

export type KansalainenKuntailmoitus = {
  kunta?: KuntailmoitusKunta
  aikaleima: ISODate
  tekijä?: KansalainenKuntailmoituksenTekijä
  yhteydenottokieli: Kieli
  oppijanYhteystiedot?: KuntailmoituksenOppijanYhteystiedot
  hakenutMuualle?: boolean
  aktiivinen: boolean
}

export type KansalainenKuntailmoituksenTekijä = {
  organisaatio: Organisaatio
}

export type KansalainenIlmanTietoja = {
  nimi: string
  hetu?: string
}

export const isAktiivinenKansalainenKuntailmoitus = (
  kuntailmoitus: KansalainenKuntailmoitus
): boolean => kuntailmoitus.aktiivinen
