import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { PerusopetuksenOppiaineenArviointi } from './PerusopetuksenOppiaineenArviointi'
import { AikuistenPerusopetuksenOppiainenTaiEiTiedossaOppiaine } from './AikuistenPerusopetuksenOppiainenTaiEiTiedossaOppiaine'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { AikuistenPerusopetuksenKurssinTaiAlkuvaiheenKurssinSuoritus } from './AikuistenPerusopetuksenKurssinTaiAlkuvaiheenKurssinSuoritus'
import { HenkilövahvistusPaikkakunnalla } from './HenkilovahvistusPaikkakunnalla'

/**
 * Perusopetuksen yksittäisen oppiaineen oppimäärän suoritus erillisenä kokonaisuutena
 *
 * @see `fi.oph.koski.schema.AikuistenPerusopetuksenOppiaineenOppimääränSuoritus`
 */
export type AikuistenPerusopetuksenOppiaineenOppimääränSuoritus = {
  $class: 'fi.oph.koski.schema.AikuistenPerusopetuksenOppiaineenOppimääränSuoritus'
  muutSuorituskielet?: Array<Koodistokoodiviite<'kieli', string>>
  arviointi?: Array<PerusopetuksenOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'perusopetuksenoppiaineenoppimaara'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  suoritustapa: Koodistokoodiviite<'perusopetuksensuoritustapa', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli: AikuistenPerusopetuksenOppiainenTaiEiTiedossaOppiaine
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<AikuistenPerusopetuksenKurssinTaiAlkuvaiheenKurssinSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export const AikuistenPerusopetuksenOppiaineenOppimääränSuoritus = (o: {
  muutSuorituskielet?: Array<Koodistokoodiviite<'kieli', string>>
  arviointi?: Array<PerusopetuksenOppiaineenArviointi>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'perusopetuksenoppiaineenoppimaara'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  suoritustapa: Koodistokoodiviite<'perusopetuksensuoritustapa', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli: AikuistenPerusopetuksenOppiainenTaiEiTiedossaOppiaine
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<AikuistenPerusopetuksenKurssinTaiAlkuvaiheenKurssinSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}): AikuistenPerusopetuksenOppiaineenOppimääränSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'perusopetuksenoppiaineenoppimaara',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class:
    'fi.oph.koski.schema.AikuistenPerusopetuksenOppiaineenOppimääränSuoritus',
  ...o
})

AikuistenPerusopetuksenOppiaineenOppimääränSuoritus.className =
  'fi.oph.koski.schema.AikuistenPerusopetuksenOppiaineenOppimääränSuoritus' as const

export const isAikuistenPerusopetuksenOppiaineenOppimääränSuoritus = (
  a: any
): a is AikuistenPerusopetuksenOppiaineenOppimääränSuoritus =>
  a?.$class ===
  'fi.oph.koski.schema.AikuistenPerusopetuksenOppiaineenOppimääränSuoritus'
