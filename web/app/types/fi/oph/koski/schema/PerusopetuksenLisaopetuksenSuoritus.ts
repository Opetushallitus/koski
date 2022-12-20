import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { PerusopetuksenLisäopetus } from './PerusopetuksenLisaopetus'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { PerusopetuksenLisäopetuksenAlisuoritus } from './PerusopetuksenLisaopetuksenAlisuoritus'
import { HenkilövahvistusPaikkakunnalla } from './HenkilovahvistusPaikkakunnalla'

/**
 * Laajennetun oppivelvollisuuden suoritus
 * Perusopetuksen lisäopetuksen suoritustiedot
 *
 * @see `fi.oph.koski.schema.PerusopetuksenLisäopetuksenSuoritus`
 */
export type PerusopetuksenLisäopetuksenSuoritus = {
  $class: 'fi.oph.koski.schema.PerusopetuksenLisäopetuksenSuoritus'
  muutSuorituskielet?: Array<Koodistokoodiviite<'kieli', string>>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'perusopetuksenlisaopetus'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  luokka?: string
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli: PerusopetuksenLisäopetus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<PerusopetuksenLisäopetuksenAlisuoritus>
  osaAikainenErityisopetus?: boolean
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export const PerusopetuksenLisäopetuksenSuoritus = (o: {
  muutSuorituskielet?: Array<Koodistokoodiviite<'kieli', string>>
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'perusopetuksenlisaopetus'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  luokka?: string
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli?: PerusopetuksenLisäopetus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<PerusopetuksenLisäopetuksenAlisuoritus>
  osaAikainenErityisopetus?: boolean
  vahvistus?: HenkilövahvistusPaikkakunnalla
}): PerusopetuksenLisäopetuksenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'perusopetuksenlisaopetus',
    koodistoUri: 'suorituksentyyppi'
  }),
  koulutusmoduuli: PerusopetuksenLisäopetus({
    tunniste: Koodistokoodiviite({
      koodiarvo: '020075',
      koodistoUri: 'koulutus'
    })
  }),
  $class: 'fi.oph.koski.schema.PerusopetuksenLisäopetuksenSuoritus',
  ...o
})

export const isPerusopetuksenLisäopetuksenSuoritus = (
  a: any
): a is PerusopetuksenLisäopetuksenSuoritus =>
  a?.$class === 'fi.oph.koski.schema.PerusopetuksenLisäopetuksenSuoritus'
