import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { IBTutkintokerta } from './IBTutkintokerta'
import { IBTutkinto } from './IBTutkinto'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { IBDiplomanOsasuoritus } from './IBDiplomanOsasuoritus'
import { Päivämäärävahvistus } from './Paivamaaravahvistus'

/**
 * IBDiplomanSuoritus
 *
 * @see `fi.oph.koski.schema.IBDiplomanSuoritus`
 */
export type IBDiplomanSuoritus = {
  $class: 'fi.oph.koski.schema.IBDiplomanSuoritus'
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ibdiploma'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  result: Koodistokoodiviite<'ibdiplomaresult', string>
  tutkintokerta: IBTutkintokerta
  totalPoints?: number
  combinedGrade?: number
  koulutusmoduuli: IBTutkinto
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<IBDiplomanOsasuoritus>
  vahvistus?: Päivämäärävahvistus
}

export const IBDiplomanSuoritus = (o: {
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'ibdiploma'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  result: Koodistokoodiviite<'ibdiplomaresult', string>
  tutkintokerta: IBTutkintokerta
  totalPoints?: number
  combinedGrade?: number
  koulutusmoduuli?: IBTutkinto
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<IBDiplomanOsasuoritus>
  vahvistus?: Päivämäärävahvistus
}): IBDiplomanSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ibdiploma',
    koodistoUri: 'suorituksentyyppi'
  }),
  koulutusmoduuli: IBTutkinto({
    tunniste: Koodistokoodiviite({
      koodiarvo: '301102',
      koodistoUri: 'koulutus'
    })
  }),
  $class: 'fi.oph.koski.schema.IBDiplomanSuoritus',
  ...o
})

IBDiplomanSuoritus.className = 'fi.oph.koski.schema.IBDiplomanSuoritus' as const

export const isIBDiplomanSuoritus = (a: any): a is IBDiplomanSuoritus =>
  a?.$class === 'fi.oph.koski.schema.IBDiplomanSuoritus'
