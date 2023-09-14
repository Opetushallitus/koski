import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { SuullisenKielitaidonKoe2019 } from './SuullisenKielitaidonKoe2019'
import { LukionOmanÄidinkielenOpinnot } from './LukionOmanAidinkielenOpinnot'
import { PuhviKoe2019 } from './PuhviKoe2019'
import { LukionOppimäärä } from './LukionOppimaara'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { LukionOppimääränOsasuoritus2019 } from './LukionOppimaaranOsasuoritus2019'
import { HenkilövahvistusPaikkakunnalla } from './HenkilovahvistusPaikkakunnalla'

/**
 * Laajennetun oppivelvollisuuden suoritus
 * Lukion oppimäärän opetussuunnitelman 2019 mukaiset suoritustiedot
 *
 * @see `fi.oph.koski.schema.LukionOppimääränSuoritus2019`
 */
export type LukionOppimääränSuoritus2019 = {
  $class: 'fi.oph.koski.schema.LukionOppimääränSuoritus2019'
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'lukionoppimaara'>
  suullisenKielitaidonKokeet?: Array<SuullisenKielitaidonKoe2019>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  omanÄidinkielenOpinnot?: LukionOmanÄidinkielenOpinnot
  suoritettuErityisenäTutkintona: boolean
  suorituskieli: Koodistokoodiviite<'kieli', string>
  puhviKoe?: PuhviKoe2019
  oppimäärä: Koodistokoodiviite<'lukionoppimaara', string>
  koulusivistyskieli?: Array<Koodistokoodiviite<'kieli', 'FI' | 'SV'>>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  ryhmä?: string
  koulutusmoduuli: LukionOppimäärä
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<LukionOppimääränOsasuoritus2019>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export const LukionOppimääränSuoritus2019 = (o: {
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'lukionoppimaara'>
  suullisenKielitaidonKokeet?: Array<SuullisenKielitaidonKoe2019>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  omanÄidinkielenOpinnot?: LukionOmanÄidinkielenOpinnot
  suoritettuErityisenäTutkintona?: boolean
  suorituskieli: Koodistokoodiviite<'kieli', string>
  puhviKoe?: PuhviKoe2019
  oppimäärä: Koodistokoodiviite<'lukionoppimaara', string>
  koulusivistyskieli?: Array<Koodistokoodiviite<'kieli', 'FI' | 'SV'>>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  ryhmä?: string
  koulutusmoduuli?: LukionOppimäärä
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<LukionOppimääränOsasuoritus2019>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}): LukionOppimääränSuoritus2019 => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'lukionoppimaara',
    koodistoUri: 'suorituksentyyppi'
  }),
  suoritettuErityisenäTutkintona: false,
  koulutusmoduuli: LukionOppimäärä({
    tunniste: Koodistokoodiviite({
      koodiarvo: '309902',
      koodistoUri: 'koulutus'
    })
  }),
  $class: 'fi.oph.koski.schema.LukionOppimääränSuoritus2019',
  ...o
})

LukionOppimääränSuoritus2019.className =
  'fi.oph.koski.schema.LukionOppimääränSuoritus2019' as const

export const isLukionOppimääränSuoritus2019 = (
  a: any
): a is LukionOppimääränSuoritus2019 =>
  a?.$class === 'fi.oph.koski.schema.LukionOppimääränSuoritus2019'
