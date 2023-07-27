import { TaiteenPerusopetusEditor } from '../../taiteenperusopetus/TaiteenPerusopetusEditor'
import { VSTEditor } from '../../vst/VSTEditor'
import { AdaptedOpiskeluoikeusEditorCollection } from './useUiAdapter'

const editors: AdaptedOpiskeluoikeusEditorCollection = {
  taiteenperusopetus: TaiteenPerusopetusEditor
}

// VST:n uusi käyttöliittymä - otetaan käyttöön feature flägin avulla
const url = new URL(window.location.href)
const useNewVST = url.searchParams.has('newVSTUI')
if (useNewVST) {
  console.warn(`WARNING: Using new user interface for VST.

  Stuff might be broken.


  To unset, remove the "newVSTUI" query parameter from your URL.
  `)
  editors['vapaansivistystyonkoulutus'] = VSTEditor
}

export const opiskeluoikeusEditors: AdaptedOpiskeluoikeusEditorCollection =
  editors
