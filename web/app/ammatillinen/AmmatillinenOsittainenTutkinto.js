import * as R from 'ramda'
import { osasuoritukset, suorituksenTyyppi } from '../suoritus/Suoritus'
import { modelData } from '../editor/EditorModel'

export const ammattillinenOsittainenTutkintoJaMuuAmmatillisenTutkinnonOsaPuuttuu =
  (suoritus) =>
    suorituksenTyyppi(suoritus) === 'ammatillinentutkintoosittainen' &&
    R.isEmpty(osasuoritukset(suoritus)) &&
    !isOstettu(suoritus)

export const isOstettu = (suoritus) => {
  if (!suoritus.parent && !suoritus.parent.parent) return false

  return modelData(suoritus.parent.parent, 'ostettu')
}
