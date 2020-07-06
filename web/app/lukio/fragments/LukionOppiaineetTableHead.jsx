import React from 'baret'
import Text from '../../i18n/Text'

export const LukionOppiaineetTableHead = ({laajuusyksikkö = 'kurssia', showArviointi = true, arvosanaHeader = <Text name='Arvosana (keskiarvo)'/>}) => (
  <thead>
  <tr>
    <th className='suorituksentila'></th>
    <th className='oppiaine'><Text name='Oppiaine'/></th>
    {<th className='laajuus'><Text name={`Laajuus (${laajuusyksikkö})`}/></th>}
    {showArviointi && <th className='arvosana'>{arvosanaHeader}</th>}
  </tr>
  <tr>
    <th colSpan='4'><hr/></th>
  </tr>
  </thead>
)

export const OmatTiedotLukionOppiaineetTableHead = ({arvosanaHeader = <Text name='Arvosana'/>}) => (
  <thead>
  <tr>
    <th className='oppiaine' scope='col'><Text name='Oppiaine'/></th>
    <th className='arvosana' scope='col'>{arvosanaHeader}</th>
  </tr>
  </thead>
)
