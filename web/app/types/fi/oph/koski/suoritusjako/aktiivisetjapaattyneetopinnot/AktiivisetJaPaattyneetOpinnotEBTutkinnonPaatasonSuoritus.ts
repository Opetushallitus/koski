import { AktiivisetJaPäättyneetOpinnotEBTutkinnonKoulutusmoduuli } from './AktiivisetJaPaattyneetOpinnotEBTutkinnonKoulutusmoduuli'
import { Vahvistus } from './Vahvistus'
import { Toimipiste } from './Toimipiste'
import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'

/**
 * AktiivisetJaPäättyneetOpinnotEBTutkinnonPäätasonSuoritus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotEBTutkinnonPäätasonSuoritus`
 */
export type AktiivisetJaPäättyneetOpinnotEBTutkinnonPäätasonSuoritus = {
  $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotEBTutkinnonPäätasonSuoritus'
  koulutusmoduuli: AktiivisetJaPäättyneetOpinnotEBTutkinnonKoulutusmoduuli
  vahvistus?: Vahvistus
  toimipiste?: Toimipiste
  tyyppi: Koodistokoodiviite<string, 'ebtutkinto'>
}

export const AktiivisetJaPäättyneetOpinnotEBTutkinnonPäätasonSuoritus = (o: {
  koulutusmoduuli: AktiivisetJaPäättyneetOpinnotEBTutkinnonKoulutusmoduuli
  vahvistus?: Vahvistus
  toimipiste?: Toimipiste
  tyyppi: Koodistokoodiviite<string, 'ebtutkinto'>
}): AktiivisetJaPäättyneetOpinnotEBTutkinnonPäätasonSuoritus => ({
  $class:
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotEBTutkinnonPäätasonSuoritus',
  ...o
})

AktiivisetJaPäättyneetOpinnotEBTutkinnonPäätasonSuoritus.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotEBTutkinnonPäätasonSuoritus' as const

export const isAktiivisetJaPäättyneetOpinnotEBTutkinnonPäätasonSuoritus = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotEBTutkinnonPäätasonSuoritus =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotEBTutkinnonPäätasonSuoritus'
