import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { SuullisenKielitaidonKoe2019 } from './SuullisenKielitaidonKoe2019'
import { PuhviKoe2019 } from './PuhviKoe2019'
import { LukionOppiaineidenOppimäärät2019 } from './LukionOppiaineidenOppimaarat2019'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { LukionOppiaineenSuoritus2019 } from './LukionOppiaineenSuoritus2019'
import { HenkilövahvistusPaikkakunnalla } from './HenkilovahvistusPaikkakunnalla'

/**
 * Lukion oppiaineiden oppimäärien suoritustiedot 2019
 *
 * @see `fi.oph.koski.schema.LukionOppiaineidenOppimäärienSuoritus2019`
 */
export type LukionOppiaineidenOppimäärienSuoritus2019 = {
  $class: 'fi.oph.koski.schema.LukionOppiaineidenOppimäärienSuoritus2019'
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'lukionaineopinnot'>
  suullisenKielitaidonKokeet?: Array<SuullisenKielitaidonKoe2019>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  puhviKoe?: PuhviKoe2019
  oppimäärä: Koodistokoodiviite<'lukionoppimaara', string>
  lukionOppimääräSuoritettu?: boolean
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  ryhmä?: string
  koulutusmoduuli: LukionOppiaineidenOppimäärät2019
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<LukionOppiaineenSuoritus2019>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export const LukionOppiaineidenOppimäärienSuoritus2019 = (o: {
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'lukionaineopinnot'>
  suullisenKielitaidonKokeet?: Array<SuullisenKielitaidonKoe2019>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  puhviKoe?: PuhviKoe2019
  oppimäärä: Koodistokoodiviite<'lukionoppimaara', string>
  lukionOppimääräSuoritettu?: boolean
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  ryhmä?: string
  koulutusmoduuli: LukionOppiaineidenOppimäärät2019
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<LukionOppiaineenSuoritus2019>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}): LukionOppiaineidenOppimäärienSuoritus2019 => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'lukionaineopinnot',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.LukionOppiaineidenOppimäärienSuoritus2019',
  ...o
})

export const isLukionOppiaineidenOppimäärienSuoritus2019 = (
  a: any
): a is LukionOppiaineidenOppimäärienSuoritus2019 =>
  a?.$class === 'fi.oph.koski.schema.LukionOppiaineidenOppimäärienSuoritus2019'
