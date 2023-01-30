import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { DIATutkinto } from './DIATutkinto'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { DIAOppiaineenTutkintovaiheenSuoritus } from './DIAOppiaineenTutkintovaiheenSuoritus'
import { HenkilövahvistusPaikkakunnalla } from './HenkilovahvistusPaikkakunnalla'

/**
 * Laajennetun oppivelvollisuuden suoritus
 *
 * @see `fi.oph.koski.schema.DIATutkinnonSuoritus`
 */
export type DIATutkinnonSuoritus = {
  $class: 'fi.oph.koski.schema.DIATutkinnonSuoritus'
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'diatutkintovaihe'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  tutkintoaineidenKokonaispistemäärä?: number
  kokonaispistemäärästäJohdettuKeskiarvo?: number
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  kokonaispistemäärä?: number
  koulutusmoduuli: DIATutkinto
  toimipiste: OrganisaatioWithOid
  lukukausisuoritustenKokonaispistemäärä?: number
  osasuoritukset?: Array<DIAOppiaineenTutkintovaiheenSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export const DIATutkinnonSuoritus = (o: {
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'diatutkintovaihe'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  tutkintoaineidenKokonaispistemäärä?: number
  kokonaispistemäärästäJohdettuKeskiarvo?: number
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  kokonaispistemäärä?: number
  koulutusmoduuli?: DIATutkinto
  toimipiste: OrganisaatioWithOid
  lukukausisuoritustenKokonaispistemäärä?: number
  osasuoritukset?: Array<DIAOppiaineenTutkintovaiheenSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}): DIATutkinnonSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'diatutkintovaihe',
    koodistoUri: 'suorituksentyyppi'
  }),
  koulutusmoduuli: DIATutkinto({
    tunniste: Koodistokoodiviite({
      koodiarvo: '301103',
      koodistoUri: 'koulutus'
    })
  }),
  $class: 'fi.oph.koski.schema.DIATutkinnonSuoritus',
  ...o
})

DIATutkinnonSuoritus.className =
  'fi.oph.koski.schema.DIATutkinnonSuoritus' as const

export const isDIATutkinnonSuoritus = (a: any): a is DIATutkinnonSuoritus =>
  a?.$class === 'fi.oph.koski.schema.DIATutkinnonSuoritus'
