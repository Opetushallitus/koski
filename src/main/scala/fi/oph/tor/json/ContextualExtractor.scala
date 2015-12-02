package fi.oph.tor.json

import org.json4s.{Formats, JValue, MappingException}

object ContextualExtractor {
  case class ExtractionState(val context: Any, var errors: List[Any])

  private val tl = new ThreadLocal[ExtractionState]

  /**
   *  Adds supports for "extraction context" to json4s data extraction (as in json4s ExtractableJsonAstNode).
   *
   *  The context object is provided as a parameter to this method and is available during extraction
   *  by calling the getContext method. Type parameter C is used for extraction context.
   *
   *  Additionally adds support for "extraction errors" (type of which is defined by the type parameter E). During
   *  extraction, the custom (de)serializers may call the `extractionError` method.
   *
   *  This method returns either a successfully extracted object fo type T or a list of errors that were reported
   *  during extraction.
   */
  def extract[T, C, E](json: JValue, context: C)(implicit mf: Manifest[T], formats: Formats): Either[List[E], T] = {
    val state: ExtractionState = new ExtractionState(context, Nil)
    tl.set(state)

    try {
      val extracted: T = json.extract[T]
      state.errors match {
        case Nil => Right(extracted)
        case errors => Left(errors.asInstanceOf[List[E]])
      }
    } catch {
      case e: MappingException =>
        findResolvingException(e) match {
          case Some(ex) => Left(List(ex.validationError.asInstanceOf[E]))
          case _ => throw e
        }
    } finally {
      tl.remove()
    }
  }

  def getContext[T]: Option[T] = threadLocalState.map(_.context.asInstanceOf[T])

  /**
   * Reports extraction error
   */
  def extractionError[E](error: E) = {
    threadLocalState.foreach { state =>
      state.errors = state.errors ++ List(error)
    }
    throw new ExtractionException(error)
  }

  private def findResolvingException(e: MappingException): Option[ExtractionException] = e.cause match {
    case x:ExtractionException => Some(x)
    case x:MappingException if x != e => findResolvingException(x)
    case _ => None
  }

  private def threadLocalState[T]: Option[ExtractionState] = {
    Option(tl.get)
  }

  private class ExtractionException(val validationError: Any) extends RuntimeException
}