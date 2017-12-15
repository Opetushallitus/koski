import React from 'baret'
import {Editor} from '../editor/Editor'
import Text from '../i18n/Text'

export const Oppija = ({oppija}) => {
  return oppija.loading
    ? <div className="loading"/>
    : (
      <div>
        <div className="oppija-content">
          <h2><Text name="Opintosuorituksesi" /></h2>
          <Editor key={document.location.toString()} model={oppija}/>
        </div>
      </div>
  )
}