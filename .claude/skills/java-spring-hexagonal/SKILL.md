---
name: java-spring-hexagonal
description: Wo Java-Code in diesem Projekt hingehört - Domain-Modul ohne Framework, Ports als Interfaces, Adapter außen - mit Negativbeispielen zu Spring-Annotationen in der Domäne, Entities als DTO, @Transactional-Fallstricken und Repository-Zuschnitt. Nutze diesen Skill bei jeder Änderung unter services/, beim Anlegen neuer Klassen, bei Fragen zu Spring, JPA, Transaktionen oder Modulgrenzen.
---

# Java und Spring, hexagonal geschnitten

Grundlage: [ADR-001](../../../docs/adr/ADR-001-ports-and-adapters.md).
ArchUnit prüft die Regeln bei jedem CI-Lauf.

## Der Zuschnitt

```
services/scheduling/
  domain/          reines Java. Keine Annotation, keine Abhängigkeit.
    model/         Records als Value Objects
    regel/         eine Klasse je Fachregel
    port/          Interfaces - was die Domäne von außen braucht
  application/     Anwendungsfälle. Orchestriert, entscheidet nicht.
  infrastructure/  Spring, JPA, REST. Implementiert die Ports.
```

Abhängigkeitsrichtung: infrastructure zu application zu domain. Niemals
umgekehrt. Das Domain-Modul hat in seiner Build-Datei keine Spring-Abhängigkeit,
so kann der Fehler gar nicht erst kompilieren.

## Negativbeispiele

Die wirken besser als abstrakte Regeln.

### 1. Spring-Annotation im Domain-Modul

```java
// FALSCH - domain/regel/FrequenzGrenze.java
@Component
public class FrequenzGrenze { ... }
```

Warum falsch: Die Regel ist jetzt nur noch mit Spring-Kontext instanziierbar.
Der Test braucht einen Anwendungskontext und dauert Sekunden statt
Millisekunden.

```java
// RICHTIG - domain/regel/FrequenzGrenze.java
public final class FrequenzGrenze {
    public Pruefergebnis pruefe(Verordnung v, LocalDate termin, List<Termin> bisher) { ... }
}

// infrastructure/config/DomainConfig.java
@Configuration
class DomainConfig {
    @Bean FrequenzGrenze frequenzGrenze() { return new FrequenzGrenze(); }
}
```

Die Verdrahtung gehört nach außen, nicht in die Regel.

### 2. Entity als API-DTO durchgereicht

```java
// FALSCH
@GetMapping("/termine/{id}")
public TerminEntity holen(@PathVariable UUID id) {
    return repo.findById(id).orElseThrow();
}
```

Warum falsch: Jede Spaltenumbenennung wird zum API-Bruch. Lazy geladene Felder
werden beim Serialisieren nachgeladen, also außerhalb der Transaktion. Das
endet in einer LazyInitializationException oder im N+1-Problem. Und die Entity
trägt die Mandanten-ID, die niemals nach außen gehört.

```java
// RICHTIG
@GetMapping("/termine/{id}")
public TerminResponse holen(@PathVariable UUID id) {
    return TerminResponse.von(service.holen(new TerminId(id)));
}
```

### 3. Transaktions-Annotation auf privater Methode

```java
// FALSCH - wirkungslos
@Service
public class BuchungsService {
    public void buchen(...) { speichern(...); }

    @Transactional
    private void speichern(...) { ... }
}
```

Warum wirkungslos: Spring legt einen Proxy um die Bean. Ein Aufruf von innen
geht am Proxy vorbei. Dasselbe gilt für den Aufruf einer öffentlichen
annotierten Methode aus derselben Klasse, den Selbstaufruf. Die Annotation
gehört an die öffentliche Methode, die von außen aufgerufen wird, oder die
Methode gehört in eine eigene Bean.

Weitere Fallstricke, die im Interview kommen:

- readOnly bei Lesepfaden spart den Dirty Check, und Hibernate darf den Flush
  unterlassen.
- REQUIRES_NEW öffnet eine zweite Verbindung. Bei knappem Verbindungspool ein
  Deadlock-Kandidat.
- Eine RuntimeException rollt zurück, eine checked Exception per Default nicht.

### 4. Repository am falschen Ort

```java
// RICHTIG
// domain/port/TerminRepository.java - Interface, spricht Domänensprache
public interface TerminRepository {
    List<Termin> imZeitraum(TherapeutId t, LocalDate von, LocalDate bis);
}

// infrastructure/persistence/JpaTerminRepository.java
@Repository
class JpaTerminRepository implements TerminRepository { ... }
```

Das Interface gehört der Domäne, weil sie es braucht. Die Implementierung
gehört nach außen, weil sie JPA kennt.

## Java-Konventionen in diesem Projekt

- **Records für Value Objects.** Ein eigener Typ statt UUID überall verhindert
  vertauschte Parameter beim Aufruf.
- **java.time durchgehend.** Kein Date, kein Calendar, kein SimpleDateFormat.
- **Kein null über Modulgrenzen.** Optional als Rückgabetyp, nie als Feld oder
  Parameter.
- **final als Standard** bei Klassen und Feldern. Konstruktor-Injection statt
  Feld-Injection.
- **Prüfergebnisse tragen eine Begründung.** Ein eigener Ergebnistyp statt
  boolean, damit die Oberfläche sagen kann, warum ein Slot nicht geht.

## Für den Einstieg in Java

Reihenfolge, damit Sprache und Framework nicht gleichzeitig gelernt werden:

1. Domänenkern mit Records, java.time und Regelklassen. Kein Spring.
2. Tests dazu mit JUnit 5 und parametrisierten Testfällen.
3. Dann erst Spring: Dependency Injection, Controller, Transaktionen, Data JPA.
4. Zuletzt Infrastruktur: Testcontainers, Multi-Tenancy-Hooks in Hibernate.

**Wichtig:** Agenten beschleunigen das Schreiben von Java stark, das Verstehen
nicht. Beim Domänenkern bewusst weniger delegieren. Was hier steht, muss
zeilengenau erklärbar sein.
