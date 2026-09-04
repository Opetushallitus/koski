import { UusiOpiskeluoikeusDialogState } from './state'

export const useDefaultKieli = (
  state: UusiOpiskeluoikeusDialogState
): string => {
  switch (state.opiskeluoikeus.value?.koodiarvo) {
    case 'diatutkinto':
      return 'kieli_DE'
    // Ahvenanmaan perusopetus on ruotsinkielistä.
    case 'ahvenanmaanperusopetus':
      return 'kieli_SV'
    default:
      return 'kieli_FI'
  }
}
