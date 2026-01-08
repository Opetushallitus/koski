import { ValtionhallinnonKielitutkinnonArviointi } from './ValtionhallinnonKielitutkinnonArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { ValtionhallinnonKirjallisenKielitaidonOsakoe } from './ValtionhallinnonKirjallisenKielitaidonOsakoe'

/**
 * ValtionhallinnonKielitutkinnonKirjallisenKielitaidonOsakokeenSuoritus
 *
 * @see `fi.oph.koski.schema.ValtionhallinnonKielitutkinnonKirjallisenKielitaidonOsakokeenSuoritus`
 */
export type ValtionhallinnonKielitutkinnonKirjallisenKielitaidonOsakokeenSuoritus =
  {
    $class: 'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonKirjallisenKielitaidonOsakokeenSuoritus'
    arviointi?: Array<ValtionhallinnonKielitutkinnonArviointi>
    tyyppi: Koodistokoodiviite<
      'suorituksentyyppi',
      'valtionhallinnonkielitutkinnonosakoe'
    >
    koulutusmoduuli: ValtionhallinnonKirjallisenKielitaidonOsakoe
    tila?: Koodistokoodiviite<'suorituksentila', string>
    alkamispäivä?: string
  }

export const ValtionhallinnonKielitutkinnonKirjallisenKielitaidonOsakokeenSuoritus =
  (o: {
    arviointi?: Array<ValtionhallinnonKielitutkinnonArviointi>
    tyyppi?: Koodistokoodiviite<
      'suorituksentyyppi',
      'valtionhallinnonkielitutkinnonosakoe'
    >
    koulutusmoduuli: ValtionhallinnonKirjallisenKielitaidonOsakoe
    tila?: Koodistokoodiviite<'suorituksentila', string>
    alkamispäivä?: string
  }): ValtionhallinnonKielitutkinnonKirjallisenKielitaidonOsakokeenSuoritus => ({
    tyyppi: Koodistokoodiviite({
      koodiarvo: 'valtionhallinnonkielitutkinnonosakoe',
      koodistoUri: 'suorituksentyyppi'
    }),
    $class:
      'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonKirjallisenKielitaidonOsakokeenSuoritus',
    ...o
  })

ValtionhallinnonKielitutkinnonKirjallisenKielitaidonOsakokeenSuoritus.className =
  'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonKirjallisenKielitaidonOsakokeenSuoritus' as const

export const isValtionhallinnonKielitutkinnonKirjallisenKielitaidonOsakokeenSuoritus =
  (
    a: any
  ): a is ValtionhallinnonKielitutkinnonKirjallisenKielitaidonOsakokeenSuoritus =>
    a?.$class ===
    'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonKirjallisenKielitaidonOsakokeenSuoritus'
