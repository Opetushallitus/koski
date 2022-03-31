import React from 'baret'
import Bacon from 'baconjs'
import {UusiOppiaineDropdown} from '../oppiaine/UusiOppiaineDropdown'

export const UusiPerusopetuksenOppiaineDropdown = ({ suoritukset = [], organisaatioOid, oppiaineenSuoritus, pakollinen, selected = Bacon.constant(undefined), resultCallback, placeholder, enableFilter = true }) => (
  <UusiOppiaineDropdown
    suoritukset={suoritukset}
    organisaatioOid={organisaatioOid}
    oppiaineenSuoritukset={oppiaineenSuoritus && [oppiaineenSuoritus] || []}
    pakollinen={pakollinen}
    selected={selected}
    resultCallback={resultCallback}
    placeholder={placeholder}
    enableFilter={enableFilter}
  />
)

UusiPerusopetuksenOppiaineDropdown.displayName = 'UusiPerusopetuksenOppiaineDropdown'
