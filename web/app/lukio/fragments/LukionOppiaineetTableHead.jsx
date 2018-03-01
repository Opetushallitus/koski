import React from 'react'
import Text from '../../i18n/Text'

export const LukionOppiaineetTableHead = () => (
  <thead>
  <tr>
    <th className='suorituksentila'></th>
    <th className='oppiaine'><Text name='Oppiaine'/></th>
    <th className='laajuus'><Text name='Laajuus (kurssia)'/></th>
    <th className='arvosana'><Text name='Arvosana (keskiarvo)'/></th>
  </tr>
  <tr>
    <th colSpan='4'><hr/></th>
  </tr>
  </thead>
)
