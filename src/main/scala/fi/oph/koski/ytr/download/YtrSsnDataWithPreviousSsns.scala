package fi.oph.koski.ytr.download

import fi.oph.koski.ytr.YtrSsnWithPreviousSsns

case class YtrSsnDataWithPreviousSsns(
  ssns: Option[List[YtrSsnWithPreviousSsns]]
)
