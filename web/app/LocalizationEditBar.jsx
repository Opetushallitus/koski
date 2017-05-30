import React from 'baret'
import {edit, cancelChanges, saveChanges, hasChanges} from './i18n-edit'

export default () => {
  let classNameP = edit.map(isEdit => 'localization-edit-bar' + (isEdit ? ' visible': ''))
  return (<div className={classNameP}>
    {'Lokalisoitujen tekstien muokkaus'}
    <a className="cancel" onClick={ cancelChanges }>{'Peruuta'}</a>
    <button disabled={hasChanges.not()} onClick={saveChanges}>{'Tallenna'}</button>
  </div>)
}