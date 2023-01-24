import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { NäyttötutkintoonValmistavanKoulutuksenOsa } from './NayttotutkintoonValmistavanKoulutuksenOsa'

/**
 * Suoritettavan näyttötutkintoon valmistavan koulutuksen osan tiedot
 *
 * @see `fi.oph.koski.schema.NäyttötutkintoonValmistavanKoulutuksenOsanSuoritus`
 */
export type NäyttötutkintoonValmistavanKoulutuksenOsanSuoritus = {
  $class: 'fi.oph.koski.schema.NäyttötutkintoonValmistavanKoulutuksenOsanSuoritus'
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'nayttotutkintoonvalmistavankoulutuksenosa'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: NäyttötutkintoonValmistavanKoulutuksenOsa
}

export const NäyttötutkintoonValmistavanKoulutuksenOsanSuoritus = (o: {
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'nayttotutkintoonvalmistavankoulutuksenosa'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: NäyttötutkintoonValmistavanKoulutuksenOsa
}): NäyttötutkintoonValmistavanKoulutuksenOsanSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'nayttotutkintoonvalmistavankoulutuksenosa',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class:
    'fi.oph.koski.schema.NäyttötutkintoonValmistavanKoulutuksenOsanSuoritus',
  ...o
})

NäyttötutkintoonValmistavanKoulutuksenOsanSuoritus.className =
  'fi.oph.koski.schema.NäyttötutkintoonValmistavanKoulutuksenOsanSuoritus' as const

export const isNäyttötutkintoonValmistavanKoulutuksenOsanSuoritus = (
  a: any
): a is NäyttötutkintoonValmistavanKoulutuksenOsanSuoritus =>
  a?.$class ===
  'fi.oph.koski.schema.NäyttötutkintoonValmistavanKoulutuksenOsanSuoritus'
