import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * TaiteenPerusopetuksenArviointi
 *
 * @see `fi.oph.koski.schema.TaiteenPerusopetuksenArviointi`
 */
export type TaiteenPerusopetuksenArviointi = {
  $class: 'fi.oph.koski.schema.TaiteenPerusopetuksenArviointi'
  arvosana: Koodistokoodiviite<
    'arviointiasteikkotaiteenperusopetus',
    'hyvaksytty'
  >
  päivä: string
  hyväksytty?: boolean
}

export const TaiteenPerusopetuksenArviointi = (o: {
  arvosana?: Koodistokoodiviite<
    'arviointiasteikkotaiteenperusopetus',
    'hyvaksytty'
  >
  päivä: string
  hyväksytty?: boolean
}): TaiteenPerusopetuksenArviointi => ({
  $class: 'fi.oph.koski.schema.TaiteenPerusopetuksenArviointi',
  arvosana: Koodistokoodiviite({
    koodiarvo: 'hyvaksytty',
    koodistoUri: 'arviointiasteikkotaiteenperusopetus'
  }),
  ...o
})

TaiteenPerusopetuksenArviointi.className =
  'fi.oph.koski.schema.TaiteenPerusopetuksenArviointi' as const

export const isTaiteenPerusopetuksenArviointi = (
  a: any
): a is TaiteenPerusopetuksenArviointi =>
  a?.$class === 'fi.oph.koski.schema.TaiteenPerusopetuksenArviointi'
