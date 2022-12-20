import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { DIAOppiaine } from './DIAOppiaine'
import { DIAVastaavuustodistuksenTiedot } from './DIAVastaavuustodistuksenTiedot'
import { DIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus } from './DIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus'

/**
 * DIAOppiaineenTutkintovaiheenSuoritus
 *
 * @see `fi.oph.koski.schema.DIAOppiaineenTutkintovaiheenSuoritus`
 */
export type DIAOppiaineenTutkintovaiheenSuoritus = {
  $class: 'fi.oph.koski.schema.DIAOppiaineenTutkintovaiheenSuoritus'
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'diaoppiaine'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koetuloksenNelinkertainenPistemäärä?: number
  koulutusmoduuli: DIAOppiaine
  vastaavuustodistuksenTiedot?: DIAVastaavuustodistuksenTiedot
  osasuoritukset?: Array<DIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus>
}

export const DIAOppiaineenTutkintovaiheenSuoritus = (o: {
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'diaoppiaine'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koetuloksenNelinkertainenPistemäärä?: number
  koulutusmoduuli: DIAOppiaine
  vastaavuustodistuksenTiedot?: DIAVastaavuustodistuksenTiedot
  osasuoritukset?: Array<DIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus>
}): DIAOppiaineenTutkintovaiheenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'diaoppiaine',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.DIAOppiaineenTutkintovaiheenSuoritus',
  ...o
})

export const isDIAOppiaineenTutkintovaiheenSuoritus = (
  a: any
): a is DIAOppiaineenTutkintovaiheenSuoritus =>
  a?.$class === 'fi.oph.koski.schema.DIAOppiaineenTutkintovaiheenSuoritus'
