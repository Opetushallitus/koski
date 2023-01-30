import { TaiteenPerusopetuksenArviointi } from './TaiteenPerusopetuksenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { TaiteenPerusopetuksenOpintotaso } from './TaiteenPerusopetuksenOpintotaso'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus } from './TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus'
import { HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla } from './HenkilovahvistusValinnaisellaTittelillaJaValinnaisellaPaikkakunnalla'

/**
 * Taiteen perusopetuksen laajan oppimäärän syventävien opintojen opintotason suoritus
 *
 * @see `fi.oph.koski.schema.TaiteenPerusopetuksenLaajanOppimääränSyventävienOpintojenSuoritus`
 */
export type TaiteenPerusopetuksenLaajanOppimääränSyventävienOpintojenSuoritus =
  {
    $class: 'fi.oph.koski.schema.TaiteenPerusopetuksenLaajanOppimääränSyventävienOpintojenSuoritus'
    arviointi?: Array<TaiteenPerusopetuksenArviointi>
    tyyppi: Koodistokoodiviite<
      'suorituksentyyppi',
      'taiteenperusopetuksenlaajanoppimaaransyventavatopinnot'
    >
    tila?: Koodistokoodiviite<'suorituksentila', string>
    koulutusmoduuli: TaiteenPerusopetuksenOpintotaso
    toimipiste: OrganisaatioWithOid
    osasuoritukset?: Array<TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus>
    vahvistus?: HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla
  }

export const TaiteenPerusopetuksenLaajanOppimääränSyventävienOpintojenSuoritus =
  (o: {
    arviointi?: Array<TaiteenPerusopetuksenArviointi>
    tyyppi?: Koodistokoodiviite<
      'suorituksentyyppi',
      'taiteenperusopetuksenlaajanoppimaaransyventavatopinnot'
    >
    tila?: Koodistokoodiviite<'suorituksentila', string>
    koulutusmoduuli: TaiteenPerusopetuksenOpintotaso
    toimipiste: OrganisaatioWithOid
    osasuoritukset?: Array<TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus>
    vahvistus?: HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla
  }): TaiteenPerusopetuksenLaajanOppimääränSyventävienOpintojenSuoritus => ({
    tyyppi: Koodistokoodiviite({
      koodiarvo: 'taiteenperusopetuksenlaajanoppimaaransyventavatopinnot',
      koodistoUri: 'suorituksentyyppi'
    }),
    $class:
      'fi.oph.koski.schema.TaiteenPerusopetuksenLaajanOppimääränSyventävienOpintojenSuoritus',
    ...o
  })

TaiteenPerusopetuksenLaajanOppimääränSyventävienOpintojenSuoritus.className =
  'fi.oph.koski.schema.TaiteenPerusopetuksenLaajanOppimääränSyventävienOpintojenSuoritus' as const

export const isTaiteenPerusopetuksenLaajanOppimääränSyventävienOpintojenSuoritus =
  (
    a: any
  ): a is TaiteenPerusopetuksenLaajanOppimääränSyventävienOpintojenSuoritus =>
    a?.$class ===
    'fi.oph.koski.schema.TaiteenPerusopetuksenLaajanOppimääränSyventävienOpintojenSuoritus'
