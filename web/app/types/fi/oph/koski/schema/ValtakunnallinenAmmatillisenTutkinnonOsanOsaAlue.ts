import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOsaamispisteissä } from './LaajuusOsaamispisteissa'

/**
 * Valtakunnallisen tutkinnon osan osa-alueen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue`
 */
export type ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue = {
  $class: 'fi.oph.koski.schema.ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue'
  tunniste: Koodistokoodiviite<'ammatillisenoppiaineet', string>
  pakollinen: boolean
  laajuus?: LaajuusOsaamispisteissä
}

export const ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue = (o: {
  tunniste: Koodistokoodiviite<'ammatillisenoppiaineet', string>
  pakollinen: boolean
  laajuus?: LaajuusOsaamispisteissä
}): ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue => ({
  $class:
    'fi.oph.koski.schema.ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue',
  ...o
})

ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue.className =
  'fi.oph.koski.schema.ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue' as const

export const isValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue = (
  a: any
): a is ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue =>
  a?.$class ===
  'fi.oph.koski.schema.ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue'
