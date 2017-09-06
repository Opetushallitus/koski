package fi.oph.koski.perftest

case class Operation(method: String = "GET",
                     uri: String,
                     uriPattern: Option[String] = None,
                     queryParams: Iterable[(String, String)] = Seq.empty,
                     headers: Iterable[(String, String)] = Seq.empty,
                     body: Array[Byte] = null,
                     gzip: Boolean = false,
                     responseCodes: List[Int] = List(200)
)
