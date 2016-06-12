
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

package client


/**
  * The command line welcome banner of Actorbase.
  */
object Welcome {

  /**
    * Prints welcome banner and user information on the console.
    */
  def printWelcomeMessage(): Unit = {
    val banner = """
                   # ______  ____    ______  _____   ____    ____     ______  ____    ____
                   #/\  _  \/\  _`\ /\__  _\/\  __`\/\  _`\ /\  _`\  /\  _  \/\  _`\ /\  _`\
                   #\ \ \L\ \ \ \/\_\/_/\ \/\ \ \/\ \ \ \L\ \ \ \L\ \\ \ \L\ \ \,\L\_\ \ \L\_\
                   # \ \  __ \ \ \/_/_ \ \ \ \ \ \ \ \ \ ,  /\ \  _ <'\ \  __ \/_\__ \\ \  _\L
                   #  \ \ \/\ \ \ \L\ \ \ \ \ \ \ \_\ \ \ \\ \\ \ \L\ \\ \ \/\ \/\ \L\ \ \ \L\ \
                   #   \ \_\ \_\ \____/  \ \_\ \ \_____\ \_\ \_\ \____/ \ \_\ \_\ `\____\ \____/
                   #    \/_/\/_/\/___/    \/_/  \/_____/\/_/\/ /\/___/   \/_/\/_/\/_____/\/___/  v0.0.1
                   #
                   #An Open-Source No-SQL Database, based on the actor model!
                   #
                   #""".stripMargin('#')

    val os = System.getProperty("os.name")
    val version = System.getProperty("os.version")
    val user = System.getProperty("user.name")
    val javaVersion = System.getProperty("java.version")

    println(banner)
    println("Hello " + user + "! And welcome to ACTORBASE \n")
    println("You are using " + os + " " + version + " with Java " + javaVersion + "\n " )
    println("Connect to a server using: connect address:port username password \n ")
    println("-----------------------------------------------------------------\n \n")
  }
}
