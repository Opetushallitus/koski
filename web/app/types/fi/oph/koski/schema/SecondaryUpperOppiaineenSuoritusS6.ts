import { SecondaryNumericalMarkArviointi } from './SecondaryNumericalMarkArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { SecondaryOppiaine } from './SecondaryOppiaine'

/**
 * SecondaryUpperOppiaineenSuoritusS6
 *
 * @see `fi.oph.koski.schema.SecondaryUpperOppiaineenSuoritusS6`
 */
export type SecondaryUpperOppiaineenSuoritusS6 = {
  $class: 'fi.oph.koski.schema.SecondaryUpperOppiaineenSuoritusS6'
  arviointi?: Array<SecondaryNumericalMarkArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'europeanschoolofhelsinkiosasuorituss6'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: SecondaryOppiaine
  yksilöllistettyOppimäärä: boolean
}

export const SecondaryUpperOppiaineenSuoritusS6 = (o: {
  arviointi?: Array<SecondaryNumericalMarkArviointi>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'europeanschoolofhelsinkiosasuorituss6'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: SecondaryOppiaine
  yksilöllistettyOppimäärä?: boolean
}): SecondaryUpperOppiaineenSuoritusS6 => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'europeanschoolofhelsinkiosasuorituss6',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.SecondaryUpperOppiaineenSuoritusS6',
  yksilöllistettyOppimäärä: false,
  ...o
})

export const isSecondaryUpperOppiaineenSuoritusS6 = (
  a: any
): a is SecondaryUpperOppiaineenSuoritusS6 =>
  a?.$class === 'SecondaryUpperOppiaineenSuoritusS6'
