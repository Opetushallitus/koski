import React from 'baret'
import * as R from 'ramda'
import Text from '../i18n/Text'
import {formatFinnishDate, parseISODate} from '../date/date'
import {pathToClassnames} from './KelaOsasuorituksetTable'
import {t} from '../i18n/i18n'


export const KeyValueTable = ({object, path}) => {
  return (
    <table>
      <tbody className={pathToClassnames(path) + ' properties key-value-table'}>
      {iterator(object, path).map(rowInTable)}
      </tbody>
    </table>
  )
}

const rowInTable = ({key, value, path}, index) => {
  const ReactComponentForDisplayingValue = spesificComponent(key, value) || componentForType[R.type(value)]

  return (
    <tr className={pathToClassnames(path)} key={index}>
      <td className='label'>
        {t(formatLabel(key))}
      </td>
      <td className='value'>
        <ReactComponentForDisplayingValue value={value} path={path}/>
      </td>
    </tr>
  )
}

export const NumberView = ({value}) => <span>{value == 0 ? '' : value}</span>
export const BooleanView = ({value}) => <Text name={value ? 'kyllä' : 'ei'}/>
export const DateView = ({value}) => <span>{ value && formatFinnishDate(parseISODate(value)) || ''}</span>

export const TextView = ({value}) => {
  return R.type(value) === 'Object' //assume its LocalizedString
    ? <span>{t(value)}</span>
    : <Text name={value}/>
}

const ArrayView = ({value}) => {
  return (
    <ul>
      {value.map((val, i) => {
        const ReactComponent = spesificComponent('key', val) || componentForType[R.type(val)]
        return (
          <li>
            <ReactComponent key={i} value={val}/>
          </li>
        )
      })}
    </ul>
  )
}

const ObjectView = ({value, path}) => {
  const hideKeys = ['alku', 'loppu', 'koodistoVersio', 'koodistoUri', 'lyhytNimi']
  const optionalAikajaksoInline = isAikajakso(value) && <AikajaksoInline value={value}/>
  return (
    <table>
      <tbody>
        {optionalAikajaksoInline}
        {iterator(R.omit(hideKeys, value), path).map(rowInTable)}
      </tbody>
    </table>
  )
}

const iterator = (obj, currentPath) => {
  return Object.entries(obj).map(([key, value]) => ({key, value, path: currentPath + '.' + key}))
}

const spesificComponent = (key, value) => {
  if (R.contains(key, ['oppilaitos', 'toimipiste', 'koulutustoimija'])) {
    return OrganisaationNimi
  }
  if (key === 'tunniste' || isKoodistoviite(value)) {
    return ShowNameFromKoodistoviite
  }
  if (isLocalizedString(value)) {
    return TextView
  }
  if (key === 'laajuus') {
    return Laajuus
  }
  if (key === 'osaamisala') {
    return OsaamisalaJaksotInline
  }
  return undefined
}

const componentForType = {
  'Object': ObjectView,
  'Array': ArrayView,
  'Boolean': BooleanView,
  'String': TextView,
  'Number': NumberView
}

const OrganisaationNimi = ({value}) => {
  const nimi = value && t(value.nimi || {}) || ''
  return <span>{nimi}</span>
}

const ShowNameFromKoodistoviite = ({value}) => {
  const nimi = t(value.nimi || {}) || ''
  return (
    <span>
      {nimi}
    </span>
  )
}

const Laajuus = ({value}) => (
  <>
    <span>{value.arvo}</span>
    {' '}
    <ShowNameFromKoodistoviite value={value.yksikkö}/>
  </>
)

const OsaamisalaJaksotInline = ({value}) => {
  const osaamisalaJaksot = value
  return (
    <ul>
      {osaamisalaJaksot.map((jakso, index) => (
        <li key={index}>
          <AikajaksoInline value={value}/>
          <ShowNameFromKoodistoviite value={jakso.osaamisala}/>
        </li>
      ))}
    </ul>
  )
}

const AikajaksoInline = ({value}) => {
  return (
    <tr>
      <td>
      <DateView value={value.alku}/>
      {value.alku && ' - '}
      <DateView value={value.loppu}/>
      </td>
    </tr>
  )
}

const isAikajakso = obj => obj.alku || obj.loppu

const isKoodistoviite = obj => Object.keys(obj).includes('koodiarvo')

const isLocalizedString = obj => !R.isEmpty(R.intersection(['fi', 'en', 'sv'], Object.keys(obj)))

const formatLabel = label => capitalizeFirstChar(splitToWords(label))
const splitToWords = str => str.split(/([A-ZÄÖÅ][a-zäöå]+)/g).map(R.toLower).join(' ').trim()
const capitalizeFirstChar = str => str.charAt(0).toUpperCase() + str.slice(1)
