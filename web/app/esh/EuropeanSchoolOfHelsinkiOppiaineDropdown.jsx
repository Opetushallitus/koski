import React from 'baret'
import Bacon from 'baconjs'
import { UusiOppiaineDropdown } from '../oppiaine/UusiOppiaineDropdown'

// TODO: TOR-1685

export const UusiEuropeanSchoolOfHelsinkiOppiaineDropdown = ({
  suoritukset = [],
  organisaatioOid,
  oppiaineenSuoritus,
  pakollinen,
  selected = Bacon.constant(undefined),
  resultCallback,
  placeholder,
  enableFilter = true,
  allowSelectingDuplicates = false
}) => (
  <UusiOppiaineDropdown
    suoritukset={suoritukset}
    organisaatioOid={organisaatioOid}
    oppiaineenSuoritukset={(oppiaineenSuoritus && [oppiaineenSuoritus]) || []}
    pakollinen={pakollinen}
    selected={selected}
    resultCallback={resultCallback}
    placeholder={placeholder}
    enableFilter={enableFilter}
    allowSelectingDuplicates={allowSelectingDuplicates}
  />
)
