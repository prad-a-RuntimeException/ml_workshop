package moviestore.misc

import java.util.ResourceBundle

import moviestore.AppResource

object ResourceLoader {
  private def resourceBundle(key: String) = ResourceBundle.getBundle(key)

  val resourceCache: Map[AppResource.Value, ResourceBundle] =
    Map(
      AppResource.TriplestoreResource -> resourceBundle(AppResource.TriplestoreResource.toString)
    )


  def apply(resource: AppResource.Value, key: String): Option[String] = {
    val resourceBundle = resourceCache.getOrElse(resource, null)
    if (resourceBundle != null && resourceBundle.containsKey(key)) Option(resourceBundle.getString(key))
    else Option.empty
  }


}
