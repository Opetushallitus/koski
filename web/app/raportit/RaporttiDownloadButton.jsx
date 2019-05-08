import React from 'baret'
import Text from '../i18n/Text'

export default ({inProgressP, disabled, onSubmit}) => (
  <div>
    {inProgressP.map((inProgress) =>
      inProgress
        ? <div className='ajax-indicator-bg'><Text name={'Ladataan raporttia. Lataaminen voi viedä useita minuutteja'}/></div>
        : <button className='koski-button' disabled={disabled} onClick={onSubmit}><Text name={'Lataa Excel-tiedosto'}/></button>
    )}
  </div>
)
