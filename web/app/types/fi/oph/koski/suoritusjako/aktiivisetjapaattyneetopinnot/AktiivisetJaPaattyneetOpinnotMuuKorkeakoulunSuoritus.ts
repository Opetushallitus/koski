import { AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunOpinto } from './AktiivisetJaPaattyneetOpinnotMuuKorkeakoulunOpinto'
import { Vahvistus } from './Vahvistus'
import { Toimipiste } from './Toimipiste'
import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'

/**
 * AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunSuoritus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunSuoritus`
 */
export type AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunSuoritus = {
  $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunSuoritus'
  koulutusmoduuli: AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunOpinto
  vahvistus?: Vahvistus
  toimipiste?: Toimipiste
  tyyppi: Koodistokoodiviite<string, 'muukorkeakoulunsuoritus'>
}

export const AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunSuoritus = (o: {
  koulutusmoduuli: AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunOpinto
  vahvistus?: Vahvistus
  toimipiste?: Toimipiste
  tyyppi: Koodistokoodiviite<string, 'muukorkeakoulunsuoritus'>
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
