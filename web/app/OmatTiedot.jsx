import React from 'react'
import Http from './http'
import {ExistingOppija} from './Oppija.jsx'
import Bacon from 'baconjs'

const omatTiedotP = () => Http.get('/koski/api/editor/omattiedot').toProperty().flatMapError((e) => e.httpStatus === 404 ? null : new Bacon.Error)

export const omatTiedotContentP = () => omatTiedotP().map((oppija) =>
  <div className="content-area omattiedot">
    <nav className="sidebar omattiedot-navi"></nav>
    { oppija ?
      <ExistingOppija className="main-content" oppija={oppija}/> :
      <div className="main-content ei-opiskeluoikeuksia">Tiedoillasi ei löydy opiskeluoikeuksia</div>
    }
  </div>
)