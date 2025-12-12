import { ValtionhallinnonKielitutkinnonArviointi } from './ValtionhallinnonKielitutkinnonArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { ValtionhallinnonKielitutkinnonYmmärtämisenKielitaito } from './ValtionhallinnonKielitutkinnonYmmartamisenKielitaito'
import { ValtionhallinnonKielitutkinnonYmmärtämisenKielitaidonOsakokeenSuoritus } from './ValtionhallinnonKielitutkinnonYmmartamisenKielitaidonOsakokeenSuoritus'

/**
 * ValtionhallinnonKielitutkinnonYmmärtämisenKielitaidonSuoritus
 *
 * @see `fi.oph.koski.schema.ValtionhallinnonKielitutkinnonYmmärtämisenKielitaidonSuoritus`
 */
export type ValtionhallinnonKielitutkinnonYmmärtämisenKielitaidonSuoritus = {
  $class: 'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonYmmärtämisenKielitaidonSuoritus'
  arviointi?: Array<ValtionhallinnonKielitutkinnonArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'valtionhallinnonkielitaito'>
  tutkintopäiväTodistuksella?: string
  koulutusmoduuli: ValtionhallinnonKielitutkinnonYmmärtämisenKielitaito
  osasuoritukset?: Array<ValtionhallinnonKielitutkinnonYmmärtämisenKielitaidonOsakokeenSuoritus>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
}

export const ValtionhallinnonKielitutkinnonYmmärtämisenKielitaidonSuoritus = (
  o: {
    arviointi?: Array<ValtionhallinnonKielitutkinnonArviointi>
    tyyppi?: Koodistokoodiviite<
      'suorituksentyyppi',
      'valtionhallinnonkielitaito'
    >
    tutkintopäiväTodistuksella?: string
    koulutusmoduuli?: ValtionhallinnonKielitutkinnonYmmärtämisenKielitaito
    osasuoritukset?: Array<ValtionhallinnonKielitutkinnonYmmärtämisenKielitaidonOsakokeenSuoritus>
    tila?: Koodistokoodiviite<'suorituksentila', string>
    alkamispäivä?: string
  } = {}
): ValtionhallinnonKielitutkinnonYmmärtämisenKielitaidonSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'valtionhallinnonkielitaito',
    koodistoUri: 'suorituksentyyppi'
  }),
  koulutusmoduuli: ValtionhallinnonKielitutkinnonYmmärtämisenKielitaito({
    tunniste: Koodistokoodiviite({
      koodiarvo: 'ymmartaminen',
      koodistoUri: 'vktkielitaito'
    })
  }),
  $class:
    'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonYmmärtämisenKielitaidonSuoritus',
  ...o
})

ValtionhallinnonKielitutkinnonYmmärtämisenKielitaidonSuoritus.className =
  'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonYmmärtämisenKielitaidonSuoritus' as const

export const isValtionhallinnonKielitutkinnonYmmärtämisenKielitaidonSuoritus = (
  a: any
): a is ValtionhallinnonKielitutkinnonYmmärtämisenKielitaidonSuoritus =>
  a?.$class ===
  'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonYmmärtämisenKielitaidonSuoritus'
