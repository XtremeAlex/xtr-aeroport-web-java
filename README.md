> Stato: deprecato dal 29/09/2026. Questo progetto non è più mantenuto.
> Il suo posto l'ha preso `xtr-aeroport-edifact-spring-web`, che adesso fa tutto da solo: home, decodifica e creazione dei messaggi EDIFACT, SEO. Quel progetto non è ancora pubblicato su GitHub.
> Il codice resta qui per chi vuole consultarlo, ma non riceverà più correzioni, aggiornamenti di sicurezza o nuove release.

# xtr-aeroport-web-java

Il frontend web della suite Aeroport. Serviva a consultare aeroporti e paesi e a lavorare con i messaggi EDIFACT PAXLST, usando le API del backend.

## Cosa faceva

È partito come frontend "sottile": nessun database suo, niente JPA. Tutti i dati arrivavano da `xtr-aeroport-api-spring` tramite un client HTTP dichiarativo (`@HttpExchange`). Con il tempo è diventato parecchio di più:

- home di presentazione del prodotto, in italiano e inglese, con tema chiaro e scuro e footer legale;
- elenchi di aeroporti (`/airports`) e paesi (`/countries`);
- pagina EDIFACT (`/edifact`) con un tabellone in stile aeroporto: decodifichi un messaggio incollandolo o caricando un file, e vedi i voli raggruppati con i passeggeri e il dettaglio di ogni volo;
- ispettore visuale del messaggio: albero di segmenti ed elementi, più un log in stile console;
- creazione guidata di un messaggio EDIFACT (`/edifact/new`) e codifica da JSON;
- SEO lato server: canonical, hreflang, Open Graph e `sitemap.xml`;
- interfaccia minimale in stile Nothing Phone.

Per i file grandi c'erano delle protezioni: una sola chiamata `/analyze` all'API, un limite sulla quantità di dati mostrati, uno stop fisso a 5 MB sulla decodifica sincrona e un overlay annullabile durante il caricamento.

## Stack

- Java 21, Spring Boot 3.4, Thymeleaf lato server, Alpine.js
- Virtual threads, actuator ridotto al solo `health`
- Dockerfile multi-stage e `docker-compose.yml`

## Configurazione e avvio

| Proprietà | Valore |
|---|---|
| artifactId / version | `aeroport-web` / `1.0.0` |
| porta | `8082` (`SERVER_PORT`) |
| context-path | `/xtr-aeroport` |
| URL dell'API | `AEROPORT_API_BASE_URL` (in locale `http://localhost:8080/xtr-aeroport-api`, nel cluster `http://xtr-aeroport-api.<namespace>.svc.cluster.local:8080/xtr-aeroport-api`) |
| timeout verso l'API | `AEROPORT_API_CONNECT_TIMEOUT_MS`, `AEROPORT_API_READ_TIMEOUT_MS` |
| dominio pubblico | `AEROPORT_PUBLIC_BASE_URL`, usato per canonical, hreflang, Open Graph e sitemap |
| upload massimo | 50 MB |

```bash
cp .env.example .env      # il file .env non va committato
docker compose up --build
```

## La suite

| Modulo | A cosa serve | Stato |
|---|---|---|
| `xtr-aeroport-api-spring` | API unica per aeroporti, tipologie, paesi e messaggi EDIFACT (non ancora pubblicata su GitHub) | Attivo |
| `xtr-aeroport-api-quarkus` | Porting della stessa API su Quarkus (non ancora pubblicato su GitHub) | Sperimentale |
| `xtr-aeroport-edifact-spring-web` | Console web EDIFACT, ha preso il posto di `xtr-aeroport-web-java` (non ancora pubblicata su GitHub) | Attivo |
| [`xtr-aeroport-batch`](https://github.com/XtremeAlex/xtr-aeroport-batch) | Import massivo dei dati | Attivo, offline |
| [`xtr-aeroport-common-lib`](https://github.com/XtremeAlex/xtr-aeroport-common-lib) | Libreria condivisa | Legacy |
| [`xtr-aeroport-ms`](https://github.com/XtremeAlex/xtr-aeroport-ms) | Microservizio di ricerca aeroporti | Deprecato |
| [`xtr-aeroport-typology`](https://github.com/XtremeAlex/xtr-aeroport-typology) | Servizio dati tipologici | Deprecato |
| [`xtr-aeroport-web-java`](https://github.com/XtremeAlex/xtr-aeroport-web-java) | Frontend web (questo modulo) | Deprecato |

## Licenza
Doppia licenza: **GNU AGPL-3.0** (vedi [`LICENSE`](LICENSE)) per l'uso open source, e **licenza commerciale** per l'uso dentro prodotti proprietari (vedi [`COMMERCIAL-LICENSE.md`](COMMERCIAL-LICENSE.md)).

## Contatti

Andrei Alexandru Dabija · [LinkedIn](https://www.linkedin.com/in/andrei-alexandru-dabija/) · [github.com/XtremeAlex](https://github.com/XtremeAlex)
