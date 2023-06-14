import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'
import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'
import { Koulutussopimusjakso } from './Koulutussopimusjakso'
import { AktiivisetJaPäättyneetOpinnotValmaKoulutus } from './AktiivisetJaPaattyneetOpinnotValmaKoulutus'
import { Toimipiste } from './Toimipiste'
import { Vahvistus } from './Vahvistus'

/**
 * AktiivisetJaPäättyneetOpinnotValmaKoulutuksenSuoritus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotValmaKoulutuksenSuoritus`
 */
export type AktiivisetJaPäättyneetOpinnotValmaKoulutuksenSuoritus = {
  $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotValmaKoulutuksenSuoritus'
  tyyppi: Koodistokoodiviite<string, 'valma'>
  suorituskieli: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  koulutussopimukset?: Array<Koulutussopimusjakso>
  koulutusmoduuli: AktiivisetJaPäättyneetOpinnotValmaKoulutus
  toimipiste?: Toimipiste
  vahvistus?: Vahvistus
}

export const AktiivisetJaPäättyneetOpinnotValmaKoulutuksenSuoritus = (o: {
  tyyppi: Koodistokoodiviite<string, 'valma'>
  suorituskieli: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  koulutussopimukset?: Array<Koulutussopimusjakso>
  koulutusmoduuli: AktiivisetJaPäättyneetOpinnotValmaKoulutus
  toimipiste?: Toimipiste
  vahvistus?: Vahvistus
}): AktiivisetJaPäättyneetOpinnotValmaKoulutuksenSuoritus => ({
  $class:
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotValmaKoulutuksenSuoritus',
  ...o
})

AktiivisetJaPäättyneetOpinnotValmaKoulutuksenSuoritus.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotValmaKoulutuksenSuoritus' as const

export const isAktiivisetJaPäättyneetOpinnotValmaKoulutuksenSuoritus = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotValmaKoulutuksenSuoritus =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotValmaKoulutuksenSuoritus'
