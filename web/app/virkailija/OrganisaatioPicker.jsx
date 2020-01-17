import React from 'react'
import Bacon from 'baconjs'
import * as R from 'ramda'
import BaconComponent from '../components/BaconComponent'
import Http from '../util/http'
import Highlight from 'react-highlighter'
import {buildClassNames} from '../components/classnames.js'
import {t} from '../i18n/i18n'
import Text from '../i18n/Text'
import {flatMapArray, parseBool, toObservable} from '../util/util'
import {parseLocation} from '../util/location'
import delays from '../util/delays'

let findSingleResult = (canSelectOrg = () => true) => (organisaatiot) => {
  let selectableOrgs = (org) => {
    let thisSelectable = canSelectOrg(org) ? [org] : []
    let selectableChildren = flatMapArray(org.children, selectableOrgs)
    return thisSelectable.concat(selectableChildren)
  }
  let selectables = flatMapArray(organisaatiot, selectableOrgs)
  return selectables.length == 1 && selectables[0]
}
export default class OrganisaatioPicker extends BaconComponent {
  constructor(props) {
    super(props)
    this.keyHandlers = {
      ArrowDown() {
        if(!this.state.open) {
          this.setState({open: true})
        }
      },
      Escape() {
        this.setState({open: false})
      }
    }
    this.state = { open: false }
    this.handleClickOutside = this.handleClickOutside.bind(this)
    this.handleFocus = this.handleFocus.bind(this)
  }
  render() {
    let { organisaatiot = [], open, loading, searchString, singleResult } = this.state
    let { onSelectionChanged, selectedOrg, canSelectOrg = () => true, shouldShowOrg = () => true, noSelectionText = '', clearText = t('kaikki') } = this.props

    let selectOrg = (org) => { this.setState({open: false}); onSelectionChanged(org) }
    let orgName = org => <span><Highlight search={searchString}>{t(org.nimi)}</Highlight> {org.aktiivinen ? null : <span>{' ('}<Text name="lakkautettu"/>{')'}</span>}</span>

    let renderTree = (orgs) => {
      let filteredOrgs = orgs.filter(shouldShowOrg)
      return filteredOrgs.map((org, i) =>
        (<li key={i}>
          {
            canSelectOrg(org)
              ? <a className="nimi" onClick={ (e) => { selectOrg(org); e.preventDefault(); e.stopPropagation() }}>{orgName(org)}</a>
              : <span>{orgName(org)}</span>
          }
          <ul className="aliorganisaatiot">
            { renderTree(org.children) }
          </ul>
        </li>)
      )
    }

    let selectionStr = selectedOrg.nimi
      ? typeof selectedOrg.nimi == 'object'
        ? t(selectedOrg.nimi)
        : selectedOrg.nimi
      : noSelectionText

    return (
      <div className={loading ? 'organisaatio loading' : 'organisaatio'} tabIndex="0" onKeyDown={this.onKeyDown.bind(this)} ref={root => this.root = root}>
        <div className={buildClassNames(['organisaatio-selection text-like-input', singleResult && 'disabled single-result'])} onClick={ () => !singleResult && this.setState({open:!open}) }>{ selectionStr }</div>
        { open &&
        <div className="organisaatio-popup">
          <input className="organisaatio-haku" type="text" placeholder={t('hae')} ref="hakuboksi" defaultValue={this.state.searchString} onChange={e => {
            if (e.target.value.length >= 3 || e.target.value.length == 0) this.inputBus.push(e.target.value)
          }}/>
          {
            clearText && <button className="koski-button kaikki" onClick={() => { this.searchStringBus.push(''); selectOrg(undefined)}}>{clearText}</button>
          }
          {
            organisaatiot.length > 0 && <div className="scroll-container">
              <ul className="organisaatiot">
                { renderTree(organisaatiot) }
              </ul>
            </div>
          }
        </div>
        }
      </div>
    )
  }
  componentDidUpdate(prevProps, prevState) {
    if (this.state.open) {
      if (this.state.searchString === undefined) {
        this.searchStringBus.push('') // trigger initial AJAX load when opened for the first time
      }
      if (!prevState.open) {
        this.refs.hakuboksi.focus()
      }
    }
  }
  componentWillMount() {
    super.componentWillMount()
    const showAll = parseBool(this.props.showAll)
    const orgTypesToShowP = toObservable(this.props.orgTypesToShow)
    this.inputBus = Bacon.Bus()
    this.searchStringBus = Bacon.Bus()
    this.searchStringBus.plug(this.inputBus.debounce(delays().delay(200)))
    this.searchStringBus
      .onValue((searchString) => this.setState({searchString, loading: true}))

    let searchResult = this.searchStringBus.flatMap(searchString => orgTypesToShowP.map(orgTypesToShow => ({searchString, orgTypesToShow}))).flatMapLatest(({searchString, orgTypesToShow}) =>
        Http.get(parseLocation('/koski/api/organisaatio/hierarkia').addQueryParams({ query: searchString, all: showAll, orgTypesToShow }))
          .map((organisaatiot) => ({ organisaatiot, searchString }))
    ).takeUntil(this.unmountE)
    searchResult.onValue(({ organisaatiot }) => this.setState({ organisaatiot, loading: false }))
    if (this.props.preselectSingleOption) {
      this.searchStringBus.push('')
      searchResult.filter(r => r.searchString == '').take(1).map('.organisaatiot').map(findSingleResult(this.props.canSelectOrg)).filter(R.identity).onValue( singleResult => {
        this.setState({singleResult})
        this.props.onSelectionChanged(singleResult)
      })
    }
  }
  componentDidMount() {
    window.addEventListener('click', this.handleClickOutside, false)
    window.addEventListener('focus', this.handleFocus, true)
  }
  componentWillUnmount() {
    super.componentWillUnmount()
    window.removeEventListener('click', this.handleClickOutside, false)
    window.removeEventListener('focus', this.handleFocus, true)
  }
  handleFocus(e) {
    const focusInside = e.target == window ? false : !!this.root.contains(e.target)
    if(!focusInside) {
      this.setState({open: false})
    }
  }
  handleClickOutside(e) {
    !e.target.closest('.organisaatio') && this.setState({open: false})
  }
  onKeyDown(e) {
    let handler = this.keyHandlers[e.key]
    if(handler) {
      handler.call(this, e)
    }
  }
}
