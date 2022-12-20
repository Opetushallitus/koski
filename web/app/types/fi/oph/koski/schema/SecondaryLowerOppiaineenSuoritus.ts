import { SecondaryLowerArviointi } from './SecondaryLowerArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { SecondaryOppiaine } from './SecondaryOppiaine'

/**
 * SecondaryLowerOppiaineenSuoritus
 *
 * @see `fi.oph.koski.schema.SecondaryLowerOppiaineenSuoritus`
 */
export type SecondaryLowerOppiaineenSuoritus = {
  $class: 'fi.oph.koski.schema.SecondaryLowerOppiaineenSuoritus'
  arviointi?: Array<SecondaryLowerArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'europeanschoolofhelsinkiosasuoritussecondarylower'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: SecondaryOppiaine
  yksilöllistettyOppimäärä: boolean
}

export const SecondaryLowerOppiaineenSuoritus = (o: {
  arviointi?: Array<SecondaryLowerArviointi>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'europeanschoolofhelsinkiosasuoritussecondarylower'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: SecondaryOppiaine
  yksilöllistettyOppimäärä?: boolean
}): SecondaryLowerOppiaineenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'europeanschoolofhelsinkiosasuoritussecondarylower',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.SecondaryLowerOppiaineenSuoritus',
  yksilöllistettyOppimäärä: false,
  ...o
})

export const isSecondaryLowerOppiaineenSuoritus = (
  a: any
): a is SecondaryLowerOppiaineenSuoritus =>
  a?.$class === 'fi.oph.koski.schema.SecondaryLowerOppiaineenSuoritus'
