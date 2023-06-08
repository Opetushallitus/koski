import { AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutus } from './AktiivisetJaPaattyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyonKoulutus'
import { Vahvistus } from './Vahvistus'
import { Toimipiste } from './Toimipiste'
import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'

/**
 * AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus`
 */
export type AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus =
  {
    $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus'
    koulutusmoduuli: AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutus
    vahvistus?: Vahvistus
    toimipiste?: Toimipiste
    tyyppi: Koodistokoodiviite<string, 'vstoppivelvollisillesuunnattukoulutus'>
  }

export const AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus =
  (o: {
    koulutusmoduuli: AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutus
    vahvistus?: Vahvistus
    toimipiste?: Toimipiste
    tyyppi: Koodistokoodiviite<string, 'vstoppivelvollisillesuunnattukoulutus'>
  }): AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus => ({
    $class:
      'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus',
    ...o
  })

AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus' as const

export const isAktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus =
  (
    a: any
  ): a is AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus =>
    a?.$class ===
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus'
