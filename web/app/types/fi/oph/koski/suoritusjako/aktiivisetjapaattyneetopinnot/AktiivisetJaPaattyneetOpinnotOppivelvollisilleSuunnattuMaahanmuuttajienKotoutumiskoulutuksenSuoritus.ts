import { AktiivisetJaPäättyneetOpinnotVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus } from './AktiivisetJaPaattyneetOpinnotVapaanSivistystyonMaahanmuuttajienKotoutumiskoulutus'
import { Vahvistus } from './Vahvistus'
import { Toimipiste } from './Toimipiste'
import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'

/**
 * AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus`
 */
export type AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus =
  {
    $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus'
    koulutusmoduuli: AktiivisetJaPäättyneetOpinnotVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus
    vahvistus?: Vahvistus
    toimipiste?: Toimipiste
    tyyppi: Koodistokoodiviite<string, 'vstmaahanmuuttajienkotoutumiskoulutus'>
  }

export const AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus =
  (o: {
    koulutusmoduuli: AktiivisetJaPäättyneetOpinnotVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus
    vahvistus?: Vahvistus
    toimipiste?: Toimipiste
    tyyppi: Koodistokoodiviite<string, 'vstmaahanmuuttajienkotoutumiskoulutus'>
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
