import { Opiskeluoikeudentyyppi } from "../../state/apitypes/koodistot"
import {
  KuntailmoituksenOppijanYhteystiedot,
  KuntailmoituksenTekijäLaajatTiedot,
  KuntailmoitusKunta,
} from "../../state/apitypes/kuntailmoitus"
import {
  Maksuttomuus,
  MuuOpetusLaajatTiedot,
  OikeuttaMaksuttomuuteenPidennetty,
  PerusopetuksenJälkeinenLaajatTiedot,
  PerusopetusLaajatTiedot,
  PäätasonSuoritus,
} from "../../state/apitypes/opiskeluoikeus"
import { ISODateTime } from "../../state/common"

// Intersektio tyypeistä OpiskeluoikeusLaajatTiedot ja KansalainenOpiskeluoikeus
export type MinimiOpiskeluoikeus = {
  tyyppi?: Opiskeluoikeudentyyppi
  perusopetusTiedot?: PerusopetusLaajatTiedot
  perusopetuksenJälkeinenTiedot?: PerusopetuksenJälkeinenLaajatTiedot
  muuOpetusTiedot?: MuuOpetusLaajatTiedot
  tarkasteltavaPäätasonSuoritus?: PäätasonSuoritus
  maksuttomuus?: Maksuttomuus[]
  oikeuttaMaksuttomuuteenPidennetty?: OikeuttaMaksuttomuuteenPidennetty[]
}

// Intersektio tyypeistä KuntailmoitusLaajatTiedotLisätiedoilla ja KansalainenKuntailmoitus
export type MinimiOppijaKuntailmoitus = {
  kunta?: KuntailmoitusKunta
  aikaleima?: ISODateTime
  tekijä?: KuntailmoituksenTekijäLaajatTiedot
  oppijanYhteystiedot?: KuntailmoituksenOppijanYhteystiedot
  hakenutMuualle?: boolean
  tietojaKarsittu?: boolean
  aktiivinen?: boolean
}

export const isTurvakiellollinenKuntailmoitus = (
  kuntailmoitus: MinimiOppijaKuntailmoitus,
): boolean =>
  kuntailmoitus.kunta === undefined &&
  kuntailmoitus.tekijä === undefined &&
  kuntailmoitus.oppijanYhteystiedot === undefined &&
  kuntailmoitus.tietojaKarsittu === true
