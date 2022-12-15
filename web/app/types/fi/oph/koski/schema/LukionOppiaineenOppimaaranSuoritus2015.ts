import { LukionOppiaineenArviointi } from './LukionOppiaineenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LukionOppiaineTaiEiTiedossaOppiaine2015 } from './LukionOppiaineTaiEiTiedossaOppiaine2015'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { LukionKurssinSuoritus2015 } from './LukionKurssinSuoritus2015'
import { HenkilövahvistusPaikkakunnalla } from './HenkilovahvistusPaikkakunnalla'

/**
 * Lukion oppiaineen oppimäärän suoritustiedot
 *
 * @see `fi.oph.koski.schema.LukionOppiaineenOppimääränSuoritus2015`
 */
export type LukionOppiaineenOppimääränSuoritus2015 = {
  $class: 'fi.oph.koski.schema.LukionOppiaineenOppimääränSuoritus2015'
  arviointi?: Array<LukionOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'lukionoppiaineenoppimaara'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  lukionOppimääräSuoritettu?: boolean
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  ryhmä?: string
  koulutusmoduuli: LukionOppiaineTaiEiTiedossaOppiaine2015
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<LukionKurssinSuoritus2015>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export const LukionOppiaineenOppimääränSuoritus2015 = (o: {
  arviointi?: Array<LukionOppiaineenArviointi>
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'lukionoppiaineenoppimaara'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  lukionOppimääräSuoritettu?: boolean
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  ryhmä?: string
  koulutusmoduuli: LukionOppiaineTaiEiTiedossaOppiaine2015
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<LukionKurssinSuoritus2015>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}): LukionOppiaineenOppimääränSuoritus2015 => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'lukionoppiaineenoppimaara',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.LukionOppiaineenOppimääränSuoritus2015',
  ...o
})

export const isLukionOppiaineenOppimääränSuoritus2015 = (
  a: any
): a is LukionOppiaineenOppimääränSuoritus2015 =>
  a?.$class === 'LukionOppiaineenOppimääränSuoritus2015'
