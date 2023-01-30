import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { OmanÄidinkielenOpinnotLaajuusKursseina } from './OmanAidinkielenOpinnotLaajuusKursseina'
import { LukionOppimäärä } from './LukionOppimaara'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { LukionOppimääränOsasuoritus2015 } from './LukionOppimaaranOsasuoritus2015'
import { HenkilövahvistusPaikkakunnalla } from './HenkilovahvistusPaikkakunnalla'

/**
 * Laajennetun oppivelvollisuuden suoritus
 * Lukion oppimäärän suoritustiedot
 *
 * @see `fi.oph.koski.schema.LukionOppimääränSuoritus2015`
 */
export type LukionOppimääränSuoritus2015 = {
  $class: 'fi.oph.koski.schema.LukionOppimääränSuoritus2015'
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'lukionoppimaara'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  omanÄidinkielenOpinnot?: OmanÄidinkielenOpinnotLaajuusKursseina
  suorituskieli: Koodistokoodiviite<'kieli', string>
  oppimäärä: Koodistokoodiviite<'lukionoppimaara', string>
  koulusivistyskieli?: Array<Koodistokoodiviite<'kieli', 'FI' | 'SV'>>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  ryhmä?: string
  koulutusmoduuli: LukionOppimäärä
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<LukionOppimääränOsasuoritus2015>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export const LukionOppimääränSuoritus2015 = (o: {
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'lukionoppimaara'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  omanÄidinkielenOpinnot?: OmanÄidinkielenOpinnotLaajuusKursseina
  suorituskieli: Koodistokoodiviite<'kieli', string>
  oppimäärä: Koodistokoodiviite<'lukionoppimaara', string>
  koulusivistyskieli?: Array<Koodistokoodiviite<'kieli', 'FI' | 'SV'>>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  ryhmä?: string
  koulutusmoduuli?: LukionOppimäärä
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<LukionOppimääränOsasuoritus2015>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}): LukionOppimääränSuoritus2015 => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'lukionoppimaara',
    koodistoUri: 'suorituksentyyppi'
  }),
  koulutusmoduuli: LukionOppimäärä({
    tunniste: Koodistokoodiviite({
      koodiarvo: '309902',
      koodistoUri: 'koulutus'
    })
  }),
  $class: 'fi.oph.koski.schema.LukionOppimääränSuoritus2015',
  ...o
})

LukionOppimääränSuoritus2015.className =
  'fi.oph.koski.schema.LukionOppimääränSuoritus2015' as const

export const isLukionOppimääränSuoritus2015 = (
  a: any
): a is LukionOppimääränSuoritus2015 =>
  a?.$class === 'fi.oph.koski.schema.LukionOppimääränSuoritus2015'
