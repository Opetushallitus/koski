import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import * as L from 'partial.lenses'
import {Editor} from './Editor.jsx'
import Dropdown from '../Dropdown.jsx'
import {pushModelValue, modelData, modelSetValue} from './EditorModel'
import {deleteOrganizationalPreference, getOrganizationalPreferences} from '../organizationalPreferences'
import {t} from '../i18n'

let preferenceKey = 'myöntäjät'
let nimi = h => modelData(h, 'nimi')
let nimiJaTitteli = h => nimi(h) && (modelData(h, 'nimi') + ', ' + t(modelData(h, 'titteli')))
let queryFilter = q => o => nimi(o).toLowerCase().indexOf(q.toLowerCase()) >= 0
var newItemLens = L.compose('value', 'newItem')

export const OrganisaatioHenkilöEditor = ({model}) => {
  let query = Atom('')
  let removeBus = Bacon.Bus()

  let myöntäjäOrganisaatio = model.context.myöntäjäOrganisaatio
  let organisaatioOid = modelData(myöntäjäOrganisaatio).oid

  let newItem = modelSetValue(L.set(newItemLens, true, model), myöntäjäOrganisaatio.value, 'organisaatio')
  let isNewItem = (o) => L.get(newItemLens, o)

  let myöntäjätPreferenceListP = getOrganizationalPreferences(organisaatioOid, preferenceKey)
    .concat(removeBus.flatMap(poistettavaNimi => deleteOrganizationalPreference(organisaatioOid, preferenceKey, poistettavaNimi)))

  let kaikkiMyöntäjätP = myöntäjätPreferenceListP
    .map('.map', m => modelSetValue(model, m.value))

  let myöntäjätP = Bacon.combineWith(kaikkiMyöntäjätP, query, (xs, q) => !q ? xs : xs.filter(queryFilter(q)))
    .startWith([])

  return (<span className="organisaatiohenkilo">
    <Dropdown
      displayValue={o => isNewItem(o) ? 'Lisää henkilö' : nimiJaTitteli(o)}
      keyValue={o => isNewItem(o) ? '_new' : nimi(o)}
      options={myöntäjätP}
      enableFilter={true}
      selected={model}
      onSelectionChanged={h => pushModelValue(model, h.value)}
      newItem={newItem}
      isRemovable={() => true}
      onRemoval={poistettavaNimi => removeBus.push(poistettavaNimi)}
    />
    {
      isNewItem(model) && (<span className="uusi-henkilo">
        <span className="nimi"><Editor model={model} path="nimi" placeholder={t('nimi')}/></span>
        <span className="titteli"><Editor model={model} path="titteli" placeholder={t('titteli')}/></span></span>
      )
    }
  </span>)
}