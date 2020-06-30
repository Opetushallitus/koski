import React, {fromBacon} from 'baret'
import Atom from 'bacon.atom'
import Text from './i18n/Text'
import {Editor} from './editor/Editor'
import {EiSuorituksiaInfo} from './omattiedot/EiSuorituksiaInfo'
import {Header} from './omattiedot/header/Header'
import {hasOpintoja} from './oppija/oppija'
import {buildClassNames} from './components/classnames'
import SuoritusjakoList from './omattiedot/suoritusjako/SuoritusjakoList'

const OpintoniTab = ({ oppija }) => (
  <>
    <Header oppija={oppija}/>
    <Editor key={document.location.toString()} model={oppija}/>
  </>
)

const TabTypes = Object.freeze({
  OPINTONI: 'opintoni',
  JAKOLINKIT: 'luodut jakolinkit'
})

const SelectedTab = ({ selectedTabAtom, oppija }) => (
  fromBacon(selectedTabAtom.map(selectedTab => {
    switch (selectedTab) {
      case TabTypes.OPINTONI:
        return <OpintoniTab oppija={oppija}/>
      case TabTypes.JAKOLINKIT:
        return <SuoritusjakoList/>
    }
  }))
)
const TabLink = ({ text, type, selectedTabAtom }) => {
  return fromBacon(selectedTabAtom.map(selectedTab => {
    const classNames = buildClassNames([
      'omat-tiedot-tab-selector',
      type === selectedTab && 'active'
    ])
    const onClick = () => selectedTabAtom.set(type)
    return (
      <a className={classNames} onClick={onClick}>
        <Text name={text}/>
      </a>
    )
  }))
}

const TabsContainer = ({ oppija }) => {
  const selectedTabAtom = Atom(TabTypes.JAKOLINKIT)
  return (
    <>
      <div className='omat-tiedot-tab-selectors'>
        <TabLink
          text={oppija.context.huollettava ? 'Huollettavani opinnot' : 'Opintoni'}
          type={TabTypes.OPINTONI}
          selectedTabAtom={selectedTabAtom}
        />
        <TabLink
          text='Luodut jakolinkit'
          type={TabTypes.JAKOLINKIT}
          selectedTabAtom={selectedTabAtom}
        />
      </div>
      <SelectedTab selectedTabAtom={selectedTabAtom} oppija={oppija}/>
    </>
  )
}

export const OmatTiedotTabs = ({ oppija }) => (
  hasOpintoja(oppija)
    ? <TabsContainer oppija={oppija}/>
    : <EiSuorituksiaInfo oppija={oppija}/>
)
