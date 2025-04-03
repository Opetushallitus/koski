import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { ValtionhallinnonSuullisenKielitaidonOsakoe } from './ValtionhallinnonSuullisenKielitaidonOsakoe'
import { ValtionhallinnonKielitutkinnonArviointi } from './ValtionhallinnonKielitutkinnonArviointi'

/**
 * ValtionhallinnonKielitutkinnonSuullisenKielitaidonOsakokeenSuoritus
 *
 * @see `fi.oph.koski.schema.ValtionhallinnonKielitutkinnonSuullisenKielitaidonOsakokeenSuoritus`
 */
export type ValtionhallinnonKielitutkinnonSuullisenKielitaidonOsakokeenSuoritus =
  {
    $class: 'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonSuullisenKielitaidonOsakokeenSuoritus'
    tyyppi: Koodistokoodiviite<
      'suorituksentyyppi',
      'valtionhallinnonkielitutkinnonosakoe'
    >
    koulutusmoduuli: ValtionhallinnonSuullisenKielitaidonOsakoe
    arviointi?: Array<ValtionhallinnonKielitutkinnonArviointi>
    tila?: Koodistokoodiviite<'suorituksentila', string>
  }

export const ValtionhallinnonKielitutkinnonSuullisenKielitaidonOsakokeenSuoritus =
  (o: {
    tyyppi?: Koodistokoodiviite<
      'suorituksentyyppi',
      'valtionhallinnonkielitutkinnonosakoe'
    >
    koulutusmoduuli: ValtionhallinnonSuullisenKielitaidonOsakoe
    arviointi?: Array<ValtionhallinnonKielitutkinnonArviointi>
    tila?: Koodistokoodiviite<'suorituksentila', string>
  }): ValtionhallinnonKielitutkinnonSuullisenKielitaidonOsakokeenSuoritus => ({
    $class:
      'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonSuullisenKielitaidonOsakokeenSuoritus',
    tyyppi: Koodistokoodiviite({
      koodiarvo: 'valtionhallinnonkielitutkinnonosakoe',
      koodistoUri: 'suorituksentyyppi'
    }),
    ...o
  })

ValtionhallinnonKielitutkinnonSuullisenKielitaidonOsakokeenSuoritus.className =
  'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonSuullisenKielitaidonOsakokeenSuoritus' as const

export const isValtionhallinnonKielitutkinnonSuullisenKielitaidonOsakokeenSuoritus =
  (
    a: any
  ): a is ValtionhallinnonKielitutkinnonSuullisenKielitaidonOsakokeenSuoritus =>
    a?.$class ===
    'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonSuullisenKielitaidonOsakokeenSuoritus'
