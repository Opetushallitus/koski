import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Esiopetus } from './Esiopetus'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { HenkilövahvistusPaikkakunnalla } from './HenkilovahvistusPaikkakunnalla'

/**
 * EsiopetuksenSuoritus
 *
 * @see `fi.oph.koski.schema.EsiopetuksenSuoritus`
 */
export type EsiopetuksenSuoritus = {
  $class: 'fi.oph.koski.schema.EsiopetuksenSuoritus'
  muutSuorituskielet?: Array<Koodistokoodiviite<'kieli', string>>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'esiopetuksensuoritus'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  kielikylpykieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: Esiopetus
  toimipiste: OrganisaatioWithOid
  osaAikainenErityisopetus?: Array<
    Koodistokoodiviite<'osaaikainenerityisopetuslukuvuodenaikana', string>
  >
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export const EsiopetuksenSuoritus = (o: {
  muutSuorituskielet?: Array<Koodistokoodiviite<'kieli', string>>
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'esiopetuksensuoritus'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  kielikylpykieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: Esiopetus
  toimipiste: OrganisaatioWithOid
  osaAikainenErityisopetus?: Array<
    Koodistokoodiviite<'osaaikainenerityisopetuslukuvuodenaikana', string>
  >
  vahvistus?: HenkilövahvistusPaikkakunnalla
}): EsiopetuksenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'esiopetuksensuoritus',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.EsiopetuksenSuoritus',
  ...o
})

export const isEsiopetuksenSuoritus = (a: any): a is EsiopetuksenSuoritus =>
  a?.$class === 'fi.oph.koski.schema.EsiopetuksenSuoritus'
