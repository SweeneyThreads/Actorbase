package Driver

/**
  * Created by eliamaino on 10/05/16.
  */
object ActorbaseClient {

  def connect(host: String, port: Integer, username: String, password: String): ActorbaseConnectionProxy = {
    new ActorbaseConnectionProxy()(host,port,username,password)
  }
}
