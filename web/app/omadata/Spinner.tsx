import React from 'react'
import { Trans } from '../components-v2/texts/Trans'

const Spinner = () => (
  <div className="loading-container">
    <div className="ajax-indicator-bg">
      <Trans>{'Ladataan...'}</Trans>
    </div>
  </div>
)

export default Spinner
