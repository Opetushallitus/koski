import { Koodistokoodiviite } from '../schema/Koodistokoodiviite'
import { LocalizedString } from '../schema/LocalizedString'
import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'
import { AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutus } from './AktiivisetJaPaattyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyonKoulutus'
import { Toimipiste } from './Toimipiste'
import { Vahvistus } from './Vahvistus'

/**
 * AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus
 *
 * @see `fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus`
 */
export type AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus =
  {
    $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus'
    tyyppi: Koodistokoodiviite<string, 'vstoppivelvollisillesuunnattukoulutus'>
    suorituskieli: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    koulutusmoduuli: AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutus
    toimipiste?: Toimipiste
    vahvistus?: Vahvistus
  }

export const AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus =
  (o: {
    tyyppi: Koodistokoodiviite<string, 'vstoppivelvollisillesuunnattukoulutus'>
    suorituskieli: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    koulutusmoduuli: AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutus
    toimipiste?: Toimipiste
    vahvistus?: Vahvistus
  }): AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus => ({
    $class:
      'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus',
    ...o
  })

AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus.className =
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus' as const

export const isAktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus =
  (
    a: any
  ): a is AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus =>
    a?.$class ===
    'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus'
