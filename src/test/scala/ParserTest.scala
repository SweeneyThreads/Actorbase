import java.util

import org.scalatest.{FlatSpec, Matchers}
import server.messages._
import server.util.Parser

/**
  * Created by kurt on 08/05/2016.
  */
case class SampleCommand(command: String,strMessage: String,message1: ActorbaseMessage,message2: ActorbaseMessage = null)

class ParserTest extends FlatSpec with Matchers {
  val parser = new Parser()

  "'connect' command" should "generate a ConnectMessage with 3rd and 4th param" in {
    parser.parseQuery("connect localhost:8181 admin admin") should be(new ConnectMessage("admin", "admin"))
    parser.parseQuery("connect 192.168.1.110:8080 tpadovan psw") should be(new ConnectMessage("tpadovan", "psw"))
  }
  it should "have 3, well formed, parameters" in {
    parser.parseQuery("connect localhost:8181") should be(new InvalidQueryMessage)
    parser.parseQuery("connect localhost:8181 admin") should be(new InvalidQueryMessage)
    parser.parseQuery("connect localhost:8181 admin admin admin") should be(new InvalidQueryMessage)
    parser.parseQuery("connect localhost admin admin") should be(new InvalidQueryMessage)
  }

  //test dei comandi a zero parametri
  val zeroParamCommands = Array(
    new SampleCommand("listdb", "ListDatabaseMessage", new ListDatabaseMessage),
    new SampleCommand("list", "ListMapMessage", new ListMapMessage),
    new SampleCommand("keys", "ListKeysMessage", new ListKeysMessage)
  )
  for (cmd <- zeroParamCommands) {
    "'" + cmd.command + "' command" should "generate a " + cmd.strMessage in {
      parser.parseQuery(cmd.command) should be(cmd.message1)
    }
    it should "have 0 parameters" in {
      parser.parseQuery(cmd.command + " lightside") should be(new InvalidQueryMessage)
      parser.parseQuery(cmd.command + " dark side") should be(new InvalidQueryMessage)
    }
  }

  //Testing commands with one param
  val oneParamCommands = Array(
    new SampleCommand("selectdb", "SelectDatabaseMessage", new SelectDatabaseMessage("aParam"), new SelectDatabaseMessage("anotherParam")),
    new SampleCommand("createdb", "CreateDatabaseMessage", new CreateDatabaseMessage("aParam"), new CreateDatabaseMessage("anotherParam")),
    new SampleCommand("deletedb", "DeleteDatabaseMessage", new DeleteDatabaseMessage("aParam"), new DeleteDatabaseMessage("anotherParam")),
    new SampleCommand("select", "SelectMapMessage", new SelectMapMessage("aParam"), new SelectMapMessage("anotherParam")),
    new SampleCommand("create", "CreateMapMessage", new CreateMapMessage("aParam"), new CreateMapMessage("anotherParam")),
    new SampleCommand("delete", "DeleteMapMessage", new DeleteMapMessage("aParam"), new DeleteMapMessage("anotherParam"))
  )
  for (cmd <- oneParamCommands) {
    "'" + cmd.command + "' command" should "generate a " + cmd.strMessage in {
      parser.parseQuery(cmd.command + " aParam") should be(cmd.message1)
      parser.parseQuery(cmd.command + " anotherParam") should be(cmd.message2)
    }
    it should "have exactly 1 parameter" in {
      parser.parseQuery(cmd.command) should be(new InvalidQueryMessage)
      parser.parseQuery(cmd.command + " something something") should be(new InvalidQueryMessage)
      parser.parseQuery(cmd.command + " something something something") should be(new InvalidQueryMessage)
    }
  }

  //Testing row level commands with one param
  val test = Array(
    new SampleCommand("find", "FindRowMessage", new FindRowMessage("fourwordsalluppercase"), new FindRowMessage("ONE WORD ALL LOWERCASE")),
    new SampleCommand("remove", "RemoveRowMessage", new RemoveRowMessage("fourwordsalluppercase"), new RemoveRowMessage("ONE WORD ALL LOWERCASE"))
  )
  for (cmd <- test) {
    "'" + cmd.command + "' command" should "generate a " + cmd.strMessage in {
      parser.parseQuery(cmd.command + " 'fourwordsalluppercase'") should be(cmd.message1)
    }
    it should "accept multiple words as parameter and generate the correct message" in {
      parser.parseQuery(cmd.command + " 'ONE WORD ALL LOWERCASE'") should be(cmd.message2)
    }
    it should "have exactly 1 parameter" in {
      parser.parseQuery(cmd.command) should be(new InvalidQueryMessage)
      parser.parseQuery(cmd.command + " 'something' something") should be(new InvalidQueryMessage)
      parser.parseQuery(cmd.command + " 'some things' 'somethingelse'") should be(new InvalidQueryMessage)
      parser.parseQuery(cmd.command + " 'something' 'something' something") should be(new InvalidQueryMessage)
    }
  }

  //Testing row level commands with two params
  val twoParamCommands = Array(
    new SampleCommand("insert", "InsertRowMessage", new InsertRowMessage("aKey", util.Arrays.toString("aValue".getBytes("UTF-8"))), new InsertRowMessage("one word is not enough for that key", util.Arrays.toString("anotherValue".getBytes("UTF-8")))),
    new SampleCommand("update", "UpdateRowMessage", new UpdateRowMessage("aKey", util.Arrays.toString("aValue".getBytes("UTF-8"))), new UpdateRowMessage("one word is not enough for that key", util.Arrays.toString("anotherValue".getBytes("UTF-8"))))
  )
  for (cmd <- twoParamCommands) {
    "'" + cmd.command + "' command" should "generate a " + cmd.strMessage + " using first param as Key and bytes-value of second param as Value" in {
      parser.parseQuery(cmd.command + " 'aKey' aValue") should be(cmd.message1)
    }
    it should "accept multiple words as first param and generate the correct message" in {
      parser.parseQuery(cmd.command + " 'one word is not enough for that key' anotherValue") should be(cmd.message2)
    }
    it should "have exactly 2 parameters" in {
      parser.parseQuery(cmd.command) should be(new InvalidQueryMessage)
      parser.parseQuery(cmd.command + " something") should be(new InvalidQueryMessage)
      parser.parseQuery(cmd.command + " something something something") should be(new InvalidQueryMessage)
    }
  }
}