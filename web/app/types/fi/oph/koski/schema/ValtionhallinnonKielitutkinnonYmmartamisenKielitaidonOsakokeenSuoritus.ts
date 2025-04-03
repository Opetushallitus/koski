import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { ValtionhallinnonYmmärtämisenKielitaidonOsakoe } from './ValtionhallinnonYmmartamisenKielitaidonOsakoe'
import { ValtionhallinnonKielitutkinnonArviointi } from './ValtionhallinnonKielitutkinnonArviointi'

/**
 * ValtionhallinnonKielitutkinnonYmmärtämisenKielitaidonOsakokeenSuoritus
 *
 * @see `fi.oph.koski.schema.ValtionhallinnonKielitutkinnonYmmärtämisenKielitaidonOsakokeenSuoritus`
 */
export type ValtionhallinnonKielitutkinnonYmmärtämisenKielitaidonOsakokeenSuoritus =
  {
    $class: 'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonYmmärtämisenKielitaidonOsakokeenSuoritus'
    tyyppi: Koodistokoodiviite<
      'suorituksentyyppi',
      'valtionhallinnonkielitutkinnonosakoe'
    >
    koulutusmoduuli: ValtionhallinnonYmmärtämisenKielitaidonOsakoe
    arviointi?: Array<ValtionhallinnonKielitutkinnonArviointi>
    tila?: Koodistokoodiviite<'suorituksentila', string>
  }

export const ValtionhallinnonKielitutkinnonYmmärtämisenKielitaidonOsakokeenSuoritus =
  (o: {
    tyyppi?: Koodistokoodiviite<
      'suorituksentyyppi',
      'valtionhallinnonkielitutkinnonosakoe'
    >
    koulutusmoduuli: ValtionhallinnonYmmärtämisenKielitaidonOsakoe
    arviointi?: Array<ValtionhallinnonKielitutkinnonArviointi>
    tila?: Koodistokoodiviite<'suorituksentila', string>
  }): ValtionhallinnonKielitutkinnonYmmärtämisenKielitaidonOsakokeenSuoritus => ({
    $class:
      'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonYmmärtämisenKielitaidonOsakokeenSuoritus',
    tyyppi: Koodistokoodiviite({
      koodiarvo: 'valtionhallinnonkielitutkinnonosakoe',
      koodistoUri: 'suorituksentyyppi'
    }),
    ...o
  })

ValtionhallinnonKielitutkinnonYmmärtämisenKielitaidonOsakokeenSuoritus.className =
  'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonYmmärtämisenKielitaidonOsakokeenSuoritus' as const

export const isValtionhallinnonKielitutkinnonYmmärtämisenKielitaidonOsakokeenSuoritus =
  (
    a: any
  ): a is ValtionhallinnonKielitutkinnonYmmärtämisenKielitaidonOsakokeenSuoritus =>
    a?.$class ===
    'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonYmmärtämisenKielitaidonOsakokeenSuoritus'
