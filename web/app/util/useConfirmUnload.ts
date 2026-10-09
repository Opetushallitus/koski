import { useEffect } from 'react'
import { t } from '../i18n/i18n'
import { lisääVaroitus, poistaVaroitus } from './router'

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
      lisääVaroitus(owner, {
        viesti: t('Haluatko varmasti poistua sivulta?'),
        hylkääMuutokset: editoriPoistuu
      })
      return () => poistaVaroitus(owner)
    }
  }, [enabled, owner])
}
