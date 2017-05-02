import React from 'react'
import Bacon from 'baconjs'
import R from 'ramda'
import BaconComponent from './BaconComponent'
import Http from './http'
import Highlight from 'react-highlighter'
import { showInternalError } from './location.js'
import { buildClassNames } from './classnames.js'

let findSingleResult = (shouldShowOrg = () => true, canSelectOrg = () => true) => (organisaatiot) => {
  let selectableOrgs = (org) => {
    let thisSelectable = canSelectOrg(org) ? [org] : []
    let selectableChildren = org.children.flatMap(selectableOrgs)
    return thisSelectable.concat(selectableChildren)
  }
  let selectables = organisaatiot.flatMap(selectableOrgs)
  return selectables.length == 1 && selectables[0]
}
export default BaconComponent({
  render() {
    let { organisaatiot = [], open, loading, searchString, singleResult } = this.state
    let { onSelectionChanged, selectedOrg, canSelectOrg = () => true, shouldShowOrg = () => true, noSelectionText = '', clearText = 'kaikki' } = this.props

    let selectOrg = (org) => { this.setState({open: false}); onSelectionChanged(org) }

    let renderTree = (orgs) => {
      let filteredOrgs = orgs.filter(shouldShowOrg)
      return filteredOrgs.map((org, i) =>
        <li key={i}>
          {
            canSelectOrg(org)
              ? <a className="nimi" onClick={ (e) => { selectOrg(org); e.preventDefault(); e.stopPropagation() }}><Highlight search={searchString}>{org.nimi.fi}</Highlight></a>
              : <span>{org.nimi.fi}</span>
          }
          <ul className="aliorganisaatiot">
            { renderTree(org.children) }
          </ul>
        </li>
      )
    }

    let selectionStr = selectedOrg.nimi // TODO: users should always supply the localized value here
      ? selectedOrg.nimi.fi
        ? selectedOrg.nimi.fi
        : selectedOrg.nimi
      : noSelectionText

    return (
      <div className={loading ? 'organisaatio loading' : 'organisaatio'} tabIndex="0" onKeyDown={this.onKeyDown} ref={root => this.root = root}>
        <div className={buildClassNames(['organisaatio-selection text-like-input', singleResult && 'disabled single-result'])} onClick={ () => !singleResult && this.setState({open:!open}) }>{ selectionStr }</div>
        { open &&
        <div className="organisaatio-popup">
          <input className="organisaatio-haku" type="text" placeholder="hae" ref="hakuboksi" defaultValue={this.state.searchString} onChange={e => {
            if (e.target.value.length >= 3 || e.target.value.length == 0) this.searchStringBus.push(e.target.value)
          }}/>
          {
            clearText && <button className="button kaikki" onClick={() => { this.searchStringBus.push(''); selectOrg(undefined)}}>{clearText}</button>
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
  },
  componentDidUpdate(prevProps, prevState) {
    if (this.state.open) {
      if (this.state.searchString === undefined) {
        this.searchStringBus.push('') // trigger initial AJAX load when opened for the first time
      }
      if (!prevState.open) {
        this.refs.hakuboksi.focus()
      }
    }
  },
  componentWillMount() {
    this.searchStringBus = Bacon.Bus()
    this.searchStringBus
      .onValue((searchString) => this.setState({searchString, loading: true}))
    let searchResult = this.searchStringBus.flatMapLatest((searchString) =>
      Http.get('/koski/api/organisaatio/hierarkia?query=' + searchString)
        .map((organisaatiot) => ({ organisaatiot, searchString }))
    ).doError(showInternalError)
     .takeUntil(this.unmountE)
    searchResult.onValue(({ organisaatiot }) => this.setState({ organisaatiot, loading: false }))
    if (this.props.preselectSingleOption) {
      this.searchStringBus.push('')
      searchResult.filter(r => r.searchString == '').take(1).map('.organisaatiot').map(findSingleResult(this.props.shouldShowOrg, this.props.canSelectOrg)).filter(R.identity).onValue( singleResult => {
        this.setState({singleResult})
        this.props.onSelectionChanged(singleResult)
      })
    }
  },
  getInitialState() {
    return { open: false }
  },
  componentDidMount() {
    window.addEventListener('click', this.handleClickOutside, false)
    window.addEventListener('focus', this.handleFocus, true)
  },
  componentWillUnmount() {
    window.removeEventListener('click', this.handleClickOutside, false)
    window.removeEventListener('focus', this.handleFocus, true)
  },
  handleFocus(e) {
    const focusInside = e.target == window ? false : !!this.root.contains(e.target)
    if(!focusInside) {
      this.setState({open: false})
    }
  },
  handleClickOutside(e) {
    !e.target.closest('.organisaatio') && this.setState({open: false})
  },
  onKeyDown(e) {
    let handler = this.keyHandlers[e.key]
    if(handler) {
      handler.call(this, e)
    }
  },
  keyHandlers: {
    ArrowDown() {
      if(!this.state.open) {
        this.setState({open: true})
      }
    },
    Escape() {
      this.setState({open: false})
    }
  }
})