import { todayISODate } from '../date/date'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { TaiteenPerusopetuksenArviointi } from '../types/fi/oph/koski/schema/TaiteenPerusopetuksenArviointi'

export const createTpoArviointi = (
  arvosana: Koodistokoodiviite<
    'arviointiasteikkotaiteenperusopetus',
    'hyvaksytty'
  >
): TaiteenPerusopetuksenArviointi =>
  TaiteenPerusopetuksenArviointi({
    arvosana,
    päivä: todayISODate()
  })
