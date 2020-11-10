import React from 'baret'
import * as R from 'ramda'

export const Tabs = ({
    options,
    selectedP,
    onSelect
}) => (
    <div className="tabs-container">
        {options.map(({ id, name }) => (
            <Tab
                key={id}
                isSelectedAtom={selectedP.map(R.equals(id))}
                onClick={() => onSelect(id)}
            >
                {name}
            </Tab>
        ))}
    </div>
)

const Tab = ({
    children,
    isSelectedAtom,
    onClick
}) => (
    <span
        className={isSelectedAtom.map(value => `tabs-item ${value ? 'tabs-item-selected' : ''}`)}
        onClick={onClick}
    >
        <span className="tabs-item-text">{children}</span>
        <span className="tabs-item-caret" />
    </span>
)
