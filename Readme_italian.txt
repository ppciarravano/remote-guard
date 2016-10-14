README.TXT 

Progetto per un Sistema di Video/Audio Sorveglianza Remota (RemoteGuard)
di Pier Paolo Ciarravano

Il progetto da me realizzato e' stato interamente sviluppato utilizzando Eclipse 3.2.1 con versione JAVA 1.6.
La versione Java 1.6 e' necessaria sia per la compilazione che per l'esecuzione, in quanto ho utilizzato la classe java.awt.SystemTray presente solo in questa release.
Ho sviluppato e implementato sia l'acquisizione video che quella audio.
Per quanto riguarda le librerie esterne ho utilizzato:
JMF (Java Media Framework 2.1.1e) sia per l'accesso alla webcam e al sistema di acquisizione audio, che per la comunicazione RTP (Real-time Transport Protocol)
La libreria Log4j per la gestione dei messaggi di log
La libreria Open Source Simple Reliable UDP (http://rudp.sourceforge.net/) per la gestione della comunicazione affidabile dei pacchetti UDP della comunicazione per i messaggi di comando.

Ho chiamato l'applicazione "RemoteGuard" e ho sviluppato con i seguenti nomi queste classi main:

ppc.remoteguard.server.Server: Main per l'applicazione server centrale
ppc.remoteguard.guard.Guard: Main per il client di acquisizione
ppc.remoteguard.controller.Controller: Main per il client di Monitoring

Principalmente la mia attenzione, oltre che a sviluppare tutte le specifiche richiesta dal documento di progetto, si e' focalizzata nell'implementare tutta la comunicazione di rete utilizzando esclusivamente UDP.
Utilizzando la normale comunicazione RTP gestita da JMF si utilizzano ben 4 porte UDP (due per i dati video e il relativo canale di controllo e le altre due per il canale audio e il relativo canale di controllo); inoltre avrei avuto bisogno di un ulteriore porta per i pacchetti di controllo e comando tra le varie componenti client/server. Ho deciso di effettuare un multiplex di tutti i 5 "canali" di cui avevo bisogno su un unico "canale": in questo modo tutta la comunicazione tra il server centrale e il client di monitoring e di acquisizione si svolge sulla stessa porta UDP. Inoltre per non dover implementare pesanti controlli per l'affidabilita' dei messaggi di comando utilizzando UDP, ho usato un wrapper della libreria "Simple Reliable UDP" solo sul "canale" dei messaggi. In questo modo ho ottenuto una comunicazione affidabile di tutti i pacchetti di comunicazione dei messaggi di comando tra le componenti client/server. La libreria "Simple Reliable UDP" (http://rudp.sourceforge.net/) utilizza il protocollo basato su l'Internet Draft degli autori Bova, Krivoruchka e Cisco Systems (1999) intitolato "Reliable UDP" del Febbraio 1999 (http://www.ietf.org/old/2009/proceedings/99mar/I-D/draft-ietf-sigtran-reliable-udp-00.txt).

Oltre al file di configurazione di log4j, e' presente un file di configurazione, al livello della root classpath, chiamato constant.properties che dettaglia tutti i parametri di configurazione dell'applicazione.

La struttura della directory del progetto comprende:
bin: classi di progetto compilate
bin_jar: esecutivi dei tre programmi (Server, Guard e Controller) in pacchetti jar eseguibili, con i relativi file di configurazione
doc: javadoc di tutte le classi del progetto
lib: librerie esterne (Log4j e ReliableUDP con relativa documentazione)
src: sorgenti dell'applicazione
build.xml: file di compilazione per Ant (e' sufficiente posizionarsi nella directory dove e' situato il file build.xml e lanciare Ant da linea di comando per compilare l'applicazione)
Readme.txt: questo file

Descrivo di seguito brevemente la struttura dei package del progetto (nella javadoc del progetto ho dettagliato tutte le classi):
ppc.remoteguard : classi comuni a tutte le componenti main dell'applicazione (Server, Guard e Controller)
ppc.remoteguard.controller : classi del main del client di monitoraggio
ppc.remoteguard.guard : classi del main del client di acquisizione
ppc.remoteguard.server : classi del main del server centrale
ppc.remoteguard.log : classi per la gestione del log con Log4j
ppc.remoteguard.resources : icone gif utilizzate dal SystemTray e dalle finestre
ppc.remoteguard.rudp : Wrapper per la gestione del canale Reliable UDP
ppc.remoteguard.util : classi di utilita' varie

Per eseguire i main delle tre componenti dell'applicazione e' sufficiente lanciare i file jar eseguibili nella cartella bin_jar, o posizionarsi all'interno della cartella bin e lanciare il comando:
java -classpath .;..\lib\log4j-1.2.15.jar;..\lib\reliableudp.jar ppc.remoteguard.server.Server
java -classpath .;..\lib\log4j-1.2.15.jar;..\lib\reliableudp.jar ppc.remoteguard.guard.Guard
java -classpath .;..\lib\log4j-1.2.15.jar;..\lib\reliableudp.jar ppc.remoteguard.controller.Controller
