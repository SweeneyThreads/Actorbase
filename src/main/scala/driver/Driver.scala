package driver

import java.net.SocketException
/**
  * Created by eliamaino on 10/05/16.
  */
object Driver {

  def connect(host: String, port: Integer, username: String, password: String): Connection = {
    try {
      new ConcreteConnection(host, port, username, password)
    }
    catch {
      case se:Exception => null
    }
  }

}
