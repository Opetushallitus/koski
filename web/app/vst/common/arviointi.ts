import { todayISODate } from '../../date/date'
import { Arviointi } from '../../types/fi/oph/koski/schema/Arviointi'

export const createArviointi =
  <T extends Arviointi>(
    ctor: (p: { arvosana: T['arvosana']; päivä: string }) => T
  ) =>
  (arvosana: T['arvosana']) =>
    ctor({
      arvosana,
      päivä: todayISODate()
    })
