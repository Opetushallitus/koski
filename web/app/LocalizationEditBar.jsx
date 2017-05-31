import React from 'baret'
import {edit, cancelChanges, saveChanges, hasChanges, languages, setLang} from './i18n-edit'
import {lang} from './i18n'

export default () => {
  let classNameP = edit.map(isEdit => 'localization-edit-bar' + (isEdit ? ' visible': ''))
  return (<div className={classNameP}>
    {'Lokalisoitujen tekstien muokkaus'}
    <a className="cancel" onClick={ cancelChanges }>{'Peruuta'}</a>
    <button disabled={hasChanges.not()} onClick={saveChanges}>{'Tallenna'}</button>
    {
      hasChanges.not().map(show => show && <span className="languages">{'Vaihda kieli: '}{
        languages.map(l => <a key={l} className={l == lang ? 'selected': null} onClick={() => setLang(l)}>{l}</a>)
      }</span>)
    }
  </div>)
}