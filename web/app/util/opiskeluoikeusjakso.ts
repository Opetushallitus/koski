import { isAikuistenPerusopetuksenOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/AikuistenPerusopetuksenOpiskeluoikeusjakso'
import { isAmmatillinenOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/AmmatillinenOpiskeluoikeusjakso'
import { isDIAOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/DIAOpiskeluoikeusjakso'
import { isEuropeanSchoolOfHelsinkiOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso'
import { isInternationalSchoolOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/InternationalSchoolOpiskeluoikeusjakso'
import { isLukionOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/LukionOpiskeluoikeusjakso'
import { isMuunKuinSäännellynKoulutuksenOpiskeluoikeudenJakso } from '../types/fi/oph/koski/schema/MuunKuinSaannellynKoulutuksenOpiskeluoikeudenJakso'
import { Opiskeluoikeusjakso } from '../types/fi/oph/koski/schema/Opiskeluoikeusjakso'
import { isTutkintokoulutukseenValmentavanOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/TutkintokoulutukseenValmentavanOpiskeluoikeusjakso'
import { isVapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenOpiskeluoikeusjakso'

export type RahoituksellinenOpiskeluoikeusjakso = Extract<
  Opiskeluoikeusjakso,
  { opintojenRahoitus?: any }
>

export const isRahoituksellinenOpiskeluoikeusjakso = (
  x: Opiskeluoikeusjakso
): x is RahoituksellinenOpiskeluoikeusjakso =>
  isAikuistenPerusopetuksenOpiskeluoikeusjakso(x) ||
  isAmmatillinenOpiskeluoikeusjakso(x) ||
  isDIAOpiskeluoikeusjakso(x) ||
  isEuropeanSchoolOfHelsinkiOpiskeluoikeusjakso(x) ||
  isInternationalSchoolOpiskeluoikeusjakso(x) ||
  isLukionOpiskeluoikeusjakso(x) ||
  isMuunKuinSäännellynKoulutuksenOpiskeluoikeudenJakso(x) ||
  isTutkintokoulutukseenValmentavanOpiskeluoikeusjakso(x) ||
  isVapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso(x) ||
  isLukionOpiskeluoikeusjakso(x)
