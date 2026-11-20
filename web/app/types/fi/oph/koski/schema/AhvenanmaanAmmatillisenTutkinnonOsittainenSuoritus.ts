import { LocalizedString } from './LocalizedString'
import { Koulutussopimusjakso } from './Koulutussopimusjakso'
import { AmmatillinenTutkintoKoulutus } from './AmmatillinenTutkintoKoulutus'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { OsittaisenAmmatillisenTutkinnonOsanSuoritus } from './OsittaisenAmmatillisenTutkinnonOsanSuoritus'
import { OsaamisenHankkimistapajakso } from './OsaamisenHankkimistapajakso'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { Osaamisalajakso } from './Osaamisalajakso'
import { HenkilövahvistusValinnaisellaPaikkakunnalla } from './HenkilovahvistusValinnaisellaPaikkakunnalla'

/**
 * Oppija suorittaa yhtä tai useampaa tutkinnon osaa, eikä koko tutkintoa. Mikäli opiskelija suorittaa toista osaamisalaa tai tutkintonimikettä erillisessä opiskeluoikeudessa, välitään tieto tällöin tämän rakenteen kautta
 *
 * @see `fi.oph.koski.schema.AhvenanmaanAmmatillisenTutkinnonOsittainenSuoritus`
 */
export type AhvenanmaanAmmatillisenTutkinnonOsittainenSuoritus = {
  $class: 'fi.oph.koski.schema.AhvenanmaanAmmatillisenTutkinnonOsittainenSuoritus'
  keskiarvoSisältääMukautettujaArvosanoja?: boolean
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutussopimukset?: Array<Koulutussopimusjakso>
  ryhmä?: string
  koulutusmoduuli: AmmatillinenTutkintoKoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<OsittaisenAmmatillisenTutkinnonOsanSuoritus>
  osaamisenHankkimistavat?: Array<OsaamisenHankkimistapajakso>
  korotettuKeskiarvoSisältääMukautettujaArvosanoja?: boolean
  toinenTutkintonimike: boolean
  tutkintonimike?: Array<Koodistokoodiviite<'tutkintonimikkeet', string>>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'ahvenanmaanammatillinentutkintoosittainen'
  >
  keskiarvo?: number
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli: Koodistokoodiviite<'kieli', string>
  toinenOsaamisala: boolean
  korotettuOpiskeluoikeusOid?: string
  korotettuKeskiarvo?: number
  suoritustapa: Koodistokoodiviite<
    'ammatillisentutkinnonsuoritustapa',
    'reformi'
  >
  osaamisala?: Array<Osaamisalajakso>
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
}

export const AhvenanmaanAmmatillisenTutkinnonOsittainenSuoritus = (o: {
  keskiarvoSisältääMukautettujaArvosanoja?: boolean
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutussopimukset?: Array<Koulutussopimusjakso>
  ryhmä?: string
  koulutusmoduuli: AmmatillinenTutkintoKoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<OsittaisenAmmatillisenTutkinnonOsanSuoritus>
  osaamisenHankkimistavat?: Array<OsaamisenHankkimistapajakso>
  korotettuKeskiarvoSisältääMukautettujaArvosanoja?: boolean
  toinenTutkintonimike?: boolean
  tutkintonimike?: Array<Koodistokoodiviite<'tutkintonimikkeet', string>>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'ahvenanmaanammatillinentutkintoosittainen'
  >
  keskiarvo?: number
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli: Koodistokoodiviite<'kieli', string>
  toinenOsaamisala?: boolean
  korotettuOpiskeluoikeusOid?: string
  korotettuKeskiarvo?: number
  suoritustapa?: Koodistokoodiviite<
    'ammatillisentutkinnonsuoritustapa',
    'reformi'
  >
  osaamisala?: Array<Osaamisalajakso>
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
}): AhvenanmaanAmmatillisenTutkinnonOsittainenSuoritus => ({
  toinenOsaamisala: false,
  $class:
    'fi.oph.koski.schema.AhvenanmaanAmmatillisenTutkinnonOsittainenSuoritus',
  toinenTutkintonimike: false,
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ahvenanmaanammatillinentutkintoosittainen',
    koodistoUri: 'suorituksentyyppi'
  }),
  suoritustapa: Koodistokoodiviite({
    koodiarvo: 'reformi',
    koodistoUri: 'ammatillisentutkinnonsuoritustapa'
  }),
  ...o
})

AhvenanmaanAmmatillisenTutkinnonOsittainenSuoritus.className =
  'fi.oph.koski.schema.AhvenanmaanAmmatillisenTutkinnonOsittainenSuoritus' as const

export const isAhvenanmaanAmmatillisenTutkinnonOsittainenSuoritus = (
  a: any
): a is AhvenanmaanAmmatillisenTutkinnonOsittainenSuoritus =>
  a?.$class ===
  'fi.oph.koski.schema.AhvenanmaanAmmatillisenTutkinnonOsittainenSuoritus'
