import {
  NumeerinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019,
  isNumeerinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019
} from './NumeerinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019'
import {
  SanallinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019,
  isSanallinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019
} from './SanallinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019'

/**
 * LukionModuulinTaiPaikallisenOpintojaksonArviointi2019
 *
 * @see `fi.oph.koski.schema.LukionModuulinTaiPaikallisenOpintojaksonArviointi2019`
 */
export type LukionModuulinTaiPaikallisenOpintojaksonArviointi2019 =
  | NumeerinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019
  | SanallinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019

export const isLukionModuulinTaiPaikallisenOpintojaksonArviointi2019 = (
  a: any
): a is LukionModuulinTaiPaikallisenOpintojaksonArviointi2019 =>
  isNumeerinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019(a) ||
  isSanallinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019(a)
