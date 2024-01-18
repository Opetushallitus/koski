import bem from "bem-ts"
import React, { useCallback, useState } from "react"
import { nonNull, toggleItemExistence } from "../../utils/arrays"
import { CaretRightIcon } from "../icons/Icon"
import "./Accordion.less"

const b = bem("accordion")

export type AccordionProps = {
  accordionId: string
  items: Array<AccordionItem | null | undefined>
}

export type AccordionItem = {
  label: string
  render: () => React.ReactNode
}

export const Accordion = (props: AccordionProps) => {
  const [openItems, setOpenItems] = useState<number[]>([0])

  const onClickItem = useCallback(
    (index: number) => () => {
      setOpenItems(toggleItemExistence(openItems, index))
    },
    [openItems],
  )

  return (
    <section className={b()}>
      {props.items.filter(nonNull).map((item, index) => (
        <AccordionItem
          key={index}
          itemId={`${props.accordionId}-${index}`}
          label={item.label}
          onClick={onClickItem(index)}
          isOpen={openItems.includes(index)}
        >
          {item.render()}
        </AccordionItem>
      ))}
    </section>
  )
}

type AccordionItemProps = {
  itemId: string
  label: string
  isOpen: boolean
  onClick: () => void
  children: React.ReactNode
}

const AccordionItem = (props: AccordionItemProps) => {
  const triggerId = `${props.itemId}-trigger`
  const regionId = `${props.itemId}-region`

  return (
    <>
      <h4 className={b("label")}>
        <button
          id={triggerId}
          aria-expanded={props.isOpen ? "true" : "false"}
          aria-controls={regionId}
          className={b("trigger")}
          onClick={props.onClick}
        >
          <CaretRightIcon inline />
          {props.label}
        </button>
      </h4>

      <div
        id={regionId}
        role="region"
        aria-labelledby={triggerId}
        className={b("region", { open: props.isOpen })}
        aria-hidden={props.isOpen ? undefined : "true"}
      >
        {props.children}
      </div>
    </>
  )
}
