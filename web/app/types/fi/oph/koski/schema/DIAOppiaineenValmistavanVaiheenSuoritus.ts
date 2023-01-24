import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { DIAOppiaine } from './DIAOppiaine'
import { DIAOppiaineenValmistavanVaiheenLukukaudenSuoritus } from './DIAOppiaineenValmistavanVaiheenLukukaudenSuoritus'

/**
 * DIAOppiaineenValmistavanVaiheenSuoritus
 *
 * @see `fi.oph.koski.schema.DIAOppiaineenValmistavanVaiheenSuoritus`
 */
export type DIAOppiaineenValmistavanVaiheenSuoritus = {
  $class: 'fi.oph.koski.schema.DIAOppiaineenValmistavanVaiheenSuoritus'
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'diaoppiaine'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: DIAOppiaine
  osasuoritukset?: Array<DIAOppiaineenValmistavanVaiheenLukukaudenSuoritus>
}

export const DIAOppiaineenValmistavanVaiheenSuoritus = (o: {
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'diaoppiaine'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: DIAOppiaine
  osasuoritukset?: Array<DIAOppiaineenValmistavanVaiheenLukukaudenSuoritus>
}): DIAOppiaineenValmistavanVaiheenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'diaoppiaine',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.DIAOppiaineenValmistavanVaiheenSuoritus',
  ...o
})

DIAOppiaineenValmistavanVaiheenSuoritus.className =
  'fi.oph.koski.schema.DIAOppiaineenValmistavanVaiheenSuoritus' as const

export const isDIAOppiaineenValmistavanVaiheenSuoritus = (
  a: any
): a is DIAOppiaineenValmistavanVaiheenSuoritus =>
  a?.$class === 'fi.oph.koski.schema.DIAOppiaineenValmistavanVaiheenSuoritus'
