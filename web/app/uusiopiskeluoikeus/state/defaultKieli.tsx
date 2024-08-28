import { UusiOpiskeluoikeusDialogState } from './state'

export const useDefaultKieli = (
  state: UusiOpiskeluoikeusDialogState
): string => {
  return state.opiskeluoikeus.value?.koodiarvo === 'diatutkinto'
    ? 'kieli_DE'
    : 'kieli_FI'
}
