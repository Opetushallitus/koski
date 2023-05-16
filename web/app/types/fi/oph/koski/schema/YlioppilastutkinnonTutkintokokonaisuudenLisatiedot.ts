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
  tutkintokerrat: Array<YlioppilastutkinnonTutkintokerranLisätiedot>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  tunniste: number
}

export const YlioppilastutkinnonTutkintokokonaisuudenLisätiedot = (o: {
  aiemminSuoritetutKokeet?: Array<YlioppilastutkinnonSisältyväKoe>
  tyyppi?: Koodistokoodiviite<'ytrtutkintokokonaisuudentyyppi', string>
  tila?: Koodistokoodiviite<'ytrtutkintokokonaisuudentila', string>
  tutkintokerrat?: Array<YlioppilastutkinnonTutkintokerranLisätiedot>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  tunniste: number
}): YlioppilastutkinnonTutkintokokonaisuudenLisätiedot => ({
  tutkintokerrat: [],
  $class:
    'fi.oph.koski.schema.YlioppilastutkinnonTutkintokokonaisuudenLisätiedot',
  ...o
})

YlioppilastutkinnonTutkintokokonaisuudenLisätiedot.className =
  'fi.oph.koski.schema.YlioppilastutkinnonTutkintokokonaisuudenLisätiedot' as const

export const isYlioppilastutkinnonTutkintokokonaisuudenLisätiedot = (
  a: any
): a is YlioppilastutkinnonTutkintokokonaisuudenLisätiedot =>
  a?.$class ===
  'fi.oph.koski.schema.YlioppilastutkinnonTutkintokokonaisuudenLisätiedot'
