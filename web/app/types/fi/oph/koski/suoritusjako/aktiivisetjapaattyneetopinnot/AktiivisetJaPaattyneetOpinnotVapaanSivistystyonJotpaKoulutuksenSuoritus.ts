import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'
import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'
import { AktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutus } from './AktiivisetJaPaattyneetOpinnotVapaanSivistystyonJotpaKoulutus'
import { Toimipiste } from './Toimipiste'
import { Vahvistus } from './Vahvistus'

/**
 * AktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutuksenSuoritus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutuksenSuoritus`
 */
export type AktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutuksenSuoritus =
  {
    $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutuksenSuoritus'
    tyyppi: Koodistokoodiviite<string, 'vstjotpakoulutus'>
    suorituskieli: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    koulutusmoduuli: AktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutus
    toimipiste?: Toimipiste
    vahvistus?: Vahvistus
  }

export const AktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutuksenSuoritus =
  (o: {
    tyyppi: Koodistokoodiviite<string, 'vstjotpakoulutus'>
    suorituskieli: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    koulutusmoduuli: AktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutus
    toimipiste?: Toimipiste
    vahvistus?: Vahvistus
  }): AktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutuksenSuoritus => ({
    $class:
      'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutuksenSuoritus',
    ...o
  })

AktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutuksenSuoritus.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutuksenSuoritus' as const

export const isAktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutuksenSuoritus =
  (
    a: any
  ): a is AktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutuksenSuoritus =>
    a?.$class ===
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutuksenSuoritus'
