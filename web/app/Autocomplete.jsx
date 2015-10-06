import React from "react"
import ReactDOM from "react-dom"
import Bacon from "baconjs"

export default React.createClass({

  render() {
    let {selected} = this.props
    let {items, query, selectionIndex} = this.state

    let itemElems = items ? items.map((item, i) => {
        return (
          <li key={i} className={i === selectionIndex ? "selected" : null} onClick={this.handleSelect.bind(this, item)}>{item.nimi}</li>
        )}
    ) : []

    return (
      <div ref="autocomplete" className="autocomplete">
        <input className="autocomplete-input" onKeyDown={this.onKeyDown} onInput={this.handleInput} value={query ? query : selected ? selected.nimi : ''}></input>
        <ul className="results">{itemElems}</ul>
      </div>
    )
  },

  handleInput(e) {
    let query = e.target.value
    this.props.resultBus.push(undefined)
    this.props.fetchItems(query).mapError([]).onValue((items) => this.setState({ items: items, query: query }))
  },

  handleSelect(selected) {
    this.setState({query: undefined, items: []})
    this.props.resultBus.push(selected)
  },

  onKeyDown(e) {
    let handler = this.keyHandlers[e.key]
    if(handler) {
      handler.call(this, e)
    }
  },

  getInitialState() {
    return {query: undefined, items: [], selectionIndex: 0}
  },

  keyHandlers: {
    ArrowUp: function() {
      let {selectionIndex} = this.state
      selectionIndex = selectionIndex === 0 ? 0 : selectionIndex - 1
      this.setState({selectionIndex: selectionIndex})
    },
    ArrowDown: function() {
      let {selectionIndex, items} = this.state
      selectionIndex = selectionIndex === items.length - 1 ? selectionIndex : selectionIndex + 1
      this.setState({selectionIndex: selectionIndex})
    },
    Enter: function(e) {
      e.preventDefault()
      let {selectionIndex, items} = this.state
      this.handleSelect(items[selectionIndex])
    },
    Escape: function() {
      this.setState({query: undefined, items: []})
    }
  }
})
