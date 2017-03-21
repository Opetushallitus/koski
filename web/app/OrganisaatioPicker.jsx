import React from 'react'
import Bacon from 'baconjs'
import BaconComponent from './BaconComponent'
import Http from './http'
import Highlight from 'react-highlighter'
import { showInternalError } from './location.js'

export default BaconComponent({
  render() {
    let { organisaatiot = [], open, loading, searchString } = this.state
    let { onSelectionChanged, selectedOrg, renderOrg, filterOrgs, noSelectionText = '', clearText = 'kaikki' } = this.props

    let selectOrg = (org) => { this.setState({open: false}); onSelectionChanged(org) }

    let link = org => <a className="nimi" onClick={ (e) => { selectOrg(org); e.preventDefault(); e.stopPropagation() }}><Highlight search={searchString}>{org.nimi.fi}</Highlight></a>

    let renderTree = (orgs) => {
      let filteredOrgs = filterOrgs ? orgs.filter(filterOrgs) : orgs
      return filteredOrgs.map((org, i) =>
        <li key={i}>
          {
            renderOrg ? renderOrg(org, link) : link(org)
          }
          <ul className="aliorganisaatiot">
            { renderTree(org.children) }
          </ul>
        </li>
      )
    }

    return (
      <div className="organisaatio" tabIndex="0" onKeyDown={this.onKeyDown} ref={root => this.root = root}>
        <div className="organisaatio-selection text-like-input" onClick={ () => this.setState({open:!open}) }>{ selectedOrg.nimi ? selectedOrg.nimi : noSelectionText}</div>
        { open &&
        <div className="organisaatio-popup">
          <input type="text" placeholder="hae" ref="hakuboksi" defaultValue={this.state.searchString} onChange={e => {
            if (e.target.value.length >= 3 || e.target.value.length == 0) this.searchStringBus.push(e.target.value)
          }}/>
          <button className="button kaikki" onClick={() => { this.searchStringBus.push(''); selectOrg(undefined)}}>{clearText}</button>
          <div className="scroll-container">
            <ul className={loading ? 'organisaatiot loading' : 'organisaatiot'}>
              { renderTree(organisaatiot) }
            </ul>
          </div>
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
    this.searchStringBus.flatMapLatest((searchString) =>
      Http.get('/koski/api/organisaatio/hierarkia?query=' + searchString))
        .doError(showInternalError)
        .map((organisaatiot) => ({ organisaatiot, loading: false }))
        .takeUntil(this.unmountE)
        .onValue((result) => this.setState(result))
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