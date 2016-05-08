import org.scalatest.{Matchers, FlatSpec}
import server.messages._
import server.util.Parser

/**
  * Created by kurt on 08/05/2016.
  */
class ParserTest extends FlatSpec with Matchers{
  val parser = new Parser()

  "'connect' command" should "generate a ConnectMessage with 3rd and 4th param" in {
    parser.parseQuery("connect localhost:8181 admin admin") should be (new ConnectMessage("admin","admin"))
    parser.parseQuery("connect 192.168.1.110:8080 tpadovan psw") should be (new ConnectMessage("tpadovan","psw"))
  }
  it should "have 3, well formed, parameters" in {
    parser.parseQuery("connect localhost:8181") should be (new InvalidQueryMessage)
    parser.parseQuery("connect localhost:8181 admin") should be (new InvalidQueryMessage)
    parser.parseQuery("connect localhost:8181 admin admin admin") should be (new InvalidQueryMessage)
    parser.parseQuery("connect localhost admin admin") should be (new InvalidQueryMessage)
  }



  //test dei comandi listdb, list, e keys
  "'listdb' command" should "generate a ListDatabaseMessage" in {
    parser.parseQuery("listdb") should be (new ListDatabaseMessage)
  }
  it should "have 0 parameters" in {
    parser.parseQuery("listdb databaseName") should be (new InvalidQueryMessage)
    parser.parseQuery("listdb databaseName mapName") should be (new InvalidQueryMessage)
  }

  "'list' command" should "generate a ListMapMessage" in {
    parser.parseQuery("list") should be (new ListMapMessage)
  }
  it should "have 0 parameters" in {
    parser.parseQuery("list databaseName") should be (new InvalidQueryMessage)
    parser.parseQuery("list databaseName mapName") should be (new InvalidQueryMessage)
  }

  "'keys' command" should "generate a ListKeysMessage" in {
    parser.parseQuery("keys") should be (new ListKeysMessage)
  }
  it should "have 0 parameters" in {
    parser.parseQuery("keys databaseName") should be (new InvalidQueryMessage)
    parser.parseQuery("keys databaseName mapName") should be (new InvalidQueryMessage)
  }


  //test dei comandi ad un parametro
  val oneParamCommands = Array(
    new SampleCommand("selectdb","SelectDatabaseMessage",new SelectDatabaseMessage("aParam"), new SelectDatabaseMessage("anotherParam")),
    new SampleCommand("createdb","CreateDatabaseMessage",new CreateDatabaseMessage("aParam"), new CreateDatabaseMessage("anotherParam")),
    new SampleCommand("deletedb","DeleteDatabaseMessage",new DeleteDatabaseMessage("aParam"), new DeleteDatabaseMessage("anotherParam")),
    new SampleCommand("select","SelectMapMessage",new SelectMapMessage("aParam"), new SelectMapMessage("anotherParam")),
    new SampleCommand("create","CreateMapMessage",new CreateMapMessage("aParam"), new CreateMapMessage("anotherParam")),
    new SampleCommand("delete","DeleteMapMessage",new DeleteMapMessage("aParam"), new DeleteMapMessage("anotherParam")),
    new SampleCommand("find","FindRowMessage",new FindRowMessage("aParam"), new FindRowMessage("anotherParam")),
    new SampleCommand("remove","RemoveRowMessage",new RemoveRowMessage("aParam"), new RemoveRowMessage("anotherParam"))
  )
  for (cmd <- oneParamCommands) {
    "'"+cmd.command+"' command" should "generate a "+cmd.strMessage in {
      parser.parseQuery(cmd.command+" aParam") should be (cmd.message1)
      parser.parseQuery(cmd.command+" anotherParam") should be (cmd.message2)
    }
    it should "have exactly 1 parameter" in {
      parser.parseQuery(cmd.command) should be (new InvalidQueryMessage)
      parser.parseQuery(cmd.command+" something something") should be (new InvalidQueryMessage)
      parser.parseQuery(cmd.command+" something something something") should be (new InvalidQueryMessage)
    }
  }


  //test dei comandi con 2 parametri
  val twoParamCommands = Array(
    new SampleCommand("insert","InsertRowMessage", new InsertRowMessage("aKey","aValue".getBytes()), new InsertRowMessage("anotherKey","anotherValue".getBytes())),
    new SampleCommand("update","UpdateRowMessage", new UpdateRowMessage("aKey","aValue".getBytes()), new UpdateRowMessage("anotherKey","anotherValue".getBytes()))
  )
  for (cmd <- twoParamCommands) {
    "'"+cmd.command+"' command" should "generate a "+cmd.strMessage in {
      parser.parseQuery(cmd.command+" aKey aValue") should be (cmd.message1)
      parser.parseQuery(cmd.command+" anotherKey anotherValue") should be (cmd.message2)
    }
    it should "have exactly 2 parameters" in {
      parser.parseQuery(cmd.command) should be (new InvalidQueryMessage)
      parser.parseQuery(cmd.command+" something") should be (new InvalidQueryMessage)
      parser.parseQuery(cmd.command+" something something something") should be (new InvalidQueryMessage)
    }
  }
}





class SampleCommand(val command: String,
                   val strMessage: String,
                   val message1: ActorbaseMessage,
                   val message2: ActorbaseMessage)




















