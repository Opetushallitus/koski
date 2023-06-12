import { TaiteenPerusopetusEditor } from '../../taiteenperusopetus/TaiteenPerusopetusEditor'
import { VSTEditor } from '../../vst/VSTEditor'
import { AdaptedOpiskeluoikeusEditorCollection } from './useUiAdapter'

const editors: AdaptedOpiskeluoikeusEditorCollection = {
  taiteenperusopetus: TaiteenPerusopetusEditor
}

// VST:n uusi käyttöliittymä - otetaan käyttöön feature flägin avulla
const useNewVST = window.localStorage.getItem('new_vst_ui')
if (useNewVST !== null) {
  console.warn(`WARNING: Using new user interface for VST.

  Stuff might be broken.


  To unset, copy & paste to console:


  localStorage.removeItem("new_vst_ui")

  `)
  editors['vapaansivistystyonkoulutus'] = VSTEditor
}

export const opiskeluoikeusEditors: AdaptedOpiskeluoikeusEditorCollection =
  editors
