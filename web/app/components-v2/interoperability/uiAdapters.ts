import { IBEditor } from '../../ib/IBEditor'
import { KielitutkintoEditor } from '../../kielitutkinto/KielitutkintoEditor'
import { TaiteenPerusopetusEditor } from '../../taiteenperusopetus/TaiteenPerusopetusEditor'
import { VSTEditor } from '../../vst/VSTEditor'
import { AdaptedOpiskeluoikeusEditorCollection } from './useUiAdapter'
import AmmatillinenTutkintoOsittainenEditor from '../../ammatillinen-v2/AmmatillinentutkintoOsittainen'

export const opiskeluoikeusEditors: AdaptedOpiskeluoikeusEditorCollection = {
  taiteenperusopetus: TaiteenPerusopetusEditor,
  vapaansivistystyonkoulutus: VSTEditor,
  ibtutkinto: IBEditor,
  kielitutkinto: KielitutkintoEditor,
  ammatillinenkoulutus: AmmatillinenTutkintoOsittainenEditor
}
