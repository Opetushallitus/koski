import React from 'react'
import Http from './http'
import {ExistingOppija} from './Oppija.jsx'
import Bacon from 'baconjs'

export const omatTiedotContentP = () => innerContentP().map(inner =>
  ({
      content: (<div className="content-area omattiedot">
                  <nav className="sidebar omattiedot-navi"></nav>
                  {inner}
                </div>
              ),
      title: 'Omat tiedot'
  })
)

const omatTiedotP = () => Http.get('/koski/api/editor/omattiedot').toProperty().flatMapError((e) => e.httpStatus === 404 ? null : new Bacon.Error)

const innerContentP = () => omatTiedotP().map(oppija =>
  oppija ? <ExistingOppija oppija={oppija}/> : <div className="main-content ei-opiskeluoikeuksia">Tiedoillasi ei löydy opiskeluoikeuksia</div>
).startWith(<div className="main-content ajax-indicator-bg">Ladataan...</div>)