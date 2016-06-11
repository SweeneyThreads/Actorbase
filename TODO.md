# Dev TODO List
Robe da sviluppare

>"Working harder  
>&nbsp;Make it better  
>&nbsp;Do it faster  
>&nbsp;Make it stronger"   
>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;~Sasha Gray

## FATTI
* Risposte al client
* Loggin' attori
* Più storefinder per mappa
* Superuser
* Driver
* Client
* Parsing comandi utente
* Parsing comandi admin
* Gestiti tutti i comandi utente
* Ninja (messaggio become e metodo become)
* Permessi utente admin
* Togliere spazi iniziali e finali nei comandi in entrata (da più margine d'errore)
* Spostare risposte testuali da tutti gli attori a solo Usermanager
* Gestire comandi su più pacchetti in Usermanager / Driver
* Spostare responsabilità usermanger da Server and ActorSystem
* Gestire utenti e permessi in database 'master'
* Gestione comandi admin nel main
* Libreria per gestire file disco per Warehouseman
* Crezione dinamica storekeeper
* Creazione dinamica storefinder
* Creazione dinamica storemanager

## DA FINIRE

* Warehouseman
* Timeout connessione client/server
* Gestione distribuzione (conf Akka)
* Fare un README con processo di compilazione
* Accensione server (con lettura configurazione)

##DA INIZIARE
* Gestione morte storekeeper -> trasformazione Ninja
* Gestione Ninja e Warehouseman di uno Storekeeper (messaggi ed invio)
* Spegnimento server (con salvataggio configurazione)
* Gestire errore porta occupata
* Gestire application.conf esterno (non compilato)
* Azzerare selectmap quando viene eliminata la mappa
* Mettere selectmap quando viene creata
* Fare help specifici più lunghi con anche le stringhe in caso di errore
* Mostrare tipo di errore di connessione
* Controllare currentMethodName nel log
* Fare comando refresh config
* Fare test Main e Storemanager
* Leggere proprietà attori da file
* Mettere scaladoc mancanti nel Main e controllare altri.
* Togliere info inutili nei messaggi.
