import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { IBTutkintokerta } from './IBTutkintokerta'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { IBDiplomanOsasuoritus } from './IBDiplomanOsasuoritus'
import { Päivämäärävahvistus } from './Paivamaaravahvistus'
import { IBTutkinto } from './IBTutkinto'

/**
 * IBDiplomanSuoritus
 *
 * @see `fi.oph.koski.schema.IBDiplomanSuoritus`
 */
export type IBDiplomanSuoritus = {
  $class: 'fi.oph.koski.schema.IBDiplomanSuoritus'
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ibdiploma'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  tutkintokerta: IBTutkintokerta
  kokonaispistemäärä?: number
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<IBDiplomanOsasuoritus>
  tulos: Koodistokoodiviite<'ibdiplomantulos', string>
  vahvistus?: Päivämäärävahvistus
  lisäpisteet?: number
  koulutusmoduuli: IBTutkinto
}

export const IBDiplomanSuoritus = (o: {
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'ibdiploma'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  tutkintokerta: IBTutkintokerta
  kokonaispistemäärä?: number
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<IBDiplomanOsasuoritus>
  tulos: Koodistokoodiviite<'ibdiplomantulos', string>
  vahvistus?: Päivämäärävahvistus
  lisäpisteet?: number
  koulutusmoduuli?: IBTutkinto
}): IBDiplomanSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ibdiploma',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.IBDiplomanSuoritus',
  koulutusmoduuli: IBTutkinto({
    tunniste: Koodistokoodiviite({
      koodiarvo: '301102',
      koodistoUri: 'koulutus'
    })
  }),
  ...o
})

IBDiplomanSuoritus.className = 'fi.oph.koski.schema.IBDiplomanSuoritus' as const

export const isIBDiplomanSuoritus = (a: any): a is IBDiplomanSuoritus =>
  a?.$class === 'fi.oph.koski.schema.IBDiplomanSuoritus'
