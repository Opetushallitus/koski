import { SuoritetutTutkinnotKorkeakoulututkinto } from './SuoritetutTutkinnotKorkeakoulututkinto'
import { Toimipiste } from './Toimipiste'
import { Vahvistus } from './Vahvistus'
import { Koodistokoodiviite } from '../schema/Koodistokoodiviite'
import { LocalizedString } from '../schema/LocalizedString'

/**
 * SuoritetutTutkinnotKorkeakoulututkinnonSuoritus
 *
 * @see `fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotKorkeakoulututkinnonSuoritus`
 */
export type SuoritetutTutkinnotKorkeakoulututkinnonSuoritus = {
  $class: 'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotKorkeakoulututkinnonSuoritus'
  koulutusmoduuli: SuoritetutTutkinnotKorkeakoulututkinto
  toimipiste?: Toimipiste
  vahvistus?: Vahvistus
  tyyppi: Koodistokoodiviite<string, 'korkeakoulututkinto'>
}

export const SuoritetutTutkinnotKorkeakoulututkinnonSuoritus = (o: {
  koulutusmoduuli: SuoritetutTutkinnotKorkeakoulututkinto
  toimipiste?: Toimipiste
  vahvistus?: Vahvistus
  tyyppi: Koodistokoodiviite<string, 'korkeakoulututkinto'>
}): SuoritetutTutkinnotKorkeakoulututkinnonSuoritus => ({
  $class:
    'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotKorkeakoulututkinnonSuoritus',
  ...o
})

SuoritetutTutkinnotKorkeakoulututkinnonSuoritus.className =
  'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotKorkeakoulututkinnonSuoritus' as const

export const isSuoritetutTutkinnotKorkeakoulututkinnonSuoritus = (
  a: any
): a is SuoritetutTutkinnotKorkeakoulututkinnonSuoritus =>
  a?.$class ===
  'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotKorkeakoulututkinnonSuoritus'
