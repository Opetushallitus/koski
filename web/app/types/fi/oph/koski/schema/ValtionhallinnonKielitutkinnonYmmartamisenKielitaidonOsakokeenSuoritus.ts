import { ValtionhallinnonKielitutkinnonArviointi } from './ValtionhallinnonKielitutkinnonArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { ValtionhallinnonYmmärtämisenKielitaidonOsakoe } from './ValtionhallinnonYmmartamisenKielitaidonOsakoe'

/**
 * ValtionhallinnonKielitutkinnonYmmärtämisenKielitaidonOsakokeenSuoritus
 *
 * @see `fi.oph.koski.schema.ValtionhallinnonKielitutkinnonYmmärtämisenKielitaidonOsakokeenSuoritus`
 */
export type ValtionhallinnonKielitutkinnonYmmärtämisenKielitaidonOsakokeenSuoritus =
  {
    $class: 'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonYmmärtämisenKielitaidonOsakokeenSuoritus'
    arviointi?: Array<ValtionhallinnonKielitutkinnonArviointi>
    tyyppi: Koodistokoodiviite<
      'suorituksentyyppi',
      'valtionhallinnonkielitutkinnonosakoe'
    >
    koulutusmoduuli: ValtionhallinnonYmmärtämisenKielitaidonOsakoe
    tila?: Koodistokoodiviite<'suorituksentila', string>
    alkamispäivä?: string
  }

export const ValtionhallinnonKielitutkinnonYmmärtämisenKielitaidonOsakokeenSuoritus =
  (o: {
    arviointi?: Array<ValtionhallinnonKielitutkinnonArviointi>
    tyyppi?: Koodistokoodiviite<
      'suorituksentyyppi',
      'valtionhallinnonkielitutkinnonosakoe'
    >
    koulutusmoduuli: ValtionhallinnonYmmärtämisenKielitaidonOsakoe
    tila?: Koodistokoodiviite<'suorituksentila', string>
    alkamispäivä?: string
  }): ValtionhallinnonKielitutkinnonYmmärtämisenKielitaidonOsakokeenSuoritus => ({
    tyyppi: Koodistokoodiviite({
      koodiarvo: 'valtionhallinnonkielitutkinnonosakoe',
      koodistoUri: 'suorituksentyyppi'
    }),
    $class:
      'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonYmmärtämisenKielitaidonOsakokeenSuoritus',
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
