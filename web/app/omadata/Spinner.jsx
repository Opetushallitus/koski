import React from 'react'
import Text from '../i18n/Text'

const Spinner = () => (
  <div className="loading-container">
    <div className="ajax-indicator-bg">
      <Text name="Ladataan..." />
    </div>
  </div>
)

Spinner.displayName = 'Spinner'

export default Spinner
