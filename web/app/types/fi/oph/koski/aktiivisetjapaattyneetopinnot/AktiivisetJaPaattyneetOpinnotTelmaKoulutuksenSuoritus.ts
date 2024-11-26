import { Koodistokoodiviite } from '../schema/Koodistokoodiviite'
import { LocalizedString } from '../schema/LocalizedString'
import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'
import { Koulutussopimusjakso } from './Koulutussopimusjakso'
import { AktiivisetJaPäättyneetOpinnotTelmaKoulutus } from './AktiivisetJaPaattyneetOpinnotTelmaKoulutus'
import { Toimipiste } from './Toimipiste'
import { Vahvistus } from './Vahvistus'

/**
 * AktiivisetJaPäättyneetOpinnotTelmaKoulutuksenSuoritus
 *
 * @see `fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotTelmaKoulutuksenSuoritus`
 */
export type AktiivisetJaPäättyneetOpinnotTelmaKoulutuksenSuoritus = {
  $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotTelmaKoulutuksenSuoritus'
  tyyppi: Koodistokoodiviite<string, 'telma'>
  suorituskieli: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  koulutussopimukset?: Array<Koulutussopimusjakso>
  koulutusmoduuli: AktiivisetJaPäättyneetOpinnotTelmaKoulutus
  toimipiste?: Toimipiste
  vahvistus?: Vahvistus
}

export const AktiivisetJaPäättyneetOpinnotTelmaKoulutuksenSuoritus = (o: {
  tyyppi: Koodistokoodiviite<string, 'telma'>
  suorituskieli: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  koulutussopimukset?: Array<Koulutussopimusjakso>
  koulutusmoduuli: AktiivisetJaPäättyneetOpinnotTelmaKoulutus
  toimipiste?: Toimipiste
  vahvistus?: Vahvistus
}): AktiivisetJaPäättyneetOpinnotTelmaKoulutuksenSuoritus => ({
  $class:
    'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotTelmaKoulutuksenSuoritus',
  ...o
})

AktiivisetJaPäättyneetOpinnotTelmaKoulutuksenSuoritus.className =
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotTelmaKoulutuksenSuoritus' as const

export const isAktiivisetJaPäättyneetOpinnotTelmaKoulutuksenSuoritus = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotTelmaKoulutuksenSuoritus =>
  a?.$class ===
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotTelmaKoulutuksenSuoritus'
