import { S7OppiaineKomponentti } from './S7OppiaineKomponentti'
import { SecondaryS7PreliminaryMarkArviointi } from './SecondaryS7PreliminaryMarkArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * S7OppiaineenAlaosasuoritus
 *
 * @see `fi.oph.koski.schema.S7OppiaineenAlaosasuoritus`
 */
export type S7OppiaineenAlaosasuoritus = {
  $class: 'fi.oph.koski.schema.S7OppiaineenAlaosasuoritus'
  koulutusmoduuli: S7OppiaineKomponentti
  arviointi?: Array<SecondaryS7PreliminaryMarkArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'europeanschoolofhelsinkialaosasuorituss7'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
}

export const S7OppiaineenAlaosasuoritus = (o: {
  koulutusmoduuli: S7OppiaineKomponentti
  arviointi?: Array<SecondaryS7PreliminaryMarkArviointi>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'europeanschoolofhelsinkialaosasuorituss7'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
}): S7OppiaineenAlaosasuoritus => ({
  $class: 'fi.oph.koski.schema.S7OppiaineenAlaosasuoritus',
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'europeanschoolofhelsinkialaosasuorituss7',
    koodistoUri: 'suorituksentyyppi'
  }),
  ...o
})

S7OppiaineenAlaosasuoritus.className =
  'fi.oph.koski.schema.S7OppiaineenAlaosasuoritus' as const

export const isS7OppiaineenAlaosasuoritus = (
  a: any
): a is S7OppiaineenAlaosasuoritus =>
  a?.$class === 'fi.oph.koski.schema.S7OppiaineenAlaosasuoritus'
