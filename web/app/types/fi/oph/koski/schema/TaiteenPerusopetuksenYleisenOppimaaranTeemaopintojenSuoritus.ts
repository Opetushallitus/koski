import { TaiteenPerusopetuksenArviointi } from './TaiteenPerusopetuksenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { TaiteenPerusopetuksenOpintotaso } from './TaiteenPerusopetuksenOpintotaso'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus } from './TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus'
import { HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla } from './HenkilovahvistusValinnaisellaTittelillaJaValinnaisellaPaikkakunnalla'

/**
 * Taiteen perusopetuksen yleisen oppimäärän teemaopintojen opintotason suoritus
 *
 * @see `fi.oph.koski.schema.TaiteenPerusopetuksenYleisenOppimääränTeemaopintojenSuoritus`
 */
export type TaiteenPerusopetuksenYleisenOppimääränTeemaopintojenSuoritus = {
  $class: 'fi.oph.koski.schema.TaiteenPerusopetuksenYleisenOppimääränTeemaopintojenSuoritus'
  arviointi?: Array<TaiteenPerusopetuksenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'taiteenperusopetuksenyleisenoppimaaranteemaopinnot'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  koulutusmoduuli: TaiteenPerusopetuksenOpintotaso
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus>
  vahvistus?: HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla
}

export const TaiteenPerusopetuksenYleisenOppimääränTeemaopintojenSuoritus =
  (o: {
    arviointi?: Array<TaiteenPerusopetuksenArviointi>
    tyyppi?: Koodistokoodiviite<
      'suorituksentyyppi',
      'taiteenperusopetuksenyleisenoppimaaranteemaopinnot'
    >
    tila?: Koodistokoodiviite<'suorituksentila', string>
    koulutusmoduuli: TaiteenPerusopetuksenOpintotaso
    toimipiste: OrganisaatioWithOid
    osasuoritukset?: Array<TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus>
    vahvistus?: HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla
  }): TaiteenPerusopetuksenYleisenOppimääränTeemaopintojenSuoritus => ({
    tyyppi: Koodistokoodiviite({
      koodiarvo: 'taiteenperusopetuksenyleisenoppimaaranteemaopinnot',
      koodistoUri: 'suorituksentyyppi'
    }),
    $class:
      'fi.oph.koski.schema.TaiteenPerusopetuksenYleisenOppimääränTeemaopintojenSuoritus',
    ...o
  })

TaiteenPerusopetuksenYleisenOppimääränTeemaopintojenSuoritus.className =
  'fi.oph.koski.schema.TaiteenPerusopetuksenYleisenOppimääränTeemaopintojenSuoritus' as const

export const isTaiteenPerusopetuksenYleisenOppimääränTeemaopintojenSuoritus = (
  a: any
): a is TaiteenPerusopetuksenYleisenOppimääränTeemaopintojenSuoritus =>
  a?.$class ===
  'fi.oph.koski.schema.TaiteenPerusopetuksenYleisenOppimääränTeemaopintojenSuoritus'
