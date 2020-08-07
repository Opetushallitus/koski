import React from 'baret'
import * as R from 'ramda'
import Atom from 'bacon.atom'
import {BooleanView, DateView, KeyValueTable} from './KeyValueTable'
import {t} from '../i18n/i18n'

export const KelaOsasuorituksetTable = ({osasuoritukset, path, nested}) => {
  const currentPath = path + '.osasuoritukset'
  return (
    <table className={pathToClassnames(currentPath) + (nested ? ' nested' : '')}>
      <thead></thead>
      <tbody>
      <tr>
        <th className='osasuoritukset-title'>{'Osasuoritukset'}</th>
        <th className='arviointi'>{t('Arviointipäivä')}</th>
        <th className='arviointi'>{t('Hyväksytty')}</th>
      </tr>
      </tbody>
    {osasuoritukset.map((osasuoritus, index) => (
      <ExpandableOsasuoritus key={index}
                             osasuoritus={osasuoritus}
                             path={currentPath}
      />
    ))}
    </table>
  )
}

const ExpandableOsasuoritus = ({osasuoritus, path}) => {
  const expandedAtom = Atom(false)
  const mahdollinenArviointi = R.last(osasuoritus.arviointi || []) || {}
  const properties = R.omit(['osasuoritukset', 'arviointi', 'koulutusmoduuli', 'tyyppi'], osasuoritus)
  const isExpandable = !R.isEmpty(properties)

  return (
    <tbody>
    {expandedAtom.map(expanded => (<>
    <tr className={pathToClassnames(path) + ' title' + (isExpandable ? ' pointer' : '')}
        onClick={() => expandedAtom.set(!expandedAtom.get())}>
      <td>
        <span className='expand-button'>{isExpandable ? (expanded ? '' : '') : ''}</span>
        {' '}
        <span className='suorituksen-nimi'>{suorituksenNimi(osasuoritus.koulutusmoduuli)}</span>
      </td>
      <td className='arviointi'>
        <DateView value={mahdollinenArviointi.päivä} />
      </td>
      <td className='arviointi'>
        <BooleanView value={mahdollinenArviointi.hyväksytty} />
      </td>
    </tr>
      { (expanded && isExpandable) && (
        <tr>
          <td className='expanded'>
            <KeyValueTable object={properties} path={path}/>
            {
              osasuoritus.osasuoritukset && (
                <KelaOsasuorituksetTable osasuoritukset={osasuoritus.osasuoritukset} path={path} nested={true}/>)
            }
          </td>
        </tr>
      )}
      </>
    ))}
    </tbody>
  )
}

const suorituksenNimi = koulutusmoduuli => {
  const koodiarvo = t(koulutusmoduuli.tunniste.nimi)
  const kieli = t(koulutusmoduuli.nimi || {})
  const oppimaara = t(koulutusmoduuli.oppimäärä || {})

  return [koodiarvo, kieli, oppimaara].filter(R.identity).join(', ')
}

export const pathToClassnames = path => path.split('.').join(' ')
