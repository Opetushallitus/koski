import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'
import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'
import { AktiivisetJaPäättyneetOpinnotVapaanSivistystyönLukutaitokoulutus } from './AktiivisetJaPaattyneetOpinnotVapaanSivistystyonLukutaitokoulutus'
import { Toimipiste } from './Toimipiste'
import { Vahvistus } from './Vahvistus'

/**
 * AktiivisetJaPäättyneetOpinnotVapaanSivistystyönLukutaitokoulutuksenSuoritus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVapaanSivistystyönLukutaitokoulutuksenSuoritus`
 */
export type AktiivisetJaPäättyneetOpinnotVapaanSivistystyönLukutaitokoulutuksenSuoritus =
  {
    $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVapaanSivistystyönLukutaitokoulutuksenSuoritus'
    tyyppi: Koodistokoodiviite<string, 'vstlukutaitokoulutus'>
    suorituskieli: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    koulutusmoduuli: AktiivisetJaPäättyneetOpinnotVapaanSivistystyönLukutaitokoulutus
    toimipiste?: Toimipiste
    vahvistus?: Vahvistus
  }

export const AktiivisetJaPäättyneetOpinnotVapaanSivistystyönLukutaitokoulutuksenSuoritus =
  (o: {
    tyyppi: Koodistokoodiviite<string, 'vstlukutaitokoulutus'>
    suorituskieli: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    koulutusmoduuli: AktiivisetJaPäättyneetOpinnotVapaanSivistystyönLukutaitokoulutus
    toimipiste?: Toimipiste
    vahvistus?: Vahvistus
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
