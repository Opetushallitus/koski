import React from 'baret'
import {
  modelData,
  modelEmpty,
  modelItems,
  modelLookup,
  modelProperties
} from '../editor/EditorModel'
import { Editor } from '../editor/Editor'
import { PropertyTitle } from '../editor/PropertiesEditor'
import { formatISODate } from '../date/date'

export const LukukausiIlmoittautuminenEditor = ({ model }) => {
  const items = modelItems(modelLookup(model, 'ilmoittautumisjaksot'))
    .slice(0)
    .reverse()
  if (!items.length) return null
  const properties = modelProperties(items[0])
  return (
    <table className="tabular-array lukukausi-ilmoittautumiset">
      <thead>
        <tr>
          {properties.filter(shouldShowColumn(items)).map((p, i) => (
            <th key={i}>
              <PropertyTitle property={p} />
            </th>
          ))}
        </tr>
      </thead>
      <tbody>
        {items.map((item, i) => {
          return (
            <tr key={i} className={isActive(item) ? 'active' : ''}>
              {modelProperties(item)
                .filter(shouldShowColumn(items))
                .map((p, j) => {
                  return (
                    <td key={j}>
                      <Editor model={p.model} />
                    </td>
                  )
                })}
            </tr>
          )
        })}
      </tbody>
    </table>
  )
}

const isActive = (item) => {
  const loppu = modelData(item, 'loppu')
  const alku = modelData(item, 'alku')
  const nyt = formatISODate(new Date())
  return loppu ? loppu >= nyt : alku <= nyt
}
const shouldShowColumn = (items) => (property) =>
  !property.hidden && somePropertyNonEmpty(items, property)
const somePropertyNonEmpty = (items, property) =>
  items.some((item) => !modelEmpty(item, property.key))
