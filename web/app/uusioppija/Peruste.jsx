import React from 'baret'
import {PerusteDropdown} from '../editor/PerusteDropdown.jsx'
import Text from '../Text.jsx'

export default ({suoritusTyyppiP, perusteAtom}) => <label className="peruste"><Text name="Peruste"/><PerusteDropdown {...{suoritusTyyppiP, perusteAtom}}/></label>
