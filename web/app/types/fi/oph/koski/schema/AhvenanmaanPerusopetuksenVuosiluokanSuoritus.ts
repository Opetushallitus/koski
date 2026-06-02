import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { AhvenanmaanPerusopetuksenLuokkaAste } from './AhvenanmaanPerusopetuksenLuokkaAste'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { AhvenanmaanOppiaineenTaiToimintaAlueenSuoritus } from './AhvenanmaanOppiaineenTaiToimintaAlueenSuoritus'
import { HenkilövahvistusPaikkakunnalla } from './HenkilovahvistusPaikkakunnalla'
import { AhvenanmaanPerusopetuksenVastuuJaYhteistyöArviointi } from './AhvenanmaanPerusopetuksenVastuuJaYhteistyoArviointi'

/**
 * Ahvenanmaan perusopetuksen vuosiluokan suoritus. Nämä suoritukset näkyvät lukuvuositodistuksella.
 *
 * @see `fi.oph.koski.schema.AhvenanmaanPerusopetuksenVuosiluokanSuoritus`
 */
export type AhvenanmaanPerusopetuksenVuosiluokanSuoritus = {
  $class: 'fi.oph.koski.schema.AhvenanmaanPerusopetuksenVuosiluokanSuoritus'
  suorituskieli: Koodistokoodiviite<'kieli', string>
  luokka: string
  suoritustapa?: Koodistokoodiviite<'perusopetuksensuoritustapa', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  jääLuokalle: boolean
  koulutusmoduuli: AhvenanmaanPerusopetuksenLuokkaAste
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<AhvenanmaanOppiaineenTaiToimintaAlueenSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
  vastuuJaYhteistyöArvio?: AhvenanmaanPerusopetuksenVastuuJaYhteistyöArviointi
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'ahvenanmaanperusopetuksenvuosiluokka'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
}

export const AhvenanmaanPerusopetuksenVuosiluokanSuoritus = (o: {
  suorituskieli: Koodistokoodiviite<'kieli', string>
  luokka: string
  suoritustapa?: Koodistokoodiviite<'perusopetuksensuoritustapa', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  jääLuokalle?: boolean
  koulutusmoduuli: AhvenanmaanPerusopetuksenLuokkaAste
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<AhvenanmaanOppiaineenTaiToimintaAlueenSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
  vastuuJaYhteistyöArvio?: AhvenanmaanPerusopetuksenVastuuJaYhteistyöArviointi
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'ahvenanmaanperusopetuksenvuosiluokka'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
}): AhvenanmaanPerusopetuksenVuosiluokanSuoritus => ({
  jääLuokalle: false,
  $class: 'fi.oph.koski.schema.AhvenanmaanPerusopetuksenVuosiluokanSuoritus',
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ahvenanmaanperusopetuksenvuosiluokka',
    koodistoUri: 'suorituksentyyppi'
  }),
  ...o
})

AhvenanmaanPerusopetuksenVuosiluokanSuoritus.className =
  'fi.oph.koski.schema.AhvenanmaanPerusopetuksenVuosiluokanSuoritus' as const

export const isAhvenanmaanPerusopetuksenVuosiluokanSuoritus = (
  a: any
): a is AhvenanmaanPerusopetuksenVuosiluokanSuoritus =>
  a?.$class ===
  'fi.oph.koski.schema.AhvenanmaanPerusopetuksenVuosiluokanSuoritus'
