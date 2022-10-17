import React from 'baret'
import Bacon from 'baconjs'
import BaconComponent from './BaconComponent'
import delays from '../util/delays'
import { t } from '../i18n/i18n'
import {
  parseBool,
  scrollElementBottomVisible,
  toObservable
} from '../util/util'

/*
    disabled: true/false
    placeholder: text
    selected: currently selected item
    resultCallback, resultBus or resultAtom
    fetchItems: String -> Property [Item]
    displayValue: Item -> String (if missing, items are expected to have a "nimi" field that's a localized text)
    createNewItem: query -> proto for new item
 */

export default class Autocomplete extends BaconComponent {
  constructor(props) {
    super(props)
    this.keyHandlers = {
      ArrowUp() {
        let { selectionIndex } = this.state
        selectionIndex = selectionIndex === 0 ? 0 : selectionIndex - 1
        this.setState({ selectionIndex })
      },
      ArrowDown() {
        let { selectionIndex, items } = this.state
        selectionIndex =
          selectionIndex === items.length - 1
            ? selectionIndex
            : selectionIndex + 1
        this.setState({ selectionIndex })
      },
      Enter(e) {
        e.preventDefault()
        const { selectionIndex, items } = this.state
        this.handleSelect(items[selectionIndex])
      },
      Escape() {
        this.setState({ query: undefined, items: [] })
      }
    }
    this.state = {
      query: undefined,
      items: [],
      selectionIndex: 0,
      inputBus: Bacon.Bus()
    }
  }

  render() {
    const {
      disabled,
      selected,
      placeholder,
      autoFocus,
      displayValue = (item) => t(item.nimi),
      createNewItem = () => null
    } = this.props
    const selectedP = toObservable(selected)
    const { items, query, selectionIndex } = this.state
    const createItemElement = (item, i) => (
      <li
        key={i}
        className={i === selectionIndex ? 'selected' : null}
        onClick={this.handleSelect.bind(this, item)}
      >
        {i >= items.length && <span className="plus">{''}</span>}
        {displayValue(item)}
      </li>
    )
    const newItem = createNewItem(query || '')
    const itemElems = items
      .concat(newItem ? [newItem] : [])
      .map(createItemElement)
    // TODO: Käytä Reactin reffejä
    const results = itemElems.length ? (
      <ul ref="results" className="results" data-test-id="autocomplete-results">
        {itemElems}
      </ul>
    ) : null
    // TODO: Käytä Reactin reffejä
    return (
      <div ref="autocomplete" className="autocomplete">
        <input
          type="text"
          className="autocomplete-input"
          placeholder={placeholder}
          onKeyDown={this.onKeyDown.bind(this)}
          onChange={this.handleInput.bind(this)}
          value={selectedP.map((s) => query || (s ? displayValue(s) : ''))}
          disabled={disabled}
          autoFocus={parseBool(autoFocus)}
        />
        {results}
      </div>
    )
  }

  handleInput(e) {
    const query = e.target.value
    this.setValue(undefined)
    this.state.inputBus.push(query)
    this.setState({ query })
  }

  handleSelect(selected) {
    this.setState({ query: undefined, items: [] })
    this.setValue(selected)
  }

  setValue(value) {
    if (this.props.resultBus) {
      this.props.resultBus.push(value)
    } else if (this.props.resultAtom) {
      this.props.resultAtom.set(value)
    } else if (this.props.resultCallback) {
      this.props.resultCallback(value)
    } else {
      throw new Error('resultBus/resultAtom/resultCallback missing')
    }
  }

  onKeyDown(e) {
    const handler = this.keyHandlers[e.key]
    if (handler) {
      handler.call(this, e)
    }
  }

  componentDidMount() {
    this.state.inputBus
      .throttle(delays().delay(200))
      .flatMapLatest((query) => this.props.fetchItems(query).mapError([]))
      .takeUntil(this.unmountE)
      .onValue((items) => {
        this.setState({ items, selectionIndex: 0 })
        Bacon.later(0).onValue(() => {
          scrollElementBottomVisible(this.refs.results)
        })
      })
  }
}
