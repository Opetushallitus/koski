import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { SecondaryOppiaine } from './SecondaryOppiaine'
import { S7OppiaineenAlaosasuoritus } from './S7OppiaineenAlaosasuoritus'

/**
 * SecondaryUpperOppiaineenSuoritusS7
 *
 * @see `fi.oph.koski.schema.SecondaryUpperOppiaineenSuoritusS7`
 */
export type SecondaryUpperOppiaineenSuoritusS7 = {
  $class: 'fi.oph.koski.schema.SecondaryUpperOppiaineenSuoritusS7'
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'europeanschoolofhelsinkiosasuorituss7'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: SecondaryOppiaine
  osasuoritukset?: Array<S7OppiaineenAlaosasuoritus>
  yksilöllistettyOppimäärä: boolean
}

export const SecondaryUpperOppiaineenSuoritusS7 = (o: {
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'europeanschoolofhelsinkiosasuorituss7'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: SecondaryOppiaine
  osasuoritukset?: Array<S7OppiaineenAlaosasuoritus>
  yksilöllistettyOppimäärä?: boolean
}): SecondaryUpperOppiaineenSuoritusS7 => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'europeanschoolofhelsinkiosasuorituss7',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.SecondaryUpperOppiaineenSuoritusS7',
  yksilöllistettyOppimäärä: false,
  ...o
})

export const isSecondaryUpperOppiaineenSuoritusS7 = (
  a: any
): a is SecondaryUpperOppiaineenSuoritusS7 =>
  a?.$class === 'fi.oph.koski.schema.SecondaryUpperOppiaineenSuoritusS7'
