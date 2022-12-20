import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { PerusopetuksenOppiaineenArviointi } from './PerusopetuksenOppiaineenArviointi'
import { NuortenPerusopetuksenOppiainenTaiEiTiedossaOppiaine } from './NuortenPerusopetuksenOppiainenTaiEiTiedossaOppiaine'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { HenkilövahvistusPaikkakunnalla } from './HenkilovahvistusPaikkakunnalla'

/**
 * Perusopetuksen yksittäisen oppiaineen oppimäärän suoritus erillisenä kokonaisuutena
 *
 * @see `fi.oph.koski.schema.NuortenPerusopetuksenOppiaineenOppimääränSuoritus`
 */
export type NuortenPerusopetuksenOppiaineenOppimääränSuoritus = {
  $class: 'fi.oph.koski.schema.NuortenPerusopetuksenOppiaineenOppimääränSuoritus'
  muutSuorituskielet?: Array<Koodistokoodiviite<'kieli', string>>
  arviointi?: Array<PerusopetuksenOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'nuortenperusopetuksenoppiaineenoppimaara'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  luokkaAste?: Koodistokoodiviite<'perusopetuksenluokkaaste', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  suoritustapa: Koodistokoodiviite<'perusopetuksensuoritustapa', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli: NuortenPerusopetuksenOppiainenTaiEiTiedossaOppiaine
  toimipiste: OrganisaatioWithOid
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export const NuortenPerusopetuksenOppiaineenOppimääränSuoritus = (o: {
  muutSuorituskielet?: Array<Koodistokoodiviite<'kieli', string>>
  arviointi?: Array<PerusopetuksenOppiaineenArviointi>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'nuortenperusopetuksenoppiaineenoppimaara'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  luokkaAste?: Koodistokoodiviite<'perusopetuksenluokkaaste', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  suoritustapa: Koodistokoodiviite<'perusopetuksensuoritustapa', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli: NuortenPerusopetuksenOppiainenTaiEiTiedossaOppiaine
  toimipiste: OrganisaatioWithOid
  vahvistus?: HenkilövahvistusPaikkakunnalla
}): NuortenPerusopetuksenOppiaineenOppimääränSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'nuortenperusopetuksenoppiaineenoppimaara',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class:
    'fi.oph.koski.schema.NuortenPerusopetuksenOppiaineenOppimääränSuoritus',
  ...o
})

export const isNuortenPerusopetuksenOppiaineenOppimääränSuoritus = (
  a: any
): a is NuortenPerusopetuksenOppiaineenOppimääränSuoritus =>
  a?.$class ===
  'fi.oph.koski.schema.NuortenPerusopetuksenOppiaineenOppimääränSuoritus'
