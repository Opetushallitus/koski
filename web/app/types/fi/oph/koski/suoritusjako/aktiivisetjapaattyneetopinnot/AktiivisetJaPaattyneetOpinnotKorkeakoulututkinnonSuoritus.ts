import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'
import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'
import { AktiivisetJaPäättyneetOpinnotKorkeakoulututkinto } from './AktiivisetJaPaattyneetOpinnotKorkeakoulututkinto'
import { Toimipiste } from './Toimipiste'
import { Vahvistus } from './Vahvistus'

/**
 * AktiivisetJaPäättyneetOpinnotKorkeakoulututkinnonSuoritus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulututkinnonSuoritus`
 */
export type AktiivisetJaPäättyneetOpinnotKorkeakoulututkinnonSuoritus = {
  $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulututkinnonSuoritus'
  tyyppi: Koodistokoodiviite<string, 'korkeakoulututkinto'>
  suorituskieli?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  koulutusmoduuli: AktiivisetJaPäättyneetOpinnotKorkeakoulututkinto
  toimipiste?: Toimipiste
  vahvistus?: Vahvistus
}

export const AktiivisetJaPäättyneetOpinnotKorkeakoulututkinnonSuoritus = (o: {
  tyyppi: Koodistokoodiviite<string, 'korkeakoulututkinto'>
  suorituskieli?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  koulutusmoduuli: AktiivisetJaPäättyneetOpinnotKorkeakoulututkinto
  toimipiste?: Toimipiste
  vahvistus?: Vahvistus
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
