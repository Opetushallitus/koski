import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import * as L from 'partial.lenses'
import { Editor } from '../editor/Editor'
import Dropdown from '../components/Dropdown'
import { pushModelValue, modelData, modelSetValue } from '../editor/EditorModel'
import {
  deleteOrganizationalPreference,
  getOrganizationalPreferences
} from '../virkailija/organizationalPreferences'
import { t } from '../i18n/i18n'

const preferenceKey = 'myöntäjät'
const nimi = (h) => modelData(h, 'nimi')
const nimiJaTitteli = (h) =>
  nimi(h) && modelData(h, 'nimi') + ', ' + t(modelData(h, 'titteli'))
const queryFilter = (q) => (o) =>
  nimi(o).toLowerCase().indexOf(q.toLowerCase()) >= 0
const newItemLens = L.compose('value', 'newItem')

export const OrganisaatioHenkilöEditor = ({ model }) => {
  const query = Atom('')
  const removeBus = Bacon.Bus()

  const myöntäjäOrganisaatio = model.context.myöntäjäOrganisaatio
  const organisaatioOid = modelData(myöntäjäOrganisaatio).oid
  const koulutustoimijaOid = modelData(
    model.context.opiskeluoikeus,
    'koulutustoimija.oid'
  )

  const newItem = modelSetValue(
    L.set(newItemLens, true, model),
    myöntäjäOrganisaatio.value,
    'organisaatio'
  )
  const isNewItem = (o) => L.get(newItemLens, o)

  const myöntäjätPreferenceListP = getOrganizationalPreferences(
    organisaatioOid,
    preferenceKey,
    koulutustoimijaOid
  ).concat(
    removeBus.flatMap((poistettavaNimi) =>
      deleteOrganizationalPreference(
        organisaatioOid,
        preferenceKey,
        poistettavaNimi,
        koulutustoimijaOid
      )
    )
  )

  const kaikkiMyöntäjätP = myöntäjätPreferenceListP.map('.map', (m) =>
    modelSetValue(model, m.value)
  )

  const myöntäjätP = Bacon.combineWith(kaikkiMyöntäjätP, query, (xs, q) =>
    !q ? xs : xs.filter(queryFilter(q))
  ).startWith([])

  return (
    <span className="organisaatiohenkilo">
      <Dropdown
        displayValue={(o) =>
          isNewItem(o) ? 'Lisää henkilö' : nimiJaTitteli(o)
        }
        keyValue={(o) => (isNewItem(o) ? '_new' : nimi(o))}
        options={myöntäjätP}
        enableFilter={true}
        selected={model}
        onSelectionChanged={(h) => pushModelValue(model, h.value)}
        newItem={newItem}
        isRemovable={() => true}
        onRemoval={(poistettavaHlo) => removeBus.push(nimi(poistettavaHlo))}
      />
      {isNewItem(model) && (
        <div className="uusi-henkilo">
          <div>
            <span className="nimi">
              <Editor model={model} path="nimi" placeholder={t('nimi')} />
            </span>
            <span className="titteli">
              <Editor model={model} path="titteli" placeholder={t('titteli')} />
            </span>
          </div>
          <div>
            <Editor
              model={model}
              path="organisaatio"
              placeholder={t('organisaatio')}
            />
          </div>
        </div>
      )}
    </span>
  )
}
