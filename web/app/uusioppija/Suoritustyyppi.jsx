import React from 'baret'
import KoodistoDropdown from '../koodisto/KoodistoDropdown'

export default ({ suoritustyyppiAtom, suoritustyypitP, title }) => {
  return (
    <div>
      <KoodistoDropdown
        className="oppimaara"
        title={title}
        options={suoritustyypitP}
        selected={suoritustyyppiAtom}
      />
    </div>
  )
}
