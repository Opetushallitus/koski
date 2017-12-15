import React from 'baret'
import {PerusteDropdown} from '../editor/PerusteDropdown'
import Text from '../Text'

export default options => <label className="peruste"><Text name="Peruste"/><PerusteDropdown {...options}/></label>
