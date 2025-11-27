import { ValtionhallinnonKielitutkinnonArviointi } from './ValtionhallinnonKielitutkinnonArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { ValtionhallinnonSuullisenKielitaidonOsakoe } from './ValtionhallinnonSuullisenKielitaidonOsakoe'

/**
 * ValtionhallinnonKielitutkinnonSuullisenKielitaidonOsakokeenSuoritus
 *
 * @see `fi.oph.koski.schema.ValtionhallinnonKielitutkinnonSuullisenKielitaidonOsakokeenSuoritus`
 */
export type ValtionhallinnonKielitutkinnonSuullisenKielitaidonOsakokeenSuoritus =
  {
    $class: 'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonSuullisenKielitaidonOsakokeenSuoritus'
    arviointi?: Array<ValtionhallinnonKielitutkinnonArviointi>
    tyyppi: Koodistokoodiviite<
      'suorituksentyyppi',
      'valtionhallinnonkielitutkinnonosakoe'
    >
    koulutusmoduuli: ValtionhallinnonSuullisenKielitaidonOsakoe
    tila?: Koodistokoodiviite<'suorituksentila', string>
    alkamispäivä?: string
  }

export const ValtionhallinnonKielitutkinnonSuullisenKielitaidonOsakokeenSuoritus =
  (o: {
    arviointi?: Array<ValtionhallinnonKielitutkinnonArviointi>
    tyyppi?: Koodistokoodiviite<
      'suorituksentyyppi',
      'valtionhallinnonkielitutkinnonosakoe'
    >
    koulutusmoduuli: ValtionhallinnonSuullisenKielitaidonOsakoe
    tila?: Koodistokoodiviite<'suorituksentila', string>
    alkamispäivä?: string
  }): ValtionhallinnonKielitutkinnonSuullisenKielitaidonOsakokeenSuoritus => ({
    tyyppi: Koodistokoodiviite({
      koodiarvo: 'valtionhallinnonkielitutkinnonosakoe',
      koodistoUri: 'suorituksentyyppi'
    }),
    $class:
      'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonSuullisenKielitaidonOsakokeenSuoritus',
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
