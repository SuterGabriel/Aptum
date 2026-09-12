# Betrieb

Wie Aptum läuft, wie es ausgerollt wird, was es einem Betrieb sagt — und was
hier bewusst nicht gebaut ist. Der letzte Teil ist der wichtigste: Ein
Portfolio-Projekt, das so tut, als betriebe es einen Cluster, ist weniger
glaubwürdig als eines, das die Grenze benennt.

## Drei Wege, dieselbe Anwendung

| Weg | Wofür | Befehl |
|---|---|---|
| **compose** | Anschauen, entwickeln, vorführen | `docker compose up --build` → http://localhost:8000 |
| **kind + Terraform + ArgoCD** | Beweisen, dass das Deployment funktioniert — lokal wie in der CI | siehe README, „Starten" |
| **Ein echter Cluster** | Betrieb | nicht Teil dieses Repos, siehe unten |

Alle drei nutzen dieselben Bilder aus denselben Dockerfiles und dasselbe
Init-Skript für die Datenbankrolle. Was in compose läuft, läuft im Cluster;
der Unterschied ist der Weg dorthin, nicht das Ergebnis.

## Die Bilder

Drei Dienste, drei mehrstufige Dockerfiles: bauen mit dem vollen
Werkzeugkasten, laufen mit dem Nötigsten. Kein Prozess läuft als Root; die
Benutzer-ID ist numerisch (10001), weil Kubernetes `runAsNonRoot` nur gegen
eine Zahl prüfen kann. Jedes Bild hat einen Healthcheck auf denselben
Endpunkt, den auch der Cluster fragt.

Die Bilder gehen in der CI in die GitHub Container Registry, mit dem
Commit-Hash als Tag. Ein `latest` gibt es nur lokal. Was im Cluster läuft,
lässt sich damit immer auf einen Commit zurückführen.

## Das Deployment (ADR-010)

Terraform stellt in zwei Ständen die Plattform — Namespace, Pull-Secret,
ArgoCD — und meldet die Anwendung an. ArgoCD liest das Helm-Chart aus
`deploy/helm/aptum` und gleicht den Cluster damit ab, mit `selfHeal` und
`prune`: Der Cluster ist eine Ableitung des Repos. Wer deployen will, pusht.

Der CI-Job `cluster` fährt das bei jedem Push in einem `kind`-Cluster: Bilder
bauen und veröffentlichen, Terraform anwenden, auf ArgoCD warten, dann fragt
ein Pod im Cluster durch den Reverse Proxy alle drei Dienste und die
Datenbank. Ein Chart, das niemand anwendet, wäre eine Behauptung; dieses wird
bei jedem Lauf angewendet, und der Lauf bricht, wenn es nicht funktioniert.

## Was ein Betrieb von den Diensten erfährt

**Zwei Fragen, zwei Antworten.** Jeder Dienst beantwortet getrennt, ob er
lebt (`liveness`) und ob er Anfragen annehmen kann (`readiness`). Der
Scheduling-Dienst braucht beim Start einige Sekunden für Flyway — in der Zeit
ist er lebendig, aber nicht bereit. Ein Orchestrator, der das nicht
unterscheidet, startet ihn in einer Schleife neu. Eine Startprobe gibt der
JVM Zeit, bevor die Livenessprobe zählt.

**Protokoll als Struktur, mit Mandant, ohne Personenbezug.** Beide Dienste
schreiben JSON, eine Zeile je Ereignis (Spring: ECS-Format; Python: eigener
Formatter, zwanzig Zeilen). Jede Zeile einer Anfrage trägt die Mandanten-ID
als Feld — das ist Regel 5 aus `DATENSCHUTZ.md`: Der Mandant ist für die
Fehlersuche nötig, Namen und Diagnosen sind es nicht. Der AI-Dienst
protokolliert den Mandanten und die Länge des Freitexts, nie den Text; ein
Test prüft, dass der Name aus einem Testfall in keiner Zeile auftaucht.

**Health ohne Details nach außen.** `/actuator/health` sagt `UP` oder `DOWN`.
Welche Komponente warum, gehört nicht in eine unauthentifizierte Antwort.

## Was hier bewusst nicht gebaut ist

Jeder Punkt ist benannt, weil sein Fehlen sonst wie Unkenntnis aussähe.

- **Ein Cluster, der bleibt.** kind entsteht bei jedem Lauf neu und
  verschwindet danach. Es beweist das Deployment, nicht den Betrieb. Ein
  echter Cluster käme über einen dritten Terraform-Stand (EKS, AKS, GKE); die
  beiden hier blieben unverändert — dafür sind sie vom Cluster getrennt.
- **Ingress und TLS.** Das Frontend liegt auf einem NodePort, weil kind den
  nach außen reicht. Im Betrieb: ClusterIP, ein Ingress-Controller, ein
  Zertifikat von cert-manager, HTTP nur als Umleitung.
- **Authentifizierung.** `X-Mandant` ist ein Platzhalter, in
  `OFFENE-PUNKTE.md` §8 als solcher geführt. Ein Betrieb bräuchte OIDC, und
  der Mandant käme aus einem signierten Token.
- **Postgres als Dienst.** Hier ein Pod mit flüchtigem Volume. Im Betrieb
  ein verwalteter Dienst oder ein Operator — mit Backups, die man einmal
  zurückgespielt hat, sonst sind es keine.
- **Geheimnisse.** Das Pull-Secret trägt das Token des Pipeline-Laufs, die
  Datenbankpasswörter stehen in den Chart-Values. Für den Betrieb: External
  Secrets oder Sealed Secrets, und kein Passwort im Repo, auch kein
  Test-Passwort.
- **Netzwerkrichtlinien.** Heute darf jeder Pod jeden erreichen. Im Betrieb:
  Postgres nur vom Scheduling-Dienst, der AI-Dienst nur vom Frontend.
- **Metriken und Traces.** Actuator kann Prometheus-Metriken liefern; hier
  ist es nicht eingeschaltet, weil niemand sie einsammelt. Ohne Sammler ist
  ein Metriken-Endpunkt eine Behauptung. Dasselbe für Traces.
- **Mehrere Umgebungen.** Eine Application, ein Namespace. Staging und
  Produktion wären je eine Values-Datei und eine Application — oder ein
  ApplicationSet.
- **Ressourcen unter Last.** Die Requests und Limits im Chart sind Schätzungen
  für einen Beweiscluster, keine Messwerte.

## Was das für die Anforderungen heißt

C4 verlangt „solide Kenntnisse" von Kubernetes, Helm, ArgoCD und Terraform.
Dieses Repo zeigt alle vier angewendet, in der CI, bei jedem Push — nicht als
Dateien. Es zeigt nicht, einen Cluster über Monate betrieben zu haben. Das
ist eine Erfahrungsanforderung wie S5, und sie wird im Gespräch genauso
gesagt: teilweise belegbar, und die Grenze steht hier.
