import { UusiOpiskeluoikeusDialogState } from './state'

export const useDefaultKieli = (
  state: UusiOpiskeluoikeusDialogState
): string => {
  switch (state.opiskeluoikeus.value?.koodiarvo) {
    case 'diatutkinto':
      return 'kieli_DE'
    // Ahvenanmaan koulutus on ruotsinkielistä.
    case 'ahvenanmaanperusopetus':
    case 'ahvenanmaanammatillinenkoulutus':
      return 'kieli_SV'
    default:
      return 'kieli_FI'
  }
}
