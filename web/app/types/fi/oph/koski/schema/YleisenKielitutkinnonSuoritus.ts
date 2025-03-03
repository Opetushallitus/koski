import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { YleinenKielitutkinto } from './YleinenKielitutkinto'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { YleisenKielitutkinnonOsanSuoritus } from './YleisenKielitutkinnonOsanSuoritus'
import { Päivämäärävahvistus } from './Paivamaaravahvistus'

/**
 * YleisenKielitutkinnonSuoritus
 *
 * @see `fi.oph.koski.schema.YleisenKielitutkinnonSuoritus`
 */
export type YleisenKielitutkinnonSuoritus = {
  $class: 'fi.oph.koski.schema.YleisenKielitutkinnonSuoritus'
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'yleinenkielitutkinto'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  koulutusmoduuli: YleinenKielitutkinto
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<YleisenKielitutkinnonOsanSuoritus>
  vahvistus?: Päivämäärävahvistus
}

export const YleisenKielitutkinnonSuoritus = (o: {
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'yleinenkielitutkinto'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  koulutusmoduuli: YleinenKielitutkinto
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<YleisenKielitutkinnonOsanSuoritus>
  vahvistus?: Päivämäärävahvistus
}): YleisenKielitutkinnonSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'yleinenkielitutkinto',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.YleisenKielitutkinnonSuoritus',
  ...o
})

YleisenKielitutkinnonSuoritus.className =
  'fi.oph.koski.schema.YleisenKielitutkinnonSuoritus' as const

export const isYleisenKielitutkinnonSuoritus = (
  a: any
): a is YleisenKielitutkinnonSuoritus =>
  a?.$class === 'fi.oph.koski.schema.YleisenKielitutkinnonSuoritus'
