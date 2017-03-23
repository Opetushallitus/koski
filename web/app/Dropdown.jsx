import React from 'react'
import BaconComponent from './BaconComponent'

export default BaconComponent({
  render() {
    const {open, selectionIndex} = this.state
    const {keyValue, displayValue, options, selected} = this.props
    return (
      <div id={this.props.id} className="dropdown" tabIndex="0" ref={el => this.dropdown = el} onBlur={this.handleOnBlur} onKeyDown={this.onKeyDown}>
        <div ref={(select => this.select = select)} className={selected ? 'select' : 'select no-selection'} onClick={this.toggleOpen}>{selected ? displayValue(selected): 'valitse'}<span className="toggle-open"/>
        </div>
        { open ?
          <ul className="options">
            {
              options.map((o,i) =>
                <li key={keyValue(o) || displayValue(o)} className={i == selectionIndex ? 'option selected' : 'option'} onClick={() => this.selectOption(o)} onMouseOver={() => this.handleMouseOver(o)}>{displayValue(o)}</li>
              )
            }
          </ul>
          : null
        }
      </div>
    )
  },
  handleOnBlur() {
    this.setState({open: false})
  },
  selectOption(option) {
    this.setState({selected: option, open: false, selectionIndex: 0}, () => this.props.onSelectionChanged(option))
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
    const dropdown = e.target.closest('.dropdown')
    const clickedInside = dropdown && dropdown.getAttribute('id') == this.props.id
    !clickedInside && this.setState({open: false})
  },
  handleMouseOver(o) {
    const {options} = this.props
    const index = options.findIndex(option => this.props.keyValue(option) == this.props.keyValue(o))
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
        this.selectOption(options[selectionIndex])
      }
    }
  }
})