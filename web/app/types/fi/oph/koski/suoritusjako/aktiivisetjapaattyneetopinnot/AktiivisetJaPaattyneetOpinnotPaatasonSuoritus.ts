import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'
import { AktiivisetJaPäättyneetOpinnotPäätasonKoulutusmoduuli } from './AktiivisetJaPaattyneetOpinnotPaatasonKoulutusmoduuli'
import { Toimipiste } from './Toimipiste'
import { Vahvistus } from './Vahvistus'

/**
 * AktiivisetJaPäättyneetOpinnotPäätasonSuoritus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotPäätasonSuoritus`
 */
export type AktiivisetJaPäättyneetOpinnotPäätasonSuoritus = {
  $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotPäätasonSuoritus'
  tyyppi: Koodistokoodiviite
  suorituskieli: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  koulutusmoduuli: AktiivisetJaPäättyneetOpinnotPäätasonKoulutusmoduuli
  toimipiste?: Toimipiste
  vahvistus?: Vahvistus
}

export const AktiivisetJaPäättyneetOpinnotPäätasonSuoritus = (o: {
  tyyppi: Koodistokoodiviite
  suorituskieli: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  koulutusmoduuli: AktiivisetJaPäättyneetOpinnotPäätasonKoulutusmoduuli
  toimipiste?: Toimipiste
  vahvistus?: Vahvistus
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
