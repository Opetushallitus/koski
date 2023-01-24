import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * DIA-tutkinnon päättökokeen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.DIAPäättökoe`
 */
export type DIAPäättökoe = {
  $class: 'fi.oph.koski.schema.DIAPäättökoe'
  tunniste: Koodistokoodiviite<
    'diapaattokoe',
    'kirjallinenkoe' | 'suullinenkoe'
  >
}

export const DIAPäättökoe = (o: {
  tunniste: Koodistokoodiviite<
    'diapaattokoe',
    'kirjallinenkoe' | 'suullinenkoe'
  >
}): DIAPäättökoe => ({ $class: 'fi.oph.koski.schema.DIAPäättökoe', ...o })

DIAPäättökoe.className = 'fi.oph.koski.schema.DIAPäättökoe' as const

export const isDIAPäättökoe = (a: any): a is DIAPäättökoe =>
  a?.$class === 'fi.oph.koski.schema.DIAPäättökoe'
