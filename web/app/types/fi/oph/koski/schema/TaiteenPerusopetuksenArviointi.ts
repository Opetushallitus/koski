import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Arvioitsija } from './Arvioitsija'

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
  arvioitsijat?: Array<Arvioitsija>
  hyväksytty?: boolean
}

export const TaiteenPerusopetuksenArviointi = (o: {
  arvosana?: Koodistokoodiviite<
    'arviointiasteikkotaiteenperusopetus',
    'hyvaksytty'
  >
  päivä: string
  arvioitsijat?: Array<Arvioitsija>
  hyväksytty?: boolean
}): TaiteenPerusopetuksenArviointi => ({
  $class: 'fi.oph.koski.schema.TaiteenPerusopetuksenArviointi',
  arvosana: Koodistokoodiviite({
    koodiarvo: 'hyvaksytty',
    koodistoUri: 'arviointiasteikkotaiteenperusopetus'
  }),
  ...o
})

export const isTaiteenPerusopetuksenArviointi = (
  a: any
): a is TaiteenPerusopetuksenArviointi =>
  a?.$class === 'fi.oph.koski.schema.TaiteenPerusopetuksenArviointi'
