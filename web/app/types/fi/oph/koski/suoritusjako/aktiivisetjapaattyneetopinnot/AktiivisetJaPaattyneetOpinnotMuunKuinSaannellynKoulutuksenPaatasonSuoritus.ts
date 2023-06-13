import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'
import { AktiivisetJaPäättyneetOpinnotMuuKuinSäänneltyKoulutus } from './AktiivisetJaPaattyneetOpinnotMuuKuinSaanneltyKoulutus'
import { Toimipiste } from './Toimipiste'
import { Vahvistus } from './Vahvistus'

/**
 * AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenPäätasonSuoritus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenPäätasonSuoritus`
 */
export type AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenPäätasonSuoritus =
  {
    $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenPäätasonSuoritus'
    tyyppi: Koodistokoodiviite
    suorituskieli: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    koulutusmoduuli: AktiivisetJaPäättyneetOpinnotMuuKuinSäänneltyKoulutus
    toimipiste?: Toimipiste
    vahvistus?: Vahvistus
  }

export const AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenPäätasonSuoritus =
  (o: {
    tyyppi: Koodistokoodiviite
    suorituskieli: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    koulutusmoduuli: AktiivisetJaPäättyneetOpinnotMuuKuinSäänneltyKoulutus
    toimipiste?: Toimipiste
    vahvistus?: Vahvistus
  }): AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenPäätasonSuoritus => ({
    $class:
      'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenPäätasonSuoritus',
    ...o
  })

AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenPäätasonSuoritus.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenPäätasonSuoritus' as const

export const isAktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenPäätasonSuoritus =
  (
    a: any
  ): a is AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenPäätasonSuoritus =>
    a?.$class ===
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenPäätasonSuoritus'
