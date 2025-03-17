import { ValtionhallinnonKielitutkinnonArviointi } from './ValtionhallinnonKielitutkinnonArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { ValtionhallinnonKielitutkinnonSuullinenKielitaito } from './ValtionhallinnonKielitutkinnonSuullinenKielitaito'
import { ValtionhallinnonKielitutkinnonSuullisenKielitaidonOsakokeenSuoritus } from './ValtionhallinnonKielitutkinnonSuullisenKielitaidonOsakokeenSuoritus'

/**
 * ValtionhallinnonKielitutkinnonSuullisenKielitaidonSuoritus
 *
 * @see `fi.oph.koski.schema.ValtionhallinnonKielitutkinnonSuullisenKielitaidonSuoritus`
 */
export type ValtionhallinnonKielitutkinnonSuullisenKielitaidonSuoritus = {
  $class: 'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonSuullisenKielitaidonSuoritus'
  arviointi?: Array<ValtionhallinnonKielitutkinnonArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'valtionhallinnonkielitaito'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  koulutusmoduuli: ValtionhallinnonKielitutkinnonSuullinenKielitaito
  osasuoritukset?: Array<ValtionhallinnonKielitutkinnonSuullisenKielitaidonOsakokeenSuoritus>
}

export const ValtionhallinnonKielitutkinnonSuullisenKielitaidonSuoritus = (
  o: {
    arviointi?: Array<ValtionhallinnonKielitutkinnonArviointi>
    tyyppi?: Koodistokoodiviite<
      'suorituksentyyppi',
      'valtionhallinnonkielitaito'
    >
    tila?: Koodistokoodiviite<'suorituksentila', string>
    koulutusmoduuli?: ValtionhallinnonKielitutkinnonSuullinenKielitaito
    osasuoritukset?: Array<ValtionhallinnonKielitutkinnonSuullisenKielitaidonOsakokeenSuoritus>
  } = {}
): ValtionhallinnonKielitutkinnonSuullisenKielitaidonSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'valtionhallinnonkielitaito',
    koodistoUri: 'suorituksentyyppi'
  }),
  koulutusmoduuli: ValtionhallinnonKielitutkinnonSuullinenKielitaito({
    tunniste: Koodistokoodiviite({
      koodiarvo: 'suullinen',
      koodistoUri: 'vktkielitaito'
    })
  }),
  $class:
    'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonSuullisenKielitaidonSuoritus',
  ...o
})

ValtionhallinnonKielitutkinnonSuullisenKielitaidonSuoritus.className =
  'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonSuullisenKielitaidonSuoritus' as const

export const isValtionhallinnonKielitutkinnonSuullisenKielitaidonSuoritus = (
  a: any
): a is ValtionhallinnonKielitutkinnonSuullisenKielitaidonSuoritus =>
  a?.$class ===
  'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonSuullisenKielitaidonSuoritus'
