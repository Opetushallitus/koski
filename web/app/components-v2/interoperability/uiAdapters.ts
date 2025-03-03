import { IBEditor } from '../../ib/IBEditor'
import { KielitutkintoEditor } from '../../kielitutkinto/KielitutkintoEditor'
import { TaiteenPerusopetusEditor } from '../../taiteenperusopetus/TaiteenPerusopetusEditor'
import { VSTEditor } from '../../vst/VSTEditor'
import { AdaptedOpiskeluoikeusEditorCollection } from './useUiAdapter'

export const opiskeluoikeusEditors: AdaptedOpiskeluoikeusEditorCollection = {
  taiteenperusopetus: TaiteenPerusopetusEditor,
  vapaansivistystyonkoulutus: VSTEditor,
  ibtutkinto: IBEditor,
  kielitutkinto: KielitutkintoEditor
}
