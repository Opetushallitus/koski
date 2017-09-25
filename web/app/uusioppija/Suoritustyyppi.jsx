import React from 'baret'
import KoodistoDropdown from '../KoodistoDropdown.jsx'

export default ({suoritustyyppiAtom, suoritustyypitP}) => {
  return (<div>
    <KoodistoDropdown
      className="oppimaara"
      title="Oppimäärä"
      options = { suoritustyypitP }
      selected = { suoritustyyppiAtom }
    />
  </div> )
}
