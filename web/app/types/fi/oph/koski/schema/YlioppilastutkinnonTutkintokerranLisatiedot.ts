import { YlioppilastutkinnonTutkintokerta } from './YlioppilastutkinnonTutkintokerta'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Oppilaitos } from './Oppilaitos'

/**
 * YlioppilastutkinnonTutkintokerranLisätiedot
 *
 * @see `fi.oph.koski.schema.YlioppilastutkinnonTutkintokerranLisätiedot`
 */
export type YlioppilastutkinnonTutkintokerranLisätiedot = {
  $class: 'fi.oph.koski.schema.YlioppilastutkinnonTutkintokerranLisätiedot'
  tutkintokerta: YlioppilastutkinnonTutkintokerta
  koulutustausta?: Koodistokoodiviite<'ytrkoulutustausta', string>
  oppilaitos?: Oppilaitos
}

export const YlioppilastutkinnonTutkintokerranLisätiedot = (o: {
  tutkintokerta: YlioppilastutkinnonTutkintokerta
  koulutustausta?: Koodistokoodiviite<'ytrkoulutustausta', string>
  oppilaitos?: Oppilaitos
}): YlioppilastutkinnonTutkintokerranLisätiedot => ({
  $class: 'fi.oph.koski.schema.YlioppilastutkinnonTutkintokerranLisätiedot',
  ...o
})

YlioppilastutkinnonTutkintokerranLisätiedot.className =
  'fi.oph.koski.schema.YlioppilastutkinnonTutkintokerranLisätiedot' as const

export const isYlioppilastutkinnonTutkintokerranLisätiedot = (
  a: any
): a is YlioppilastutkinnonTutkintokerranLisätiedot =>
  a?.$class ===
  'fi.oph.koski.schema.YlioppilastutkinnonTutkintokerranLisätiedot'
