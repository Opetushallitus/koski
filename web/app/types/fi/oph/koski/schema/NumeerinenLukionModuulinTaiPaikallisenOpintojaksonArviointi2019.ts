import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * NumeerinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019
 *
 * @see `fi.oph.koski.schema.NumeerinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019`
 */
export type NumeerinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019 = {
  $class: 'fi.oph.koski.schema.NumeerinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019'
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoyleissivistava',
    '4' | '5' | '6' | '7' | '8' | '9' | '10'
  >
  päivä: string
  hyväksytty?: boolean
}

export const NumeerinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019 =
  (o: {
    arvosana: Koodistokoodiviite<
      'arviointiasteikkoyleissivistava',
      '4' | '5' | '6' | '7' | '8' | '9' | '10'
    >
    päivä: string
    hyväksytty?: boolean
  }): NumeerinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019 => ({
    $class:
      'fi.oph.koski.schema.NumeerinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019',
    ...o
  })

export const isNumeerinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019 =
  (
    a: any
  ): a is NumeerinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019 =>
    a?.$class ===
    'fi.oph.koski.schema.NumeerinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019'
