import { AktiivisetJaPäättyneetOpinnotVapaanSivistystyönLukutaitokoulutus } from './AktiivisetJaPaattyneetOpinnotVapaanSivistystyonLukutaitokoulutus'
import { Vahvistus } from './Vahvistus'
import { Toimipiste } from './Toimipiste'
import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'

/**
 * AktiivisetJaPäättyneetOpinnotVapaanSivistystyönLukutaitokoulutuksenSuoritus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVapaanSivistystyönLukutaitokoulutuksenSuoritus`
 */
export type AktiivisetJaPäättyneetOpinnotVapaanSivistystyönLukutaitokoulutuksenSuoritus =
  {
    $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVapaanSivistystyönLukutaitokoulutuksenSuoritus'
    koulutusmoduuli: AktiivisetJaPäättyneetOpinnotVapaanSivistystyönLukutaitokoulutus
    vahvistus?: Vahvistus
    toimipiste?: Toimipiste
    tyyppi: Koodistokoodiviite<string, 'vstlukutaitokoulutus'>
  }

export const AktiivisetJaPäättyneetOpinnotVapaanSivistystyönLukutaitokoulutuksenSuoritus =
  (o: {
    koulutusmoduuli: AktiivisetJaPäättyneetOpinnotVapaanSivistystyönLukutaitokoulutus
    vahvistus?: Vahvistus
    toimipiste?: Toimipiste
    tyyppi: Koodistokoodiviite<string, 'vstlukutaitokoulutus'>
  }): AktiivisetJaPäättyneetOpinnotVapaanSivistystyönLukutaitokoulutuksenSuoritus => ({
    $class:
      'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVapaanSivistystyönLukutaitokoulutuksenSuoritus',
    ...o
  })

AktiivisetJaPäättyneetOpinnotVapaanSivistystyönLukutaitokoulutuksenSuoritus.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVapaanSivistystyönLukutaitokoulutuksenSuoritus' as const

export const isAktiivisetJaPäättyneetOpinnotVapaanSivistystyönLukutaitokoulutuksenSuoritus =
  (
    a: any
  ): a is AktiivisetJaPäättyneetOpinnotVapaanSivistystyönLukutaitokoulutuksenSuoritus =>
    a?.$class ===
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVapaanSivistystyönLukutaitokoulutuksenSuoritus'
