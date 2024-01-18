import bem from "bem-ts"
import React, { useEffect, useRef, useState } from "react"
import { joinClassNames } from "../../utils/classnames"
import { plainComponent } from "../../utils/plaincomponent"
import "./cards.less"

const b = bem("card")

export type CardProps = React.HTMLAttributes<HTMLDivElement>

const CardContainer = plainComponent("section", b())
const CardBorders = plainComponent("div", b("borders"))

export const Card = ({ children, ...props }: CardProps) => (
  <CardContainer {...props}>
    <CardBorders>{children}</CardBorders>
  </CardContainer>
)

export const BorderlessCard = plainComponent("section", b(["borderless"]))

export const CardHeader = plainComponent("header", b("header"))

export const CardBody = plainComponent("div", b("body"))

export type ConstrainedCardBodyProps = React.HTMLAttributes<HTMLDivElement> & {
  extraMargin?: number
}

/**
 * Versio CardBodysta, joka pyrkii pysymään korkeudeltaan kohtuullisen kokoisena
 */
export const ConstrainedCardBody = ({
  style,
  className,
  extraMargin,
  ...rest
}: ConstrainedCardBodyProps) => {
  const containerRef = useRef<HTMLDivElement>(null)
  const [maxHeight, setMaxHeight] = useState<number | undefined>(undefined)

  useEffect(() => {
    const updateMaxHeight = () =>
      setMaxHeight(calculateConstrainedBodyMaxHeight(containerRef, extraMargin))

    updateMaxHeight()

    window.addEventListener("resize", updateMaxHeight)
    return () => window.removeEventListener("resize", updateMaxHeight)
  }, [extraMargin])

  return (
    <div
      ref={containerRef}
      className={joinClassNames(b("body", { constrained: true }), className)}
      style={{ ...style, maxHeight }}
      {...rest}
    />
  )
}

const calculateConstrainedBodyMaxHeight = (
  containerRef: React.RefObject<HTMLDivElement>,
  extraMargin: number = 0,
) => {
  const defaultBaseMargin = 50
  const cardMargin = 30
  const minHeight = 500
  const viewHeight = document.documentElement.clientHeight

  // Vaihtoehto 1) venytä body lähes ruudun alareunaan asti
  let stretchToBottomHeight = 0
  if (containerRef.current) {
    const containerTop = containerRef.current.getBoundingClientRect().top
    stretchToBottomHeight = viewHeight - containerTop - extraMargin - cardMargin
  }

  // Vaihtoehto 2) venytä bodya niin että se ja kortin otsikko täyttävät ikkunan lähes kokonaan pystysuunnassa
  const fillVerticallyHeight =
    viewHeight - defaultBaseMargin - cardMargin * 2 - extraMargin

  return Math.max(
    minHeight,
    Math.min(stretchToBottomHeight, fillVerticallyHeight),
  )
}
