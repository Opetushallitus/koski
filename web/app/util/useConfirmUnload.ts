import { useEffect } from 'react'
import { t } from '../i18n/i18n'
import { addNavigationGuard, removeNavigationGuard } from './router'

// Uuden käyttöliittymän editori poistuu, kun navigointi vaihtaa sivun,
// opiskeluoikeuden tyypin välilehden tai katseltavan version (OppijaEditor).
const editoriPoistuu = (mistä: URL, mihin: URL) =>
  mistä.pathname !== mihin.pathname ||
  ['opiskeluoikeudenTyyppi', 'opiskeluoikeus', 'versionumero'].some(
    (key) => mistä.searchParams.get(key) !== mihin.searchParams.get(key)
  )

// Varoittaa tallentamattomista muutoksista ennen navigointia, joka poistaa
// editorin, ja sivulta poistuttaessa.
export const useConfirmUnload = (enabled: boolean, owner: string) => {
  useEffect(() => {
    if (enabled) {
      addNavigationGuard(owner, {
        message: t('Haluatko varmasti poistua sivulta?'),
        discardsChanges: editoriPoistuu
      })
      return () => removeNavigationGuard(owner)
    }
  }, [enabled, owner])
}
