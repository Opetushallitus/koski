import React from 'baret'
import {editAtom, cancelChanges, saveChanges, hasChanges, languages} from './i18n-edit'
import {lang, setLang} from './i18n'
import Text from './Text.jsx'
import R from 'ramda'
import Atom from 'bacon.atom'

export default ({user}) => {
  if (!user.hasLocalizationWriteAccess) {
    return null
  }
  let classNameP = editAtom.map(isEdit => 'localization-edit-bar' + (isEdit ? ' visible': ''))
  let editAll = Atom(false)
  return (<div>
    {
      editAtom.and(editAll).map(e => e && <EditAllTexts/> )
    }
    <div className={classNameP}>
      {'Lokalisoitujen tekstien muokkaus'}
      <a className="cancel" onClick={ cancelChanges }>{'Peruuta'}</a>
      <button disabled={hasChanges.not()} onClick={saveChanges}>{'Tallenna'}</button>
      {
        hasChanges.not().map(show => show && <span className="languages">{'Vaihda kieli: '}{
          languages.map(l => <a key={l} className={l + (l == lang ? ' selected': '')} onClick={() => {localStorage.editLocalizations = true; setLang(l)}}>{l}</a>)
        }</span>)
      }
      <label className="show-all">{'Näytä kaikki tekstit'}<input type="checkbox" checked={editAll} onChange={e => editAll.set(e.target.checked)}/></label>
    </div>
  </div>)
}

const EditAllTexts = () => {
  return (<div className="localization-edit-all">
    <table>
      <thead>
        <tr><th></th><th>{'Ruotsiksi'}</th><th>{'Englanniksi'}</th></tr>
      </thead>
      <tbody> {
        R.keys(window.koskiLocalizationMap).sort((a, b) => a.localeCompare(b, 'fi')).map(key => {
          return (<tr key={key}>{
            languages.map(l => <td><Text key={l} name={key} lang={l} edit={l != 'fi'}/></td>)
          }</tr>)
        })
      }</tbody>
    </table>
  </div>)
}