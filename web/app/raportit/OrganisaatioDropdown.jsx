import React from 'baret'
import Atom from 'bacon.atom'
import {t} from '../i18n/i18n'
import Bacon from 'baconjs'
import {buildClassNames} from '../components/classnames'

const filterOrgTree = (query, orgs) => {
    return orgs
        .map(filterOrg(query))
        .filter(o => o.match)
}

const filterOrg = query => org => {
    const matches = t(org.nimi).toLowerCase().includes(query)
    const filteredChildren = filterOrgTree(query, org.children)

    return {
        ...org,
        children: matches
            ? org.children
            : filteredChildren,
        match: filteredChildren.length > 0 || matches
    }
}

const flattenOrgTree = (orgs) => orgs.flatMap(org => [org, ...flattenOrgTree(org.children)])

export const OrganisaatioDropdown = ({
    organisaatiotP,
    selectedP,
    onSelect
}) => {
    const openAtom = Atom(false)
    const filterAtom = Atom(null)
    const keySelectE = new Bacon.Bus()

    const displayP = Bacon.combineWith(openAtom, filterAtom, selectedP, (open, filter, selected) => (
        (open ? filter : (selected && t(selected.nimi))) || ''
    ))

    const filteredOrgsP = Bacon.combineWith(filterAtom, organisaatiotP, (filter, orgs) => {
        const query = filter && filter.toLowerCase()
        return filter
            ? filterOrgTree(query, orgs)
            : orgs
    })

    Bacon
        .combineAsArray(filteredOrgsP, selectedP)
        .sampledBy(keySelectE, (arr, keySelect) => ([...arr, keySelect]))
        .forEach(([filteredOrgs, selected, keySelect]) => {
            const flatOrgList = flattenOrgTree(filteredOrgs)
            const newIndex = flatOrgList.findIndex(org => org.oid === selected.oid) + keySelect
            if (newIndex >= 0 && newIndex < flatOrgList.length) {
                onSelect(flatOrgList[newIndex])
            }
        })

    const activate = () => {
        openAtom.set(true)
        filterAtom.set(null)
    }

    const deactivate = () => {
        openAtom.set(false)
    }

    const select = org => {
        openAtom.set(false)
        onSelect(org)
    }

    const updateFilter = filter => {
        filterAtom.set(filter.length > 0 ? filter : null)
    }

    const handleKey = event => {
        switch (event.which) {
            case 38: return keySelectE.push(-1)
            case 40: return keySelectE.push(1)
            case 13: return deactivate()
            default: return true
        }
    }

    return (
        <div
            className="organisaatio-dropdown"
            onFocus={activate}
            onBlur={deactivate}
        >
            <Input
                display={displayP}
                value={selectedP}
                onKeyDown={handleKey}
                onChange={updateFilter}
                onClick={activate}
            />
            {Bacon.combineWith(openAtom, filteredOrgsP, selectedP, (isOpen, orgs, selected) => (
                <Options
                    organisaatiot={orgs}
                    selected={selected}
                    onSelect={select}
                    isOpen={isOpen}
                />
            ))}
        </div>
    )
}

OrganisaatioDropdown.displayName = 'OrganisaatioDropdown'

const Input = ({ display, onChange, ...rest }) => (
    <div className="input-container">
        <input
            {...rest}
            type="text"
            placeholder="Valitse..."
            className="select"
            value={display}
            onChange={event => onChange(event.target.value)}
        />
    </div>
)

Input.displayName = 'Input'

const Options = ({ organisaatiot, selected, onSelect, isOpen, isChild }) => (
    <ul
        className={
            isChild
            ? 'child-options'
            : isOpen
            ? 'options open'
            : 'options'
        }
    >
        {organisaatiot.map(org => {
            const clickHandler = org.selectable
                ? () => onSelect(org)
                : null

            return (
                <li
                    key={org.oid}
                    className="option"
                >
                    <span
                        className={buildClassNames([
                            'value',
                            selected && org.oid === selected.oid ? 'selected' : null,
                            org.selectable ? null : 'disabled'
                        ])}
                        onMouseDown={clickHandler}
                        onTouchStart={clickHandler}
                        onClick={clickHandler}
                    >
                        {t(org.nimi)}
                    </span>
                    {org.children.length === 0 ? null : (
                        <Options
                            organisaatiot={org.children}
                            selected={selected}
                            onSelect={onSelect}
                            isChild
                        />
                    )}
                </li>
            )
        })}
    </ul>
)

Options.displayName = 'Options'
