import { EBOppiaineKomponentti } from './EBOppiaineKomponentti'
import { EBArviointi } from './EBArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * EBOppiaineenAlaosasuoritus
 *
 * @see `fi.oph.koski.schema.EBOppiaineenAlaosasuoritus`
 */
export type EBOppiaineenAlaosasuoritus = {
  $class: 'fi.oph.koski.schema.EBOppiaineenAlaosasuoritus'
  koulutusmoduuli: EBOppiaineKomponentti
  arviointi?: Array<EBArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ebtutkinnonalaosasuoritus'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
}

export const EBOppiaineenAlaosasuoritus = (o: {
  koulutusmoduuli: EBOppiaineKomponentti
  arviointi?: Array<EBArviointi>
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'ebtutkinnonalaosasuoritus'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
}): EBOppiaineenAlaosasuoritus => ({
  $class: 'fi.oph.koski.schema.EBOppiaineenAlaosasuoritus',
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ebtutkinnonalaosasuoritus',
    koodistoUri: 'suorituksentyyppi'
  }),
  ...o
})

export const isEBOppiaineenAlaosasuoritus = (
  a: any
): a is EBOppiaineenAlaosasuoritus =>
  a?.$class === 'fi.oph.koski.schema.EBOppiaineenAlaosasuoritus'
