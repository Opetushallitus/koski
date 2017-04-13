import React from 'react'
import BaconComponent from './BaconComponent'
import R from 'ramda'

export default BaconComponent({
  render() {
    let {open, selectionIndex, query} = this.state
    let {keyValue, displayValue, selected, onFilter, selectionText = 'valitse', newItem} = this.props
    let allOptions = this.optionsAndNewItem()
    return (
      <div id={this.props.id} className="dropdown" tabIndex={onFilter ? '' : '0'} onBlur={this.handleOnBlur} ref={el => this.dropdown = el} onKeyDown={this.onKeyDown}>
        {
          onFilter ?
            <div className="input-container" onClick={this.toggleOpen}>
              <input
                type="text"
                ref={(input => this.input = input)}
                onChange={this.handleInput}
                onBlur={this.handleInputBlur}
                value={query != undefined ? query : selected ? displayValue(selected) : selectionText}
                className={selected ? 'select' : 'select no-selection'}
              />
            </div> :
            <div ref={(select => this.select = select)} className={selected ? 'select' : 'select no-selection'}
                 onClick={this.toggleOpen}>{selected ? displayValue(selected) : selectionText}
            </div>
        }
        {(allOptions.length > 0) && <ul className={open ? 'options open' : 'options'}>
          {
            allOptions.map((o,i) => {
              let isNew = this.isNewItem(o, i)
              return <li key={keyValue(o) || displayValue(o)}
                  className={'option' + (i == selectionIndex ? ' selected' : '') + (isNew ? ' new-item' : '')}
                  onMouseDown={(e) => {this.selectOption(e, o)}} onMouseOver={() => this.handleMouseOver(o)}>
                { isNew ? <span><span className="plus"></span>{displayValue(newItem)}</span> : displayValue(o)}
              </li>
            })
          }
        </ul>}
      </div>
    )
  },
  isNewItem(o, i) {
    return i == this.props.options.length
  },
  optionsAndNewItem() {
    let {options, newItem} = this.props
    return options.concat(newItem ? [newItem] : [])
  },
  handleInput(e) {
    let {onFilter} = this.props
    let query = e.target.value
    this.setState({query: query, open: true}, onFilter(query))
  },
  handleInputBlur(e) {
    let {selected, displayValue} = this.props
    let options = this.optionsAndNewItem()
    let matchingOption = options.find(o => this.input.value && displayValue(o).toLowerCase() == this.input.value.toLowerCase())
    if (matchingOption && !R.equals(matchingOption,selected)) {
      this.selectOption(e, matchingOption)
    } else {
      this.setState({open: false, selectionIndex: 0, query: undefined})
    }
  },
  handleOnBlur() {
    this.setState({open: false})
  },
  selectOption(e, option) {
    e.preventDefault()
    e.stopPropagation()
    this.setState({selected: option, open: false, selectionIndex: 0, query: undefined}, () => this.props.onSelectionChanged(option))
  },
  toggleOpen() {
    if(this.input && !this.state.open) {
      this.input.select()
    }
    this.setState({open: !this.state.open})
  },
  getDefaultProps() {
    return {
      keyValue: option => option == (this.props || {}).newItem ? '_new' : option.key,
      displayValue: option => option.value
    }
  },
  handleMouseOver(o) {
    let options = this.optionsAndNewItem()
    let index = options.findIndex(option => this.props.keyValue(option) == this.props.keyValue(o))
    this.setState({selectionIndex: index})
  },
  getInitialState() {
    return {
      open: false,
      selected: undefined,
      selectionIndex: 0
    }
  },
  onKeyDown(e) {
    let handler = this.keyHandlers[e.key]
    if (handler) {
      handler.call(this, e)
    }
  },
  keyHandlers: {
    ArrowUp() {
      let {selectionIndex} = this.state
      selectionIndex = selectionIndex === 0 ? 0 : selectionIndex - 1
      this.setState({selectionIndex: selectionIndex})
    },
    ArrowDown(e) {
      e.preventDefault()
      e.stopPropagation()
      if (this.state.open) {
        let {selectionIndex} = this.state
        let options = this.optionsAndNewItem()
        selectionIndex = (selectionIndex === options.length - 1) ? selectionIndex : selectionIndex + 1
        this.setState({selectionIndex: selectionIndex})
      } else {
        this.setState({open: true})
      }
    },
    Escape() {
      this.setState({open: false})
    },
    Enter(e) {
      e.preventDefault()
      let {selectionIndex, open} = this.state
      if (open) {
        let options = this.optionsAndNewItem()
        this.selectOption(e, options[selectionIndex])
      }
    }
  }
})