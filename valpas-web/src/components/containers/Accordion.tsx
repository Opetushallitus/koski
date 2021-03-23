import bem from "bem-ts"
import React, { useState } from "react"
import { nonNull, toggleItemExistence } from "../../utils/arrays"
import { CaretDownIcon, CaretRightIcon } from "../icons/Icon"
import "./Accordion.less"

const b = bem("accordion")

export type AccordionProps = {
  items: Array<AccordionItem | null | undefined>
}

export type AccordionItem = {
  label: string
  render: () => React.ReactNode
}

export const Accordion = (props: AccordionProps) => {
  const [openItems, setOpenItems] = useState<number[]>([0])

  const onClickItem = (index: number) => () => {
    setOpenItems(toggleItemExistence(openItems, index))
  }

  return (
    <ul className={b()}>
      {props.items.filter(nonNull).map((item, index) => (
        <AccordionItem
          key={index}
          label={item.label}
          onClick={onClickItem(index)}
          open={openItems.includes(index)}
        >
          {item.render()}
        </AccordionItem>
      ))}
    </ul>
  )
}

type AccordionItemProps = React.HTMLAttributes<HTMLElement> & {
  label: string
  open: boolean
}

const AccordionItem = ({
  label,
  onClick,
  children,
  open,
  ...rest
}: AccordionItemProps) => (
  <li {...rest} className={b("item", { open })}>
    <h4 className={b("label")} onClick={onClick}>
      {open ? <CaretDownIcon inline /> : <CaretRightIcon inline />}
      {label}
    </h4>
    <div className={b("content")}>{children}</div>
  </li>
)
