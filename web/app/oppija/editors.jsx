import * as R from 'ramda'
import {LaajuusEditor} from '../suoritus/LaajuusEditor'
import {VahvistusEditor} from '../suoritus/VahvistusEditor'
import {KoulutusmoduuliEditor} from '../suoritus/KoulutusmoduuliEditor'
import {PäivämääräväliEditor} from '../date/PaivamaaravaliEditor'
import {InlineJaksoEditor, JaksoEditor} from '../date/JaksoEditor'
import {OppijaEditor} from './OppijaEditor'
import {OmatTiedotEditor} from '../omattiedot/OmatTiedotEditor'
import {ToimipisteEditor} from '../organisaatio/ToimipisteEditor'
import ammatillinenEditors from '../ammatillinen/ammatillinenEditors'
import {OrganisaatioEditor} from '../organisaatio/OrganisaatioEditor'
import {OrganisaatioHenkilöEditor} from '../organisaatio/OrganisaatioHenkiloEditor'
import {SuoritusEditor} from '../suoritus/SuoritusEditor'
import {PerusteEditor} from '../suoritus/PerusteEditor'
import {PerusopetuksenOppiaineRowEditor} from '../perusopetus/PerusopetuksenOppiaineRowEditor'
import {OpiskeluoikeudenTilaEditor} from '../opiskeluoikeus/OpiskeluoikeudenTilaEditor'
import {OsaaminenTunnustettuEditor} from '../suoritus/OsaaminenTunnustettuEditor'
import genericEditors from '../editor/genericEditors'
import {LukukausiIlmoittautuminenEditor} from '../opiskeluoikeus/LukukausiIlmoittautuminenEditor'

const oppijaEditors = {
  'oppijaeditorview': OppijaEditor,
  'omattiedoteditorview': OmatTiedotEditor,
  'opiskeluoikeudentila': OpiskeluoikeudenTilaEditor,
  'paatasonsuoritus': SuoritusEditor,
  'vahvistus': VahvistusEditor,
  'laajuus' : LaajuusEditor,
  'koulutusmoduuli' : KoulutusmoduuliEditor,
  'paatosjakso': PäivämääräväliEditor,
  'erityisentuenpaatos': JaksoEditor,
  'poissaolojakso': JaksoEditor,
  'jakso': JaksoEditor,
  'erityisenkoulutustehtavanjakso': InlineJaksoEditor,
  'toimipiste': ToimipisteEditor,
  'organisaatiowithoid': OrganisaatioEditor,
  'organisaatiohenkilo': OrganisaatioHenkilöEditor,
  'peruste': PerusteEditor,
  'osaamisentunnustaminen': OsaaminenTunnustettuEditor,
  // Perusopetus
  'perusopetuksenoppiaineensuoritus': PerusopetuksenOppiaineRowEditor,
  'perusopetuksenlisaopetuksenoppiaineensuoritus': PerusopetuksenOppiaineRowEditor,
  // Korkeakoulu
  'lukukausi_ilmoittautuminen': LukukausiIlmoittautuminenEditor
}

// Combine all editor mappings into one object
export default R.mergeAll([oppijaEditors, ammatillinenEditors, genericEditors])
