import React from 'react'
import BaconComponent from './BaconComponent'
import R from 'ramda'

export default BaconComponent({
  render() {
    let {open, selectionIndex, query} = this.state
    let {options, keyValue, displayValue, selected, onFilter} = this.props

    return (
      <div id={this.props.id} className="dropdown" tabIndex={onFilter ? '' : '0'} ref={el => this.dropdown = el} onBlur={this.handleOnBlur} onKeyDown={this.onKeyDown}>
        {
          onFilter ?
            <div className="input-container" onClick={this.toggleOpen}>
              <input
                type="text"
                ref={(input => this.input = input)}
                onChange={this.handleInput}
                onFocus={this.handleInputFocus}
                onBlur={this.handleInputBlur}
                value={query != undefined ? query : selected ? displayValue(selected) : 'valitse'}
                className={selected ? 'select' : 'select no-selection'}
              />
            </div> :
            <div ref={(select => this.select = select)} className={selected ? 'select' : 'select no-selection'}
                 onClick={this.toggleOpen}>{selected ? displayValue(selected) : 'valitse'}
            </div>
        }
        {options.length > 0 && <ul className={open ? 'options open' : 'options'}>
          {
            options.map((o,i) =>
              <li key={keyValue(o) || displayValue(o)} className={i == selectionIndex ? 'option selected' : 'option'} onClick={(e) => this.selectOption(e, o)} onMouseOver={() => this.handleMouseOver(o)}>{displayValue(o)}</li>
            )
          }
        </ul>}
      </div>
    )
  },
  handleInput(e) {
    let {onFilter} = this.props
    let query = e.target.value
    this.setState({query: query, open: true}, onFilter(query))
  },
  handleInputBlur(e) {
    let {selected, displayValue, options} = this.props
    let matchingOption = options.find(o => this.input.value && displayValue(o).toLowerCase() == this.input.value.toLowerCase())
    if (matchingOption && !R.equals(matchingOption,selected)) {
      this.selectOption(e, matchingOption)
    } else {
      this.setState({open: false, selectionIndex: 0, query: undefined})
    }
  },
  handleInputFocus() {
    this.input.select()
  },
  handleOnBlur(e) {
    // ie fires onBlur event so we have to check where it came from before closing the dropdown
    //e.target != this.select && e.target != this.input && this.setState({open: false})
    e.target != this.select && this.setState({open: false})
  },
  selectOption(e, option) {
    e.preventDefault()
    e.stopPropagation()
    this.setState({selected: option, open: false, selectionIndex: 0, query: undefined}, () => this.props.onSelectionChanged(option))
  },
  toggleOpen() {
    this.setState({open: !this.state.open})
  },
  componentWillMount() {
    this.propsE.merge(this.unmountE).onValue(() => {
      window.removeEventListener('click', this.handleClickOutside, false)
    })
    this.propsE.onValue(() => {
      window.addEventListener('click', this.handleClickOutside, false)
    })
  },
  getDefaultProps() {
    return {
      keyValue: option => option.key,
      displayValue: option => option.value
    }
  },
  handleClickOutside(e) {
    let dropdown = e.target.closest('.dropdown')
    let clickedInside = dropdown && dropdown == this.dropdown
    !clickedInside && this.setState({open: false})
  },
  handleMouseOver(o) {
    let {options} = this.props
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
        let {options} = this.props
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
        let {options} = this.props
        this.selectOption(e, options[selectionIndex])
      }
    }
  }
})