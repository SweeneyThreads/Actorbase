/*
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2016 SWEeneyThreads
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * <p/>
 *
 * @author SWEeneyThreads
 * @version 0.0.1
 * @since 0.0.1
 */

package server.utils

import org.scalatest.{FlatSpec, Matchers}
import server.messages.query.ErrorMessages.InvalidQueryMessage
import server.messages.query._
import server.messages.query.user.DatabaseMessages._
import server.messages.query.user.MapMessages._
import server.messages.query.user.RowMessages._

/**
  * Created by kurt on 08/05/2016.
  */
case class SampleCommand(command: String,strMessage: String,message1: QueryMessage,message2: QueryMessage = null)

class ParserTest extends FlatSpec with Matchers {
  val parser = new Parser()
  //TU3
  "'login' command" should "generate a LoginMessage with 1rd and 2th param" in {
    parser.parseQuery("login admin admin") should be(new LoginMessage("admin", "admin"))
    parser.parseQuery("login tpadovan psw") should be(new LoginMessage("tpadovan", "psw"))
  }
  it should "have 2, well formed, parameters" in {
    parser.parseQuery("login") should be(new InvalidQueryMessage)
    parser.parseQuery("login admin") should be(new InvalidQueryMessage)
    parser.parseQuery("login admin admin admin") should be(new InvalidQueryMessage)
  }

  //test dei comandi a zero parametri TU4
  val zeroParamCommands = Array(
    new SampleCommand("listdb", "ListDatabaseMessage", new ListDatabaseMessage),
    new SampleCommand("listmap", "ListMapMessage", new ListMapMessage),
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

  //Testing commands with one param TU5
  val oneParamCommands = Array(
    new SampleCommand("selectdb", "SelectDatabaseMessage", new SelectDatabaseMessage("aparam"), new SelectDatabaseMessage("anotherparam")),
    new SampleCommand("createdb", "CreateDatabaseMessage", new CreateDatabaseMessage("aparam"), new CreateDatabaseMessage("anotherparam")),
    new SampleCommand("deletedb", "DeleteDatabaseMessage", new DeleteDatabaseMessage("aparam"), new DeleteDatabaseMessage("anotherparam")),
    new SampleCommand("selectmap", "SelectMapMessage", new SelectMapMessage("aparam"), new SelectMapMessage("anotherparam")),
    new SampleCommand("createmap", "CreateMapMessage", new CreateMapMessage("aparam"), new CreateMapMessage("anotherparam")),
    new SampleCommand("deletemap", "DeleteMapMessage", new DeleteMapMessage("aparam"), new DeleteMapMessage("anotherparam"))
  )
  for (cmd <- oneParamCommands) {
    "'" + cmd.command + "' command" should "generate a " + cmd.strMessage in {
      parser.parseQuery(cmd.command + " aparam") should be(cmd.message1)
      parser.parseQuery(cmd.command + " anotherparam") should be(cmd.message2)
    }
    it should "have exactly 1 parameter" in {
      parser.parseQuery(cmd.command) should be(new InvalidQueryMessage)
      parser.parseQuery(cmd.command + " something something") should be(new InvalidQueryMessage)
      parser.parseQuery(cmd.command + " something something something") should be(new InvalidQueryMessage)
    }
  }

  //Testing row level commands with one param TU6
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

  //Testing row level commands with two params TU7
  val twoParamCommands = Array(
    new SampleCommand("insert", "InsertRowMessage", new InsertRowMessage("aKey", "aValue".getBytes("UTF-8")), new InsertRowMessage("one word is not enough for that key", "anotherValue".getBytes("UTF-8"))),
    new SampleCommand("update", "UpdateRowMessage", new UpdateRowMessage("aKey", "aValue".getBytes("UTF-8")), new UpdateRowMessage("one word is not enough for that key", "anotherValue".getBytes("UTF-8")))
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