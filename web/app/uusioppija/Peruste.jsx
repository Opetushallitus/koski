import React from 'baret'
import {PerusteDropdown} from '../editor/PerusteDropdown.jsx'
import Text from '../Text.jsx'

export default options => <label className="peruste"><Text name="Peruste"/><PerusteDropdown {...options}/></label>
