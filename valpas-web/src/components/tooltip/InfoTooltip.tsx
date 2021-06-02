import bem from "bem-ts"
import React, {
  MouseEvent,
  useCallback,
  useEffect,
  useRef,
  useState,
} from "react"
import { createPortal } from "react-dom"
import { Caret, CaretDirection } from "../icons/Caret"
import { InfoIcon } from "../icons/Icon"
import "./InfoTooltip.less"

const b = bem("infotooltip")

export type InfoTooltipProps = {
  children: React.ReactNode
}

type TooltipAlign = "left" | "right"

type Position = {
  top: number
  left: number
}

export const InfoTooltip = (props: InfoTooltipProps) => {
  const [isOpen, setOpen] = useState(false)
  const [direction, setDirection] = useState<CaretDirection>("down")
  const [align, setAlign] = useState<TooltipAlign>("right")
  const [position, setPosition] = useState<Position>({ top: 0, left: 0 })
  const iconRef = useRef<HTMLSpanElement>(null)

  const updateDirection = useCallback(() => {
    if (iconRef.current) {
      const viewportOffset = iconRef.current.getBoundingClientRect()
      setDirection(viewportOffset.top < 200 ? "up" : "down")
      setAlign(viewportOffset.left < window.innerWidth / 2 ? "right" : "left")
      setPosition({
        left: viewportOffset.left + viewportOffset.width / 2,
        top: window.scrollY + viewportOffset.top + viewportOffset.height / 2,
      })
    }
  }, [iconRef])

  const toggle = useCallback(
    (event: MouseEvent) => {
      event.stopPropagation()
      const open = !isOpen
      setOpen(open)
      if (open) {
        updateDirection()
      }
    },
    [isOpen, setOpen, updateDirection]
  )

  useEffect(() => {
    const hide = () => setOpen(false)
    const parents = getParents(iconRef.current)

    parents.forEach((parent) =>
      parent.addEventListener("scroll", updateDirection)
    )
    window.addEventListener("click", hide)

    return () => {
      parents.forEach((parent) =>
        parent.removeEventListener("scroll", updateDirection)
      )
      window.removeEventListener("click", hide)
    }
  }, [updateDirection, iconRef])

  return (
    <span className={b()} onClick={toggle}>
      <span className={b("iconwrapper")} ref={iconRef}>
        <InfoIcon />
      </span>
      {isOpen &&
        createPortal(
          <InfoTooltipPopup
            direction={direction}
            align={align}
            position={position}
          >
            {props.children}
          </InfoTooltipPopup>,
          document.getElementById("app")!!
        )}
    </span>
  )
}

export type InfoTooltipPopupProps = {
  children: React.ReactNode
  direction: CaretDirection
  align: TooltipAlign
  position: Position
}

const InfoTooltipPopup = (props: InfoTooltipPopupProps) => (
  <div className={b("popupcontainer")} style={props.position}>
    <div className={b("popup", [props.direction, props.align])} aria-hidden>
      <div className={b("icon")}>
        <InfoIcon />
      </div>
      <div className={b("content")}>{props.children}</div>
      <Caret width={20} direction={props.direction} />
    </div>
  </div>
)

const getParents = (
  element: HTMLElement | null,
  acc: HTMLElement[] = []
): HTMLElement[] =>
  element ? getParents(element.parentElement, [...acc, element]) : acc
