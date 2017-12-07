import React from 'react'
import Http from './http'
import {ExistingOppija} from './Oppija.jsx'
import {Editor} from './editor/Editor.jsx'
import Bacon from 'baconjs'
import Text from './Text.jsx'
import {editorMapping} from './editor/Editors.jsx'
import {userP} from './user'
import {addContext} from './editor/EditorModel'

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

const omatTiedotP = () => Bacon.combineWith(
  Http.cachedGet('/koski/api/editor/omattiedot', { errorMapper: (e) => e.httpStatus === 404 ? null : new Bacon.Error}).toProperty(),
  userP,
  (omattiedot, user) => {
    return omattiedot && addContext(omattiedot, {kansalainen: user.kansalainen})
  }
)

const innerContentP = () => omatTiedotP().map(oppija =>
  oppija
    ? <div className="main-content oppija"><ExistingOppija oppija={Editor.setupContext(oppija, {editorMapping})} stateP={Bacon.constant('viewing')}/></div>
    : <div className="main-content ei-opiskeluoikeuksia"><Text name="Tiedoillasi ei löydy opiskeluoikeuksia"/></div>
).startWith(<div className="main-content ajax-indicator-bg"><Text name="Ladataan..."/></div>)