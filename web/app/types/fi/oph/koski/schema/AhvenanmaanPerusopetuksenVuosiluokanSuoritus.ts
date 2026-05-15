import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { AhvenanmaanOppiaineenTaiToimintaAlueenSuoritus } from './AhvenanmaanOppiaineenTaiToimintaAlueenSuoritus'
import { HenkilövahvistusPaikkakunnalla } from './HenkilovahvistusPaikkakunnalla'
import { AhvenanmaanPerusopetuksenKäyttäytymisenArviointi } from './AhvenanmaanPerusopetuksenKayttaytymisenArviointi'
import { AhvenanmaanPerusopetuksenLuokkaAste } from './AhvenanmaanPerusopetuksenLuokkaAste'

/**
 * Ahvenanmaan perusopetuksen vuosiluokan suoritus. Nämä suoritukset näkyvät lukuvuositodistuksella.
 *
 * @see `fi.oph.koski.schema.AhvenanmaanPerusopetuksenVuosiluokanSuoritus`
 */
export type AhvenanmaanPerusopetuksenVuosiluokanSuoritus = {
  $class: 'fi.oph.koski.schema.AhvenanmaanPerusopetuksenVuosiluokanSuoritus'
  muutSuorituskielet?: Array<Koodistokoodiviite<'kieli', string>>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'ahvenanmaanperusopetuksenvuosiluokka'
  >
  suorituskieli: Koodistokoodiviite<'kieli', string>
  luokka: string
  suoritustapa?: Koodistokoodiviite<'perusopetuksensuoritustapa', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  jääLuokalle: boolean
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<AhvenanmaanOppiaineenTaiToimintaAlueenSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  käyttäytymisenArvio?: AhvenanmaanPerusopetuksenKäyttäytymisenArviointi
  koulutusmoduuli: AhvenanmaanPerusopetuksenLuokkaAste
}

export const AhvenanmaanPerusopetuksenVuosiluokanSuoritus = (o: {
  muutSuorituskielet?: Array<Koodistokoodiviite<'kieli', string>>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'ahvenanmaanperusopetuksenvuosiluokka'
  >
  suorituskieli: Koodistokoodiviite<'kieli', string>
  luokka: string
  suoritustapa?: Koodistokoodiviite<'perusopetuksensuoritustapa', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  jääLuokalle?: boolean
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<AhvenanmaanOppiaineenTaiToimintaAlueenSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  käyttäytymisenArvio?: AhvenanmaanPerusopetuksenKäyttäytymisenArviointi
  koulutusmoduuli: AhvenanmaanPerusopetuksenLuokkaAste
}): AhvenanmaanPerusopetuksenVuosiluokanSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ahvenanmaanperusopetuksenvuosiluokka',
    koodistoUri: 'suorituksentyyppi'
  }),
  jääLuokalle: false,
  $class: 'fi.oph.koski.schema.AhvenanmaanPerusopetuksenVuosiluokanSuoritus',
  ...o
})

AhvenanmaanPerusopetuksenVuosiluokanSuoritus.className =
  'fi.oph.koski.schema.AhvenanmaanPerusopetuksenVuosiluokanSuoritus' as const

export const isAhvenanmaanPerusopetuksenVuosiluokanSuoritus = (
  a: any
): a is AhvenanmaanPerusopetuksenVuosiluokanSuoritus =>
  a?.$class ===
  'fi.oph.koski.schema.AhvenanmaanPerusopetuksenVuosiluokanSuoritus'
