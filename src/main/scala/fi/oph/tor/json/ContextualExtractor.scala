package fi.oph.tor.json

import org.json4s.{Formats, JValue, MappingException}

object ContextualExtractor {
  case class ExtractionState(val context: Any, var errors: List[Any])

  private val tl = new ThreadLocal[ExtractionState]

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

  private def findResolvingException(e: MappingException): Option[ResolvingException] = e.cause match {
    case x:ResolvingException => Some(x)
    case x:MappingException if x != e => findResolvingException(x)
    case _ => None
  }

  def context[T]: Option[T] = threadLocalState.map(_.context.asInstanceOf[T])

  def threadLocalState[T]: Option[ExtractionState] = {
    Option(tl.get)
  }

  def parseError[E](error: E) = {
    threadLocalState.foreach { state =>
      state.errors = state.errors ++ List(error)
    }
    throw new ResolvingException(error)
  }

  private class ResolvingException(val validationError: Any) extends RuntimeException
}