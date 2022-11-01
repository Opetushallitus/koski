import React from 'baret'
import { enumValueToKoodiviiteLens } from '../koodisto/koodistot'
import KoodistoDropdown from '../koodisto/KoodistoDropdown'

const LisaaOsasuoritusDropdown = ({
  selectedAtom,
  osat,
  placeholder,
  title
}) => {
  return (
    <KoodistoDropdown
      className="tutkinnon-osat"
      title={title}
      options={osat}
      selected={selectedAtom.view(enumValueToKoodiviiteLens)}
      enableFilter="true"
      selectionText={placeholder}
      showKoodiarvo="true"
    />
  )
}

export default LisaaOsasuoritusDropdown
