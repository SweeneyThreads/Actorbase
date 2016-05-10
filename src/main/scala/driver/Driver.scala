package driver

/**
  * Created by eliamaino on 10/05/16.
  */
object Driver {

  def connect(host: String, port: Integer, username: String, password: String): Connection = {
    new Connection(host,port,username,password)
  }
}
