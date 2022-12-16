import { Järjestämismuotojakso } from './Jarjestamismuotojakso'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Koulutussopimusjakso } from './Koulutussopimusjakso'
import { Työssäoppimisjakso } from './Tyossaoppimisjakso'
import { NäyttötutkintoonValmistavaKoulutus } from './NayttotutkintoonValmistavaKoulutus'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { NäyttötutkintoonValmistavanKoulutuksenOsanSuoritus } from './NayttotutkintoonValmistavanKoulutuksenOsanSuoritus'
import { AmmatillinenTutkintoKoulutus } from './AmmatillinenTutkintoKoulutus'
import { OsaamisenHankkimistapajakso } from './OsaamisenHankkimistapajakso'
import { Osaamisalajakso } from './Osaamisalajakso'
import { HenkilövahvistusValinnaisellaPaikkakunnalla } from './HenkilovahvistusValinnaisellaPaikkakunnalla'

/**
 * Suoritettavan näyttötutkintoon valmistavan koulutuksen tiedot
 *
 * @see `fi.oph.koski.schema.NäyttötutkintoonValmistavanKoulutuksenSuoritus`
 */
export type NäyttötutkintoonValmistavanKoulutuksenSuoritus = {
  $class: 'fi.oph.koski.schema.NäyttötutkintoonValmistavanKoulutuksenSuoritus'
  järjestämismuodot?: Array<Järjestämismuotojakso>
  tutkintonimike?: Array<Koodistokoodiviite<'tutkintonimikkeet', string>>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'nayttotutkintoonvalmistavakoulutus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli: Koodistokoodiviite<'kieli', string>
  päättymispäivä?: string
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutussopimukset?: Array<Koulutussopimusjakso>
  ryhmä?: string
  työssäoppimisjaksot?: Array<Työssäoppimisjakso>
  koulutusmoduuli: NäyttötutkintoonValmistavaKoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<NäyttötutkintoonValmistavanKoulutuksenOsanSuoritus>
  tutkinto: AmmatillinenTutkintoKoulutus
  osaamisenHankkimistavat?: Array<OsaamisenHankkimistapajakso>
  osaamisala?: Array<Osaamisalajakso>
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
}

export const NäyttötutkintoonValmistavanKoulutuksenSuoritus = (o: {
  järjestämismuodot?: Array<Järjestämismuotojakso>
  tutkintonimike?: Array<Koodistokoodiviite<'tutkintonimikkeet', string>>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'nayttotutkintoonvalmistavakoulutus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli: Koodistokoodiviite<'kieli', string>
  päättymispäivä?: string
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutussopimukset?: Array<Koulutussopimusjakso>
  ryhmä?: string
  työssäoppimisjaksot?: Array<Työssäoppimisjakso>
  koulutusmoduuli?: NäyttötutkintoonValmistavaKoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<NäyttötutkintoonValmistavanKoulutuksenOsanSuoritus>
  tutkinto: AmmatillinenTutkintoKoulutus
  osaamisenHankkimistavat?: Array<OsaamisenHankkimistapajakso>
  osaamisala?: Array<Osaamisalajakso>
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
}): NäyttötutkintoonValmistavanKoulutuksenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'nayttotutkintoonvalmistavakoulutus',
    koodistoUri: 'suorituksentyyppi'
  }),
  koulutusmoduuli: NäyttötutkintoonValmistavaKoulutus({
    tunniste: Koodistokoodiviite({
      koodiarvo: '999904',
      koodistoUri: 'koulutus'
    })
  }),
  $class: 'fi.oph.koski.schema.NäyttötutkintoonValmistavanKoulutuksenSuoritus',
  ...o
})

export const isNäyttötutkintoonValmistavanKoulutuksenSuoritus = (
  a: any
): a is NäyttötutkintoonValmistavanKoulutuksenSuoritus =>
  a?.$class === 'NäyttötutkintoonValmistavanKoulutuksenSuoritus'
