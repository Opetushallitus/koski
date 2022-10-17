import React from 'react'
import Text from '../i18n/Text'

export default ({
  title = 'Omadata virhe',
  text = 'Tapahtuman käsittelyssä tapahtui virhe'
}) => (
  <div className="error-container">
    <div className="heading">
      <h1>
        <Text name={title} />
      </h1>
    </div>
    <div className="error-text">
      <Text name={text} />
    </div>
  </div>
)
