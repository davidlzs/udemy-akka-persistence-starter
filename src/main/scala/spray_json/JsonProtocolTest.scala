package spray_json

import spray.json.{DefaultJsonProtocol, _}

case class Color(name: String, red: Int, green: Int, blue: Int)

object MyJsonProtocol extends DefaultJsonProtocol {
  implicit val colorFormat = jsonFormat4(Color.apply)
}

object JsonProtocolTest extends App {

  import MyJsonProtocol.colorFormat
  val json = Color("CadetBlue", 95, 158, 160).toJson
  val color = json.convertTo[Color]
  println(color)
}





