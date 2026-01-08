import { YlioppilastutkinnonSisältyväKoe } from './YlioppilastutkinnonSisaltyvaKoe'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { YlioppilastutkinnonTutkintokerranLisätiedot } from './YlioppilastutkinnonTutkintokerranLisatiedot'

/**
 * YlioppilastutkinnonTutkintokokonaisuudenLisätiedot
 *
 * @see `fi.oph.koski.schema.YlioppilastutkinnonTutkintokokonaisuudenLisätiedot`
 */
export type YlioppilastutkinnonTutkintokokonaisuudenLisätiedot = {
  $class: 'fi.oph.koski.schema.YlioppilastutkinnonTutkintokokonaisuudenLisätiedot'
  aiemminSuoritetutKokeet?: Array<YlioppilastutkinnonSisältyväKoe>
  tyyppi?: Koodistokoodiviite<'ytrtutkintokokonaisuudentyyppi', string>
  tila?: Koodistokoodiviite<'ytrtutkintokokonaisuudentila', string>
  tunniste: number
  tutkintokerrat: Array<YlioppilastutkinnonTutkintokerranLisätiedot>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
}

export const YlioppilastutkinnonTutkintokokonaisuudenLisätiedot = (o: {
  aiemminSuoritetutKokeet?: Array<YlioppilastutkinnonSisältyväKoe>
  tyyppi?: Koodistokoodiviite<'ytrtutkintokokonaisuudentyyppi', string>
  tila?: Koodistokoodiviite<'ytrtutkintokokonaisuudentila', string>
  tunniste: number
  tutkintokerrat?: Array<YlioppilastutkinnonTutkintokerranLisätiedot>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
}): YlioppilastutkinnonTutkintokokonaisuudenLisätiedot => ({
  $class:
    'fi.oph.koski.schema.YlioppilastutkinnonTutkintokokonaisuudenLisätiedot',
  tutkintokerrat: [],
  ...o
})

YlioppilastutkinnonTutkintokokonaisuudenLisätiedot.className =
  'fi.oph.koski.schema.YlioppilastutkinnonTutkintokokonaisuudenLisätiedot' as const

export const isYlioppilastutkinnonTutkintokokonaisuudenLisätiedot = (
  a: any
): a is YlioppilastutkinnonTutkintokokonaisuudenLisätiedot =>
  a?.$class ===
  'fi.oph.koski.schema.YlioppilastutkinnonTutkintokokonaisuudenLisätiedot'
