import { Järjestämismuotojakso } from './Jarjestamismuotojakso'
import { SuoritetutTutkinnotKoodistokoodiviite } from './SuoritetutTutkinnotKoodistokoodiviite'
import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'
import { Koulutussopimusjakso } from './Koulutussopimusjakso'
import { Työssäoppimisjakso } from './Tyossaoppimisjakso'
import { SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli } from './SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli'
import { Toimipiste } from './Toimipiste'
import { OsaamisenHankkimistapajakso } from './OsaamisenHankkimistapajakso'
import { SuoritetutTutkinnotOsaamisalajakso } from './SuoritetutTutkinnotOsaamisalajakso'
import { Vahvistus } from './Vahvistus'

/**
 * SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenSuoritus
 *
 * @see `fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenSuoritus`
 */
export type SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenSuoritus = {
  $class: 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenSuoritus'
  toinenTutkintonimike?: boolean
  järjestämismuodot?: Array<Järjestämismuotojakso>
  tutkintonimike?: Array<SuoritetutTutkinnotKoodistokoodiviite>
  tyyppi: Koodistokoodiviite<string, 'ammatillinentutkintoosittainen'>
  suorituskieli?: SuoritetutTutkinnotKoodistokoodiviite
  toinenOsaamisala?: boolean
  korotettuOpiskeluoikeusOid?: string
  suoritustapa?: SuoritetutTutkinnotKoodistokoodiviite
  koulutussopimukset?: Array<Koulutussopimusjakso>
  työssäoppimisjaksot?: Array<Työssäoppimisjakso>
  koulutusmoduuli: SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli
  toimipiste?: Toimipiste
  osaamisenHankkimistavat?: Array<OsaamisenHankkimistapajakso>
  osaamisala?: Array<SuoritetutTutkinnotOsaamisalajakso>
  vahvistus?: Vahvistus
}

export const SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenSuoritus = (o: {
  toinenTutkintonimike?: boolean
  järjestämismuodot?: Array<Järjestämismuotojakso>
  tutkintonimike?: Array<SuoritetutTutkinnotKoodistokoodiviite>
  tyyppi: Koodistokoodiviite<string, 'ammatillinentutkintoosittainen'>
  suorituskieli?: SuoritetutTutkinnotKoodistokoodiviite
  toinenOsaamisala?: boolean
  korotettuOpiskeluoikeusOid?: string
  suoritustapa?: SuoritetutTutkinnotKoodistokoodiviite
  koulutussopimukset?: Array<Koulutussopimusjakso>
  työssäoppimisjaksot?: Array<Työssäoppimisjakso>
  koulutusmoduuli: SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli
  toimipiste?: Toimipiste
  osaamisenHankkimistavat?: Array<OsaamisenHankkimistapajakso>
  osaamisala?: Array<SuoritetutTutkinnotOsaamisalajakso>
  vahvistus?: Vahvistus
}): SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenSuoritus => ({
  $class:
    'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenSuoritus',
  ...o
})

SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenSuoritus.className =
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenSuoritus' as const

export const isSuoritetutTutkinnotAmmatillisenTutkinnonOsittainenSuoritus = (
  a: any
): a is SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenSuoritus =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenSuoritus'
