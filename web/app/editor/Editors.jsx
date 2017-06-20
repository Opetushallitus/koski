import R from 'ramda'
import {OptionalEditor} from './OptionalEditor.jsx'
import {ObjectEditor} from './ObjectEditor.jsx'
import {ArrayEditor} from './ArrayEditor.jsx'
import {EnumEditor} from './EnumEditor.jsx'
import {StringEditor} from './StringEditor.jsx'
import {NumberEditor} from './NumberEditor.jsx'
import {LocalizedStringEditor} from './LocalizedStringEditor.jsx'
import {DateEditor} from './DateEditor.jsx'
import {LaajuusEditor} from './LaajuusEditor.jsx'
import {VahvistusEditor} from './VahvistusEditor.jsx'
import {KoulutusmoduuliEditor} from './KoulutusmoduuliEditor.jsx'
import {PäivämääräväliEditor} from './PaivamaaravaliEditor.jsx'
import {JaksoEditor} from './JaksoEditor.jsx'
import {BooleanEditor} from './BooleanEditor.jsx'
import {OppijaEditor} from './OppijaEditor.jsx'
import {ToimipisteEditor} from './ToimipisteEditor.jsx'
import * as AmmatillinenEditors from './AmmatillinenEditors.jsx'
import {OrganisaatioEditor} from './OrganisaatioEditor.jsx'
import {OrganisaatioHenkilöEditor} from './OrganisaatioHenkiloEditor.jsx'
import {SuoritusEditor} from './SuoritusEditor.jsx'
import {PerusteEditor} from './PerusteEditor.jsx'
import {OppiaineenSuoritusEditor} from './PerusopetuksenOppiaineetEditor.jsx'

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
  'paatasonsuoritus': SuoritusEditor,
  'vahvistus': VahvistusEditor,
  'laajuus' : LaajuusEditor,
  'koulutus' : KoulutusmoduuliEditor,
  'preibkoulutusmoduuli': KoulutusmoduuliEditor,
  'paatosjakso': PäivämääräväliEditor,
  'erityisentuenpaatos': JaksoEditor,
  'poissaolojakso': JaksoEditor,
  'jakso': JaksoEditor,
  'toimipiste': ToimipisteEditor,
  'organisaatiowithoid': OrganisaatioEditor,
  'organisaatiohenkilo': OrganisaatioHenkilöEditor,
  'peruste': PerusteEditor,
  // Perusopetus
  'erityisentuenpaatos': JaksoEditor,
  'perusopetuksenoppiaineensuoritus': OppiaineenSuoritusEditor,
  'perusopetuksenlisaopetuksenoppiaineensuoritus': OppiaineenSuoritusEditor

}, AmmatillinenEditors.editorMapping])