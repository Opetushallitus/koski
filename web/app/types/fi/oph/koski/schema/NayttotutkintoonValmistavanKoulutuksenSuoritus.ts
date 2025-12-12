import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Koulutussopimusjakso } from './Koulutussopimusjakso'
import { NäyttötutkintoonValmistavaKoulutus } from './NayttotutkintoonValmistavaKoulutus'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { NäyttötutkintoonValmistavanKoulutuksenOsanSuoritus } from './NayttotutkintoonValmistavanKoulutuksenOsanSuoritus'
import { AmmatillinenTutkintoKoulutus } from './AmmatillinenTutkintoKoulutus'
import { OsaamisenHankkimistapajakso } from './OsaamisenHankkimistapajakso'
import { Järjestämismuotojakso } from './Jarjestamismuotojakso'
import { Työssäoppimisjakso } from './Tyossaoppimisjakso'
import { Osaamisalajakso } from './Osaamisalajakso'
import { HenkilövahvistusValinnaisellaPaikkakunnalla } from './HenkilovahvistusValinnaisellaPaikkakunnalla'

/**
 * Suoritettavan näyttötutkintoon valmistavan koulutuksen tiedot
 *
 * @see `fi.oph.koski.schema.NäyttötutkintoonValmistavanKoulutuksenSuoritus`
 */
export type NäyttötutkintoonValmistavanKoulutuksenSuoritus = {
  $class: 'fi.oph.koski.schema.NäyttötutkintoonValmistavanKoulutuksenSuoritus'
  suorituskieli: Koodistokoodiviite<'kieli', string>
  päättymispäivä?: string
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutussopimukset?: Array<Koulutussopimusjakso>
  koulutusmoduuli: NäyttötutkintoonValmistavaKoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<NäyttötutkintoonValmistavanKoulutuksenOsanSuoritus>
  tutkinto: AmmatillinenTutkintoKoulutus
  osaamisenHankkimistavat?: Array<OsaamisenHankkimistapajakso>
  järjestämismuodot?: Array<Järjestämismuotojakso>
  tutkintonimike?: Array<Koodistokoodiviite<'tutkintonimikkeet', string>>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'nayttotutkintoonvalmistavakoulutus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  ryhmä?: string
  työssäoppimisjaksot?: Array<Työssäoppimisjakso>
  osaamisala?: Array<Osaamisalajakso>
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
}

export const NäyttötutkintoonValmistavanKoulutuksenSuoritus = (o: {
  suorituskieli: Koodistokoodiviite<'kieli', string>
  päättymispäivä?: string
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutussopimukset?: Array<Koulutussopimusjakso>
  koulutusmoduuli?: NäyttötutkintoonValmistavaKoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<NäyttötutkintoonValmistavanKoulutuksenOsanSuoritus>
  tutkinto: AmmatillinenTutkintoKoulutus
  osaamisenHankkimistavat?: Array<OsaamisenHankkimistapajakso>
  järjestämismuodot?: Array<Järjestämismuotojakso>
  tutkintonimike?: Array<Koodistokoodiviite<'tutkintonimikkeet', string>>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'nayttotutkintoonvalmistavakoulutus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  ryhmä?: string
  työssäoppimisjaksot?: Array<Työssäoppimisjakso>
  osaamisala?: Array<Osaamisalajakso>
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
}): NäyttötutkintoonValmistavanKoulutuksenSuoritus => ({
  koulutusmoduuli: NäyttötutkintoonValmistavaKoulutus({
    tunniste: Koodistokoodiviite({
      koodiarvo: '999904',
      koodistoUri: 'koulutus'
    })
  }),
  $class: 'fi.oph.koski.schema.NäyttötutkintoonValmistavanKoulutuksenSuoritus',
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'nayttotutkintoonvalmistavakoulutus',
    koodistoUri: 'suorituksentyyppi'
  }),
  ...o
})

NäyttötutkintoonValmistavanKoulutuksenSuoritus.className =
  'fi.oph.koski.schema.NäyttötutkintoonValmistavanKoulutuksenSuoritus' as const

export const isNäyttötutkintoonValmistavanKoulutuksenSuoritus = (
  a: any
): a is NäyttötutkintoonValmistavanKoulutuksenSuoritus =>
  a?.$class ===
  'fi.oph.koski.schema.NäyttötutkintoonValmistavanKoulutuksenSuoritus'
