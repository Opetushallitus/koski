import React, { useState } from 'react'
import { t } from '../../i18n/i18n'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { baseProps, BaseProps } from '../baseProps'

export type TabsProps<T> = BaseProps & {
  tabs: Tab<T>[]
  onSelect: (key: T) => void
}

export type Tab<T> = {
  key: T
  label: string | LocalizedString
}

export const Tabs = <T,>(props: TabsProps<T>) => {
  const [active, setActive] = useState(props.tabs[0]?.key)

  const select = (key: T) => {
    setActive(key)
    props.onSelect(key)
  }

  return (
    <nav {...baseProps(props, 'Tabs')}>
      <ul className="Tabs__list">
        {props.tabs.map((tab, i) => (
          <li
            key={i}
            {...baseProps(
              'Tabs__item',
              tab.key === active && 'Tabs__item-active'
            )}
          >
            <button
              tabIndex={0}
              className="Tabs__button"
              onClick={() => select(tab.key)}
            >
              {t(tab.label)}
            </button>
          </li>
        ))}
        <div className="Tabs__filler" />
      </ul>
    </nav>
  )
}
