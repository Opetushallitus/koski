import { AktiivisetJaPäättyneetOpinnotKorkeakoulututkinto } from './AktiivisetJaPaattyneetOpinnotKorkeakoulututkinto'
import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'
import { Vahvistus } from './Vahvistus'
import { Toimipiste } from './Toimipiste'

/**
 * AktiivisetJaPäättyneetOpinnotKorkeakoulututkinnonSuoritus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulututkinnonSuoritus`
 */
export type AktiivisetJaPäättyneetOpinnotKorkeakoulututkinnonSuoritus = {
  $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulututkinnonSuoritus'
  koulutusmoduuli: AktiivisetJaPäättyneetOpinnotKorkeakoulututkinto
  tyyppi: Koodistokoodiviite<string, 'korkeakoulututkinto'>
  vahvistus?: Vahvistus
  toimipiste?: Toimipiste
}

export const AktiivisetJaPäättyneetOpinnotKorkeakoulututkinnonSuoritus = (o: {
  koulutusmoduuli: AktiivisetJaPäättyneetOpinnotKorkeakoulututkinto
  tyyppi: Koodistokoodiviite<string, 'korkeakoulututkinto'>
  vahvistus?: Vahvistus
  toimipiste?: Toimipiste
}): AktiivisetJaPäättyneetOpinnotKorkeakoulututkinnonSuoritus => ({
  $class:
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulututkinnonSuoritus',
  ...o
})

AktiivisetJaPäättyneetOpinnotKorkeakoulututkinnonSuoritus.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulututkinnonSuoritus' as const

export const isAktiivisetJaPäättyneetOpinnotKorkeakoulututkinnonSuoritus = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotKorkeakoulututkinnonSuoritus =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulututkinnonSuoritus'
