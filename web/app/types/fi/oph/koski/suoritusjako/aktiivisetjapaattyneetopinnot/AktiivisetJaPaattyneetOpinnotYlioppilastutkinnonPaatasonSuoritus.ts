import { AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli } from './AktiivisetJaPaattyneetOpinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli'
import { Toimipiste } from './Toimipiste'
import { Vahvistus } from './Vahvistus'
import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'

/**
 * AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonPäätasonSuoritus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonPäätasonSuoritus`
 */
export type AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonPäätasonSuoritus = {
  $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonPäätasonSuoritus'
  koulutusmoduuli: AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli
  toimipiste?: Toimipiste
  vahvistus?: Vahvistus
  tyyppi: Koodistokoodiviite
}

export const AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonPäätasonSuoritus =
  (o: {
    koulutusmoduuli: AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli
    toimipiste?: Toimipiste
    vahvistus?: Vahvistus
    tyyppi: Koodistokoodiviite
  }): AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonPäätasonSuoritus => ({
    $class:
      'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonPäätasonSuoritus',
    ...o
  })

AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonPäätasonSuoritus.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonPäätasonSuoritus' as const

export const isAktiivisetJaPäättyneetOpinnotYlioppilastutkinnonPäätasonSuoritus =
  (
    a: any
  ): a is AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonPäätasonSuoritus =>
    a?.$class ===
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonPäätasonSuoritus'
