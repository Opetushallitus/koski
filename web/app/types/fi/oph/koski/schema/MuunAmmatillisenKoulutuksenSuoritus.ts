import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { AmmatillinenTutkintoKoulutus } from './AmmatillinenTutkintoKoulutus'
import { Koulutussopimusjakso } from './Koulutussopimusjakso'
import { MuuAmmatillinenKoulutus } from './MuuAmmatillinenKoulutus'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { MuuAmmatillinenOsasuoritus } from './MuuAmmatillinenOsasuoritus'
import { OsaamisenHankkimistapajakso } from './OsaamisenHankkimistapajakso'
import { HenkilövahvistusValinnaisellaPaikkakunnalla } from './HenkilovahvistusValinnaisellaPaikkakunnalla'

/**
 * MuunAmmatillisenKoulutuksenSuoritus
 *
 * @see `fi.oph.koski.schema.MuunAmmatillisenKoulutuksenSuoritus`
 */
export type MuunAmmatillisenKoulutuksenSuoritus = {
  $class: 'fi.oph.koski.schema.MuunAmmatillisenKoulutuksenSuoritus'
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'muuammatillinenkoulutus'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli: Koodistokoodiviite<'kieli', string>
  täydentääTutkintoa?: AmmatillinenTutkintoKoulutus
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutussopimukset?: Array<Koulutussopimusjakso>
  ryhmä?: string
  koulutusmoduuli: MuuAmmatillinenKoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<MuuAmmatillinenOsasuoritus>
  osaamisenHankkimistavat?: Array<OsaamisenHankkimistapajakso>
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
}

export const MuunAmmatillisenKoulutuksenSuoritus = (o: {
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'muuammatillinenkoulutus'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli: Koodistokoodiviite<'kieli', string>
  täydentääTutkintoa?: AmmatillinenTutkintoKoulutus
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutussopimukset?: Array<Koulutussopimusjakso>
  ryhmä?: string
  koulutusmoduuli: MuuAmmatillinenKoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<MuuAmmatillinenOsasuoritus>
  osaamisenHankkimistavat?: Array<OsaamisenHankkimistapajakso>
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
}): MuunAmmatillisenKoulutuksenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'muuammatillinenkoulutus',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.MuunAmmatillisenKoulutuksenSuoritus',
  ...o
})

export const isMuunAmmatillisenKoulutuksenSuoritus = (
  a: any
): a is MuunAmmatillisenKoulutuksenSuoritus =>
  a?.$class === 'fi.oph.koski.schema.MuunAmmatillisenKoulutuksenSuoritus'
