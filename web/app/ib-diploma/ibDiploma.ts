import { t } from '../i18n/i18n'
import { isIBDiplomanKieliOppiaine } from '../types/fi/oph/koski/schema/IBDiplomanKieliOppiaine'
import { IBDiplomanOppiaine } from '../types/fi/oph/koski/schema/IBDiplomanOppiaine'
import { IBExaminationSession } from '../types/fi/oph/koski/schema/IBExaminationSession'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'

const kuukaudet = [
  'Tammikuu',
  'Helmikuu',
  'Maaliskuu',
  'Huhtikuu',
  'Toukokuu',
  'Kesäkuu',
  'Heinäkuu',
  'Elokuu',
  'Syyskuu',
  'Lokakuu',
  'Marraskuu',
  'Joulukuu'
]

export const formatExaminationSession = (
  session: IBExaminationSession
): string => `${t(kuukaudet[session.month - 1])} ${session.year}`

export const oppiaineenNimi = (oppiaine: IBDiplomanOppiaine): string =>
  isIBDiplomanKieliOppiaine(oppiaine)
    ? `${t(oppiaine.tunniste.nimi)}, ${t(oppiaine.kieli.nimi)}`
    : t(oppiaine.tunniste.nimi)

export const arvosanaJaNimi = (arvosana: Koodistokoodiviite): string =>
  [arvosana.koodiarvo, t(arvosana.nimi)].filter(Boolean).join(' ')
