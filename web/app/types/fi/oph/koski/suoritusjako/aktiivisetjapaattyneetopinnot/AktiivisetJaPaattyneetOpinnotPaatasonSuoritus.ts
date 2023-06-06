import { AktiivisetJaPäättyneetOpinnotPäätasonKoulutusmoduuli } from './AktiivisetJaPaattyneetOpinnotPaatasonKoulutusmoduuli'
import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { Vahvistus } from './Vahvistus'
import { Toimipiste } from './Toimipiste'

/**
 * AktiivisetJaPäättyneetOpinnotPäätasonSuoritus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotPäätasonSuoritus`
 */
export type AktiivisetJaPäättyneetOpinnotPäätasonSuoritus = {
  $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotPäätasonSuoritus'
  koulutusmoduuli: AktiivisetJaPäättyneetOpinnotPäätasonKoulutusmoduuli
  tyyppi: Koodistokoodiviite
  vahvistus?: Vahvistus
  toimipiste?: Toimipiste
}

export const AktiivisetJaPäättyneetOpinnotPäätasonSuoritus = (o: {
  koulutusmoduuli: AktiivisetJaPäättyneetOpinnotPäätasonKoulutusmoduuli
  tyyppi: Koodistokoodiviite
  vahvistus?: Vahvistus
  toimipiste?: Toimipiste
}): AktiivisetJaPäättyneetOpinnotPäätasonSuoritus => ({
  $class:
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotPäätasonSuoritus',
  ...o
})

AktiivisetJaPäättyneetOpinnotPäätasonSuoritus.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotPäätasonSuoritus' as const

export const isAktiivisetJaPäättyneetOpinnotPäätasonSuoritus = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotPäätasonSuoritus =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotPäätasonSuoritus'
