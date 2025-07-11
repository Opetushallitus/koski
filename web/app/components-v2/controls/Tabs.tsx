import React, { useMemo, useState } from 'react'
import { t } from '../../i18n/i18n'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { common, CommonProps, cx } from '../CommonProps'
import { TestIdLayer, useTestId } from '../../appstate/useTestId'

export type TabsProps<T> = CommonProps<{
  tabs: Tab<T>[]
  active: T
  onSelect: (key: T) => void
}>

export type Tab<T> = {
  key: T
  label: string | LocalizedString
  display?: React.ReactNode
  testId?: string
}

export const Tabs = <T,>(props: TabsProps<T>) => (
  <nav {...common(props, ['Tabs'])}>
    <ul className="Tabs__list">
      {props.tabs.map((tab, i) => (
        <TestIdLayer key={i} id={i}>
          <li
            className={cx(
              'Tabs__item',
              tab.key === props.active && 'Tabs__item-active'
            )}
          >
            <TabButton onClick={() => props.onSelect(tab.key)}>
              {tab.display || t(tab.label)}
            </TabButton>
          </li>
        </TestIdLayer>
      ))}
      <li className="Tabs__filler" />
    </ul>
  </nav>
)
const TabButton: React.FC<{
  onClick: () => void
  children: React.ReactNode
}> = (props) => (
  <button
    tabIndex={0}
    className="Tabs__button"
    onClick={props.onClick}
    data-testid={useTestId('tab')}
  >
    {props.children}
  </button>
)
