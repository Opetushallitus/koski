import * as R from 'ramda'
import {osasuoritukset, suorituksenTyyppi} from '../suoritus/Suoritus'
import {isOsittaisenAmmatillisenTutkinnonMuunTutkinnonOsanSuoritus} from './TutkinnonOsa'

export const ammattillinenOsittainenTutkintoJaMuuAmmatillisenTutkinnonOsaPuuttuu = suoritus => (
  suorituksenTyyppi(suoritus) === 'ammatillinentutkintoosittainen'
  && R.none(isOsittaisenAmmatillisenTutkinnonMuunTutkinnonOsanSuoritus)(osasuoritukset(suoritus))
)


