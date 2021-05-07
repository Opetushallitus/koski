import bem from "bem-ts"
import React, {
  MouseEvent,
  useCallback,
  useEffect,
  useRef,
  useState,
} from "react"
import { Caret, CaretDirection } from "../icons/Caret"
import { InfoIcon } from "../icons/Icon"
import "./InfoTooltip.less"

const b = bem("infotooltip")

export type InfoTooltipProps = {
  children: React.ReactNode
}

type TooltipAlign = "left" | "right"

export const InfoTooltip = (props: InfoTooltipProps) => {
  const [isOpen, setOpen] = useState(false)
  const [direction, setDirection] = useState<CaretDirection>("down")
  const [align, setAlign] = useState<TooltipAlign>("right")
  const iconRef = useRef<HTMLSpanElement>(null)

  const updateDirection = useCallback(() => {
    if (iconRef.current) {
      const viewportOffset = iconRef.current.getBoundingClientRect()
      setDirection(viewportOffset.top < 200 ? "up" : "down")
      setAlign(viewportOffset.left < window.innerWidth / 2 ? "right" : "left")
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
    window.addEventListener("scroll", updateDirection)
    window.addEventListener("click", hide)
    return () => {
      window.removeEventListener("scroll", updateDirection)
      window.removeEventListener("click", hide)
    }
  }, [updateDirection])

  return (
    <span className={b()} onClick={toggle} ref={iconRef}>
      <InfoIcon />
      {isOpen && (
        <InfoTooltipPopup direction={direction} align={align}>
          {props.children}
        </InfoTooltipPopup>
      )}
    </span>
  )
}

export type InfoTooltipPopupProps = {
  children: React.ReactNode
  direction: CaretDirection
  align: TooltipAlign
}

const InfoTooltipPopup = (props: InfoTooltipPopupProps) => (
  <div className={b("popup", [props.direction, props.align])} aria-hidden>
    <div className={b("icon")}>
      <InfoIcon />
    </div>
    <div className={b("content")}>{props.children}</div>
    <Caret width={20} direction={props.direction} />
  </div>
)
