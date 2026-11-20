import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Koulutussopimusjakso } from './Koulutussopimusjakso'
import { AmmatillinenTutkintoKoulutus } from './AmmatillinenTutkintoKoulutus'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { AmmatillisenTutkinnonOsanSuoritus } from './AmmatillisenTutkinnonOsanSuoritus'
import { OsaamisenHankkimistapajakso } from './OsaamisenHankkimistapajakso'
import { Osaamisalajakso } from './Osaamisalajakso'
import { HenkilövahvistusValinnaisellaPaikkakunnalla } from './HenkilovahvistusValinnaisellaPaikkakunnalla'

/**
 * Suoritettavan ammatillisen tutkinnon tiedot
 *
 * @see `fi.oph.koski.schema.AhvenanmaanAmmatillisenTutkinnonSuoritus`
 */
export type AhvenanmaanAmmatillisenTutkinnonSuoritus = {
  $class: 'fi.oph.koski.schema.AhvenanmaanAmmatillisenTutkinnonSuoritus'
  suorituskieli: Koodistokoodiviite<'kieli', string>
  keskiarvoSisältääMukautettujaArvosanoja?: boolean
  suoritustapa: Koodistokoodiviite<
    'ammatillisentutkinnonsuoritustapa',
    'reformi'
  >
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutussopimukset?: Array<Koulutussopimusjakso>
  ryhmä?: string
  koulutusmoduuli: AmmatillinenTutkintoKoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<AmmatillisenTutkinnonOsanSuoritus>
  osaamisenHankkimistavat?: Array<OsaamisenHankkimistapajakso>
  tutkintonimike?: Array<Koodistokoodiviite<'tutkintonimikkeet', string>>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'ahvenanmaanammatillinentutkinto'
  >
  keskiarvo?: number
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  osaamisala?: Array<Osaamisalajakso>
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
}

export const AhvenanmaanAmmatillisenTutkinnonSuoritus = (o: {
  suorituskieli: Koodistokoodiviite<'kieli', string>
  keskiarvoSisältääMukautettujaArvosanoja?: boolean
  suoritustapa?: Koodistokoodiviite<
    'ammatillisentutkinnonsuoritustapa',
    'reformi'
  >
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutussopimukset?: Array<Koulutussopimusjakso>
  ryhmä?: string
  koulutusmoduuli: AmmatillinenTutkintoKoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<AmmatillisenTutkinnonOsanSuoritus>
  osaamisenHankkimistavat?: Array<OsaamisenHankkimistapajakso>
  tutkintonimike?: Array<Koodistokoodiviite<'tutkintonimikkeet', string>>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'ahvenanmaanammatillinentutkinto'
  >
  keskiarvo?: number
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  osaamisala?: Array<Osaamisalajakso>
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
}): AhvenanmaanAmmatillisenTutkinnonSuoritus => ({
  suoritustapa: Koodistokoodiviite({
    koodiarvo: 'reformi',
    koodistoUri: 'ammatillisentutkinnonsuoritustapa'
  }),
  $class: 'fi.oph.koski.schema.AhvenanmaanAmmatillisenTutkinnonSuoritus',
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ahvenanmaanammatillinentutkinto',
    koodistoUri: 'suorituksentyyppi'
  }),
  ...o
})

AhvenanmaanAmmatillisenTutkinnonSuoritus.className =
  'fi.oph.koski.schema.AhvenanmaanAmmatillisenTutkinnonSuoritus' as const

export const isAhvenanmaanAmmatillisenTutkinnonSuoritus = (
  a: any
): a is AhvenanmaanAmmatillisenTutkinnonSuoritus =>
  a?.$class === 'fi.oph.koski.schema.AhvenanmaanAmmatillisenTutkinnonSuoritus'
