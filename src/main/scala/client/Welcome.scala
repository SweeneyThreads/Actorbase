package client

/**
  * Created by eliamaino on 05/05/16.
  */
object Welcome {
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

  def printWelcomeMessage: Unit = {
    println(banner)
    println("Hello " + user + "! And welcome to ACTORBASE \n")
    println("You are using " + os + " " + version + " with Java " + javaVersion + "\n " )
    println("Connect to a server using: connect address username password \n \n")
  }
}
