import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'
import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'
import { AktiivisetJaPäättyneetOpinnotVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus } from './AktiivisetJaPaattyneetOpinnotVapaanSivistystyonMaahanmuuttajienKotoutumiskoulutus'
import { Toimipiste } from './Toimipiste'
import { Vahvistus } from './Vahvistus'

/**
 * AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus`
 */
export type AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus =
  {
    $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus'
    tyyppi: Koodistokoodiviite<string, 'vstmaahanmuuttajienkotoutumiskoulutus'>
    suorituskieli: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    koulutusmoduuli: AktiivisetJaPäättyneetOpinnotVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus
    toimipiste?: Toimipiste
    vahvistus?: Vahvistus
  }

export const AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus =
  (o: {
    tyyppi: Koodistokoodiviite<string, 'vstmaahanmuuttajienkotoutumiskoulutus'>
    suorituskieli: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    koulutusmoduuli: AktiivisetJaPäättyneetOpinnotVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus
    toimipiste?: Toimipiste
    vahvistus?: Vahvistus
  }): AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus => ({
    $class:
      'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus',
    ...o
  })

AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus' as const

export const isAktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus =
  (
    a: any
  ): a is AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus =>
    a?.$class ===
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus'
