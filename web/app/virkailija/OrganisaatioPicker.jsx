import React from 'react'
import Bacon from 'baconjs'
import * as R from 'ramda'
import BaconComponent from '../components/BaconComponent'
import Http from '../util/http'
import Highlight from 'react-highlighter'
import { buildClassNames } from '../components/classnames.js'
import { t } from '../i18n/i18n'
import Text from '../i18n/Text'
import { flatMapArray, parseBool, toObservable } from '../util/util'
import { parseLocation } from '../util/location'
import delays from '../util/delays'

const findSingleResult =
  (canSelectOrg = () => true) =>
  (organisaatiot) => {
    const selectableOrgs = (org) => {
      const thisSelectable = canSelectOrg(org) ? [org] : []
      const selectableChildren = flatMapArray(org.children, selectableOrgs)
      return thisSelectable.concat(selectableChildren)
    }
    const selectables = flatMapArray(organisaatiot, selectableOrgs)
    return selectables.length === 1 && selectables[0]
  }
export default class OrganisaatioPicker extends BaconComponent {
  constructor(props) {
    super(props)
    this.keyHandlers = {
      ArrowDown() {
        if (!this.state.open) {
          this.setState({ open: true })
        }
      },
      Escape() {
        this.setState({ open: false })
      }
    }
    this.state = { open: false }
    this.handleClickOutside = this.handleClickOutside.bind(this)
    this.handleFocus = this.handleFocus.bind(this)
  }

  render() {
    const {
      organisaatiot = [],
      open,
      loading,
      searchString,
      singleResult
    } = this.state
    const {
      onSelectionChanged,
      selectedOrg,
      canSelectOrg = () => true,
      shouldShowOrg = () => true,
      shouldShowChildren = () => true,
      noSelectionText = '',
      clearText = t('kaikki')
    } = this.props

    const selectOrg = (org) => {
      this.setState({ open: false })
      onSelectionChanged(org)
    }
    const orgName = (org) => (
      <span>
        <Highlight search={searchString}>{t(org.nimi)}</Highlight>{' '}
        {org.aktiivinen ? null : (
          <span>
            {' ('}
            <Text name="lakkautettu" />
            {')'}
          </span>
        )}
      </span>
    )

    const renderTree = (orgs) => {
      const filteredOrgs = orgs.filter(shouldShowOrg)
      return filteredOrgs.map((org, i) => (
        <li key={i}>
          {canSelectOrg(org) ? (
            <a
              className="nimi"
              onClick={(e) => {
                selectOrg(org)
                e.preventDefault()
                e.stopPropagation()
              }}
              data-testid={`organisaatio-list-item-${t(org.nimi)}`}
            >
              {orgName(org)}
            </a>
          ) : (
            <span>{orgName(org)}</span>
          )}
          {shouldShowChildren(org) ? (
            <ul className="aliorganisaatiot">{renderTree(org.children)}</ul>
          ) : null}
        </li>
      ))
    }

    const selectionStr = selectedOrg.nimi
      ? typeof selectedOrg.nimi === 'object'
        ? t(selectedOrg.nimi)
        : selectedOrg.nimi
      : noSelectionText

    return (
      <div
        className={loading ? 'organisaatio loading' : 'organisaatio'}
        tabIndex="0"
        onKeyDown={this.onKeyDown.bind(this)}
        ref={(root) => (this.root = root)}
        data-testid={'organisaatio-picker'}
      >
        <div
          className={buildClassNames([
            'organisaatio-selection text-like-input',
            singleResult && 'disabled single-result'
          ])}
          onClick={() => !singleResult && this.setState({ open: !open })}
          data-testid={'organisaatio-text-input'}
        >
          {selectionStr}
        </div>
        {open && (
          <div className="organisaatio-popup">
            <input
              className="organisaatio-haku"
              type="text"
              placeholder={t('hae')}
              ref="hakuboksi"
              defaultValue={this.state.searchString}
              onChange={(e) => {
                if (e.target.value.length >= 3 || e.target.value.length === 0)
                  this.inputBus.push(e.target.value)
              }}
              data-testid="organisaatio-haku-input"
            />
            {clearText && (
              <button
                className="koski-button kaikki"
                onClick={() => {
                  this.searchStringBus.push('')
                  selectOrg(undefined)
                }}
              >
                {clearText}
              </button>
            )}
            {organisaatiot.length > 0 && (
              <div className="scroll-container">
                <ul className="organisaatiot" data-testid={'organisaatio-list'}>
                  {renderTree(organisaatiot)}
                </ul>
              </div>
            )}
          </div>
        )}
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

  UNSAFE_componentWillMount() {
    super.UNSAFE_componentWillMount()
    const showAllP = toObservable(this.props.showAll)
    const orgTypesToShowP = toObservable(this.props.orgTypesToShow)
    this.inputBus = Bacon.Bus()
    this.searchStringBus = Bacon.Bus()
    this.searchStringBus.plug(this.inputBus.debounce(delays().delay(200)))
    this.searchStringBus.onValue((searchString) =>
      this.setState({ searchString, loading: true })
    )

    const params = Bacon.combineWith(
      showAllP,
      orgTypesToShowP,
      (showAll, orgTypesToShow) => ({ showAll, orgTypesToShow })
    )

    const searchResult = this.searchStringBus
      .flatMap((searchString) =>
        params.map(({ showAll, orgTypesToShow }) => ({
          searchString,
          orgTypesToShow,
          showAll: parseBool(showAll, false)
        }))
      )
      .flatMapLatest(({ searchString, orgTypesToShow, showAll }) =>
        Http.get(
          parseLocation('/koski/api/organisaatio/hierarkia').addQueryParams({
            query: searchString,
            all: showAll,
            orgTypesToShow: showAll ? null : orgTypesToShow
          })
        ).map((organisaatiot) => ({ organisaatiot, searchString }))
      )
      .takeUntil(this.unmountE)
    searchResult.onValue(({ organisaatiot }) =>
      this.setState({ organisaatiot, loading: false })
    )
    if (this.props.preselectSingleOption) {
      this.searchStringBus.push('')
      searchResult
        .filter((r) => r.searchString === '')
        .take(1)
        .map('.organisaatiot')
        .map(findSingleResult(this.props.canSelectOrg))
        .filter(R.identity)
        .onValue((singleResult) => {
          this.setState({ singleResult })
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
    // TODO: Testaa, toimiiko oikein eqeqeq-säännön kanssa
    const focusInside =
      e.target === window ? false : !!this.root.contains(e.target)
    if (!focusInside) {
      this.setState({ open: false })
    }
  }

  handleClickOutside(e) {
    !e.target.closest('.organisaatio') && this.setState({ open: false })
  }

  onKeyDown(e) {
    const handler = this.keyHandlers[e.key]
    if (handler) {
      handler.call(this, e)
    }
  }
}
