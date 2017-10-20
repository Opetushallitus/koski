import React from 'baret'
import KoodistoDropdown from '../KoodistoDropdown.jsx'

export default ({suoritustyyppiAtom, suoritustyypitP, title}) => {
  return (<div>
    <KoodistoDropdown
      className="oppimaara"
      title={title}
      options = { suoritustyypitP }
      selected = { suoritustyyppiAtom }
    />
  </div> )
}
