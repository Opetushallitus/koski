import { IBEditor } from '../../ib/IBEditor'
import { IBDiplomaEditor } from '../../ib-diploma/IBDiplomaEditor'
import { KielitutkintoEditor } from '../../kielitutkinto/KielitutkintoEditor'
import { TaiteenPerusopetusEditor } from '../../taiteenperusopetus/TaiteenPerusopetusEditor'
import { VSTEditor } from '../../vst/VSTEditor'
import { AdaptedOpiskeluoikeusEditorCollection } from './useUiAdapter'
import AmmatillinenEditor from '../../ammatillinen-v2/AmmatillinenEditor'
import PerusopetusEditor from '../../perusopetus-v2/PerusopetusEditor'
import AhvenanmaanPerusopetusEditor from '../../ahvenanmaan-perusopetus/AhvenanmaanPerusopetusEditor'
import AhvenanmaanAmmatillinenEditor from '../../ahvenanmaan-ammatillinen/AhvenanmaanAmmatillinenEditor'
import LukioEditor from '../../lukio-v2/LukioEditor'

export const opiskeluoikeusEditors: AdaptedOpiskeluoikeusEditorCollection = {
  taiteenperusopetus: TaiteenPerusopetusEditor,
  vapaansivistystyonkoulutus: VSTEditor,
  ibtutkinto: IBEditor,
  ibdiploma: IBDiplomaEditor,
  kielitutkinto: KielitutkintoEditor,
  ammatillinenkoulutus: AmmatillinenEditor,
  perusopetus: PerusopetusEditor,
  ahvenanmaanperusopetus: AhvenanmaanPerusopetusEditor,
  ahvenanmaanammatillinenkoulutus: AhvenanmaanAmmatillinenEditor,
  lukiokoulutus: LukioEditor
}
