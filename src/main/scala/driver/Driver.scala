package driver

import java.net.SocketException
/**
  * Created by eliamaino on 10/05/16.
  */

/** Factory object for [Connection] instances */
object Driver {

  /**
    * Creates a connection with the selected server, if no connection can be established a null value is returned
    *
    * @param host  The host name as String
    * @param port  The host port as Integer
    * @param username  The username to be used to access the server as String
    * @param password  The username to be used to access the server as String
    * @return A Connection instance to the server or null value
    */
  def connect(host: String, port: Integer, username: String, password: String): Connection = {
    try {
      new ConcreteConnection(host, port, username, password)
    }
    catch {
      case se:Exception => null
    }
  }
}
