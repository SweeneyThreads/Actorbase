package driver

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by Paolo on 10/05/2016.
  */
class DriverTest  extends FlatSpec with Matchers with MockFactory{
  "Driver" should "create a new connection" in {
    val conn:Connection = Driver.connect("localhost", 8181, "admin", "admin")
    conn match{
      case c:FailedConnection =>{
        c.host should be("localhost")
        c.port should be(8181)
        c.username should be("admin")
        c.password should be("admin")
      }
      case c:ConcreteConnection =>{
        c.host should be("localhost")
        c.port should be(8181)
        c.username should be("admin")
        c.password should be("admin")
      }
    }
  }
}
