import {JaksoEditor} from './JaksoEditor.jsx'
import {OppiaineenSuoritusEditor} from './PerusopetuksenOppiaineetEditor.jsx'

export const editorMapping = {
  'erityisentuenpaatos': JaksoEditor,
  'perusopetuksenoppiaineensuoritus': OppiaineenSuoritusEditor,
  'perusopetuksenlisaopetuksenoppiaineensuoritus': OppiaineenSuoritusEditor
}