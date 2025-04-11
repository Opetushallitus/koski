import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { ValtionhallinnonKielitutkinto } from './ValtionhallinnonKielitutkinto'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { ValtionhallinnonKielitutkinnonKielitaidonSuoritus } from './ValtionhallinnonKielitutkinnonKielitaidonSuoritus'
import { HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla } from './HenkilovahvistusValinnaisellaTittelillaJaValinnaisellaPaikkakunnalla'

/**
 * ValtionhallinnonKielitutkinnonSuoritus
 *
 * @see `fi.oph.koski.schema.ValtionhallinnonKielitutkinnonSuoritus`
 */
export type ValtionhallinnonKielitutkinnonSuoritus = {
  $class: 'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonSuoritus'
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'valtionhallinnonkielitutkinto'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  koulutusmoduuli: ValtionhallinnonKielitutkinto
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<ValtionhallinnonKielitutkinnonKielitaidonSuoritus>
  vahvistus?: HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla
}

export const ValtionhallinnonKielitutkinnonSuoritus = (o: {
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'valtionhallinnonkielitutkinto'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  koulutusmoduuli: ValtionhallinnonKielitutkinto
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<ValtionhallinnonKielitutkinnonKielitaidonSuoritus>
  vahvistus?: HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla
}): ValtionhallinnonKielitutkinnonSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'valtionhallinnonkielitutkinto',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonSuoritus',
  ...o
})

ValtionhallinnonKielitutkinnonSuoritus.className =
  'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonSuoritus' as const

export const isValtionhallinnonKielitutkinnonSuoritus = (
  a: any
): a is ValtionhallinnonKielitutkinnonSuoritus =>
  a?.$class === 'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonSuoritus'
