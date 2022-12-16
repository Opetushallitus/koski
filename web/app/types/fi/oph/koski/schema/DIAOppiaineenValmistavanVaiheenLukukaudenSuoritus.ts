import { DIAOppiaineenValmistavanVaiheenLukukausi } from './DIAOppiaineenValmistavanVaiheenLukukausi'
import { DIAOppiaineenValmistavanVaiheenLukukaudenArviointi } from './DIAOppiaineenValmistavanVaiheenLukukaudenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * DIAOppiaineenValmistavanVaiheenLukukaudenSuoritus
 *
 * @see `fi.oph.koski.schema.DIAOppiaineenValmistavanVaiheenLukukaudenSuoritus`
 */
export type DIAOppiaineenValmistavanVaiheenLukukaudenSuoritus = {
  $class: 'fi.oph.koski.schema.DIAOppiaineenValmistavanVaiheenLukukaudenSuoritus'
  koulutusmoduuli: DIAOppiaineenValmistavanVaiheenLukukausi
  arviointi?: Array<DIAOppiaineenValmistavanVaiheenLukukaudenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'diaoppiaineenvalmistavanvaiheenlukukaudensuoritus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
}

export const DIAOppiaineenValmistavanVaiheenLukukaudenSuoritus = (o: {
  koulutusmoduuli: DIAOppiaineenValmistavanVaiheenLukukausi
  arviointi?: Array<DIAOppiaineenValmistavanVaiheenLukukaudenArviointi>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'diaoppiaineenvalmistavanvaiheenlukukaudensuoritus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
}): DIAOppiaineenValmistavanVaiheenLukukaudenSuoritus => ({
  $class:
    'fi.oph.koski.schema.DIAOppiaineenValmistavanVaiheenLukukaudenSuoritus',
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'diaoppiaineenvalmistavanvaiheenlukukaudensuoritus',
    koodistoUri: 'suorituksentyyppi'
  }),
  ...o
})

export const isDIAOppiaineenValmistavanVaiheenLukukaudenSuoritus = (
  a: any
): a is DIAOppiaineenValmistavanVaiheenLukukaudenSuoritus =>
  a?.$class === 'DIAOppiaineenValmistavanVaiheenLukukaudenSuoritus'
