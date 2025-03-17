import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { ValtionhallinnonKirjallisenKielitaidonOsakoe } from './ValtionhallinnonKirjallisenKielitaidonOsakoe'
import { ValtionhallinnonKielitutkinnonArviointi } from './ValtionhallinnonKielitutkinnonArviointi'

/**
 * ValtionhallinnonKielitutkinnonKirjallisenKielitaidonOsakokeenSuoritus
 *
 * @see `fi.oph.koski.schema.ValtionhallinnonKielitutkinnonKirjallisenKielitaidonOsakokeenSuoritus`
 */
export type ValtionhallinnonKielitutkinnonKirjallisenKielitaidonOsakokeenSuoritus =
  {
    $class: 'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonKirjallisenKielitaidonOsakokeenSuoritus'
    tyyppi: Koodistokoodiviite<
      'suorituksentyyppi',
      'valtionhallinnonkielitutkinnonosakoe'
    >
    koulutusmoduuli: ValtionhallinnonKirjallisenKielitaidonOsakoe
    arviointi?: Array<ValtionhallinnonKielitutkinnonArviointi>
    tila?: Koodistokoodiviite<'suorituksentila', string>
  }

export const ValtionhallinnonKielitutkinnonKirjallisenKielitaidonOsakokeenSuoritus =
  (o: {
    tyyppi?: Koodistokoodiviite<
      'suorituksentyyppi',
      'valtionhallinnonkielitutkinnonosakoe'
    >
    koulutusmoduuli: ValtionhallinnonKirjallisenKielitaidonOsakoe
    arviointi?: Array<ValtionhallinnonKielitutkinnonArviointi>
    tila?: Koodistokoodiviite<'suorituksentila', string>
  }): ValtionhallinnonKielitutkinnonKirjallisenKielitaidonOsakokeenSuoritus => ({
    $class:
      'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonKirjallisenKielitaidonOsakokeenSuoritus',
    tyyppi: Koodistokoodiviite({
      koodiarvo: 'valtionhallinnonkielitutkinnonosakoe',
      koodistoUri: 'suorituksentyyppi'
    }),
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
