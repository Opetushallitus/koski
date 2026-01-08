import { ValtionhallinnonKielitutkinnonArviointi } from './ValtionhallinnonKielitutkinnonArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { ValtionhallinnonKielitutkinnonKirjallinenKielitaito } from './ValtionhallinnonKielitutkinnonKirjallinenKielitaito'
import { ValtionhallinnonKielitutkinnonKirjallisenKielitaidonOsakokeenSuoritus } from './ValtionhallinnonKielitutkinnonKirjallisenKielitaidonOsakokeenSuoritus'

/**
 * ValtionhallinnonKielitutkinnonKirjallisenKielitaidonSuoritus
 *
 * @see `fi.oph.koski.schema.ValtionhallinnonKielitutkinnonKirjallisenKielitaidonSuoritus`
 */
export type ValtionhallinnonKielitutkinnonKirjallisenKielitaidonSuoritus = {
  $class: 'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonKirjallisenKielitaidonSuoritus'
  arviointi?: Array<ValtionhallinnonKielitutkinnonArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'valtionhallinnonkielitaito'>
  tutkintopäiväTodistuksella?: string
  koulutusmoduuli: ValtionhallinnonKielitutkinnonKirjallinenKielitaito
  osasuoritukset?: Array<ValtionhallinnonKielitutkinnonKirjallisenKielitaidonOsakokeenSuoritus>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
}

export const ValtionhallinnonKielitutkinnonKirjallisenKielitaidonSuoritus = (
  o: {
    arviointi?: Array<ValtionhallinnonKielitutkinnonArviointi>
    tyyppi?: Koodistokoodiviite<
      'suorituksentyyppi',
      'valtionhallinnonkielitaito'
    >
    tutkintopäiväTodistuksella?: string
    koulutusmoduuli?: ValtionhallinnonKielitutkinnonKirjallinenKielitaito
    osasuoritukset?: Array<ValtionhallinnonKielitutkinnonKirjallisenKielitaidonOsakokeenSuoritus>
    tila?: Koodistokoodiviite<'suorituksentila', string>
    alkamispäivä?: string
  } = {}
): ValtionhallinnonKielitutkinnonKirjallisenKielitaidonSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'valtionhallinnonkielitaito',
    koodistoUri: 'suorituksentyyppi'
  }),
  koulutusmoduuli: ValtionhallinnonKielitutkinnonKirjallinenKielitaito({
    tunniste: Koodistokoodiviite({
      koodiarvo: 'kirjallinen',
      koodistoUri: 'vktkielitaito'
    })
  }),
  $class:
    'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonKirjallisenKielitaidonSuoritus',
  ...o
})

ValtionhallinnonKielitutkinnonKirjallisenKielitaidonSuoritus.className =
  'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonKirjallisenKielitaidonSuoritus' as const

export const isValtionhallinnonKielitutkinnonKirjallisenKielitaidonSuoritus = (
  a: any
): a is ValtionhallinnonKielitutkinnonKirjallisenKielitaidonSuoritus =>
  a?.$class ===
  'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonKirjallisenKielitaidonSuoritus'
