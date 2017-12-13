import React from 'react'
import Text from '../Text.jsx'

const LanderInfo = () => (
  <div className={'lander'}>
    <h1><Text name="Koski-palvelu"/></h1>
    <p><Text name="Koulutustiedot luotettavasti yhdestä paikasta"/></p>
    <div className={'lander__caption'}>
      <p><Text name="Lander ingressi 1"/></p>
      <p><Text name="Lander ingressi 2"/></p>
      <p><Text name="Virkailijapalvelut löydät jatkossa osoitteesta"/> <a href="/koski/virkailija">{window.location.origin + '/koski/virkailija'}</a></p>
    </div>
  </div>
)

export {LanderInfo}
