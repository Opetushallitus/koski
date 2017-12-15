import R from 'ramda'
import {OptionalEditor} from './OptionalEditor'
import {ObjectEditor} from './ObjectEditor'
import {ArrayEditor} from './ArrayEditor'
import {EnumEditor} from './EnumEditor'
import {StringEditor} from './StringEditor'
import {NumberEditor} from './NumberEditor'
import {LocalizedStringEditor} from './LocalizedStringEditor'
import {DateEditor} from './DateEditor'
import {LaajuusEditor} from './LaajuusEditor'
import {VahvistusEditor} from './VahvistusEditor'
import {KoulutusmoduuliEditor} from './KoulutusmoduuliEditor'
import {PäivämääräväliEditor} from './PaivamaaravaliEditor'
import {InlineJaksoEditor, JaksoEditor} from './JaksoEditor'
import {BooleanEditor} from './BooleanEditor'
import {OppijaEditor} from './OppijaEditor'
import {OmatTiedotEditor} from './OmatTiedotEditor'
import {ToimipisteEditor} from './ToimipisteEditor'
import * as AmmatillinenEditors from './AmmatillinenEditors'
import {OrganisaatioEditor} from './OrganisaatioEditor'
import {OrganisaatioHenkilöEditor} from './OrganisaatioHenkiloEditor'
import {SuoritusEditor} from './SuoritusEditor'
import {PerusteEditor} from './PerusteEditor'
import {PerusopetuksenOppiaineRowEditor} from './PerusopetuksenOppiaineRowEditor'

export const editorMapping = R.mergeAll([{
  'optional': OptionalEditor,
  'object': ObjectEditor,
  'array': ArrayEditor,
  'string': StringEditor,
  'localizedstring': LocalizedStringEditor,
  'number': NumberEditor,
  'date': DateEditor,
  'boolean': BooleanEditor,
  'enum': EnumEditor,
  'oppijaeditorview': OppijaEditor,
  'omattiedoteditorview': OmatTiedotEditor,
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
  // Perusopetus
  'perusopetuksenoppiaineensuoritus': PerusopetuksenOppiaineRowEditor,
  'perusopetuksenlisaopetuksenoppiaineensuoritus': PerusopetuksenOppiaineRowEditor

}, AmmatillinenEditors.editorMapping])