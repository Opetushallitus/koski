import { IBEditor } from '../../ib/IBEditor'
import { KielitutkintoEditor } from '../../kielitutkinto/KielitutkintoEditor'
import { TaiteenPerusopetusEditor } from '../../taiteenperusopetus/TaiteenPerusopetusEditor'
import { VSTEditor } from '../../vst/VSTEditor'
import { AdaptedOpiskeluoikeusEditorCollection } from './useUiAdapter'
import AmmatillinenEditor from '../../ammatillinen-v2/AmmatillinenEditor'
import PerusopetusEditor from '../../perusopetus-v2/PerusopetusEditor'
import AhvenanmaanPerusopetusEditor from '../../ahvenanmaan-perusopetus/AhvenanmaanPerusopetusEditor'

export const opiskeluoikeusEditors: AdaptedOpiskeluoikeusEditorCollection = {
  taiteenperusopetus: TaiteenPerusopetusEditor,
  vapaansivistystyonkoulutus: VSTEditor,
  ibtutkinto: IBEditor,
  kielitutkinto: KielitutkintoEditor,
  ammatillinenkoulutus: AmmatillinenEditor,
  perusopetus: PerusopetusEditor,
  ahvenanmaanperusopetus: AhvenanmaanPerusopetusEditor
}
