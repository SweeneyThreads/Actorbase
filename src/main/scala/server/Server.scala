package server

import java.util.concurrent.ConcurrentHashMap

import akka.actor.{ActorRef, ActorSystem, Props}
import server.actors.{Doorkeeper, Storemanager}

/**
  * Created by matteobortolazzo on 02/05/2016.
  */

object Server extends App {
  var storemanagers = new ConcurrentHashMap[String, ActorRef]()

  override def main(args: Array[String]) {
    val system = ActorSystem("System")

    val doorkeeper = system.actorOf(Props(classOf[Doorkeeper], 8181))

    storemanagers = new ConcurrentHashMap[String, ActorRef]()
    storemanagers.put("test", system.actorOf(Props[Storemanager]))
  }
}

/* RICETTA PER LA FELICITA'

* Fai partire il server (Debug).
* Fai partire il client (Run).
* Visualizza entrambe le finestre contemporaneamente.
* Divertiti con i comandi che Borto ha implementato per te!
*
* listdb, selectdb nome, createdb nome, deletedb nome,
* list, select nome, create nome, delete nome,
* insert 'key' value, update 'key' value, remove 'key', find 'key'.
*
* Il server per ora NON risponde al client MA viene tutto stampato su console.
*
* */

/*  COME è STRUTTURAT0
*
* Il server crea un Doorkeeper (porta 8181) e uno Storemanager ("test"). **uno storemanager rappresenta una database**
* Il Doorkeeper, per ogni connessione, crea un Usermanager.
*
* Ogni stringa inviata dal un client verrà gestita dal proprio Usermanager.
* Quest'ultimo ha il compito di creare i messaggi (con l'aiuto di una classe di parsing) e spedirli ad un Main.
*
* Un Main gestisce i messaggi di un client.
* L'attore Main ha la responsabilità di gestire i database (Storemanager).
* L'attore Storemanager ha la responsabilità di gestire le mappe (Storefinder)*
* Storefinder e Storekeeper li conosciamo tutti.
*
* Ho implementato l'indicizzazione degli Storekeeper attraverso espressioni regolari.
*
* */