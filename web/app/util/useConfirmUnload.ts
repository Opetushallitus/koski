import { useEffect } from 'react'
import { t } from '../i18n/i18n'
import { addExitHook, removeExitHook } from './exitHook'

// Varoittaa tallentamattomista muutoksista sivulta poistuttaessa ja vanhan
// käyttöliittymän navigoinnissa, joka kiinnittäisi editorin uudelleen.
export const useConfirmUnload = (enabled: boolean, owner: string) => {
  useEffect(() => {
    if (enabled) {
      addExitHook(t('Haluatko varmasti poistua sivulta?'), owner)
      return () => removeExitHook(owner)
    }
  }, [enabled, owner])
}
