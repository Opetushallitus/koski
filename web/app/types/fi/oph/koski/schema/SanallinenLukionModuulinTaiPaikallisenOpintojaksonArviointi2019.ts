import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * SanallinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019
 *
 * @see `fi.oph.koski.schema.SanallinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019`
 */
export type SanallinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019 = {
  $class: 'fi.oph.koski.schema.SanallinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019'
  arvosana: Koodistokoodiviite<'arviointiasteikkoyleissivistava', 'H' | 'S'>
  kuvaus?: LocalizedString
  päivä: string
  hyväksytty?: boolean
}

export const SanallinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019 =
  (o: {
    arvosana: Koodistokoodiviite<'arviointiasteikkoyleissivistava', 'H' | 'S'>
    kuvaus?: LocalizedString
    päivä: string
    hyväksytty?: boolean
  }): SanallinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019 => ({
    $class:
      'fi.oph.koski.schema.SanallinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019',
    ...o
  })

SanallinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019.className =
  'fi.oph.koski.schema.SanallinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019' as const

export const isSanallinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019 =
  (
    a: any
  ): a is SanallinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019 =>
    a?.$class ===
    'fi.oph.koski.schema.SanallinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019'
