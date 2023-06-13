import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'
import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'
import { AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunOpinto } from './AktiivisetJaPaattyneetOpinnotMuuKorkeakoulunOpinto'
import { Toimipiste } from './Toimipiste'
import { Vahvistus } from './Vahvistus'

/**
 * AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunSuoritus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunSuoritus`
 */
export type AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunSuoritus = {
  $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunSuoritus'
  tyyppi: Koodistokoodiviite<string, 'muukorkeakoulunsuoritus'>
  suorituskieli?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  koulutusmoduuli: AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunOpinto
  toimipiste?: Toimipiste
  vahvistus?: Vahvistus
}

export const AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunSuoritus = (o: {
  tyyppi: Koodistokoodiviite<string, 'muukorkeakoulunsuoritus'>
  suorituskieli?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  koulutusmoduuli: AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunOpinto
  toimipiste?: Toimipiste
  vahvistus?: Vahvistus
}): AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunSuoritus => ({
  $class:
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunSuoritus',
  ...o
})

AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunSuoritus.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunSuoritus' as const

export const isAktiivisetJaPäättyneetOpinnotMuuKorkeakoulunSuoritus = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunSuoritus =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunSuoritus'
