# ADR-010: GitOps — Terraform stellt die Plattform, ArgoCD rollt aus

**Status:** angenommen · **Datum:** 2026-09-12

## Kontext

Stufe 4 verlangt, dass die Anwendung nicht nur läuft, sondern ausgerollt
wird — und zwar so, dass ein Fremder nachvollziehen kann, wie. Die
Ausschreibung nennt Kubernetes, Helm, ArgoCD und Terraform namentlich.

Die Versuchung bei einem Portfolio-Projekt ist, diese vier Namen als Dateien
ins Repo zu legen: ein Chart hier, eine `main.tf` dort, ein
Application-Manifest als Beispiel. Das belegt nichts. Eine Datei, die nie
angewendet wird, kann beliebig falsch sein und sieht trotzdem richtig aus.
Dieses Repo hält an allen anderen Stellen die Regel, dass eine Behauptung
kaputtgehen können muss. Für den Betrieb heißt das: Die Pipeline muss damit
deployen.

Drei Fragen waren zu entscheiden: Wer rollt die Anwendung aus? Wo verläuft
die Grenze zwischen Terraform und ArgoCD? Und was ist der Cluster, in dem
das bewiesen wird?

## Optionen

**A. `helm install` aus der Pipeline.** Der einfachste Weg. Die CI baut,
lädt, installiert, testet. Belegt Helm und Kubernetes; belegt weder GitOps
noch Terraform noch ArgoCD. Der Cluster ist das Ergebnis eines Befehls, den
jemand getippt hat — bei jedem Lauf neu, aber ohne Quelle der Wahrheit
außer dem Lauf selbst.

**B. Terraform rollt alles aus, auch die Anwendung.** Ein Stand, `helm_release`
für ArgoCD *und* `helm_release` für Aptum. Belegt Terraform und Helm;
ArgoCD stünde daneben, ohne Aufgabe. Und es verwischt die Grenze, die im
Betrieb zählt: Terraform ist für das gemacht, was selten und bewusst
geändert wird. Eine Anwendung ändert sich mit jedem Commit.

**C. Terraform stellt die Plattform, ArgoCD rollt aus.** Terraform legt an,
was einmal stehen muss: Namespace, Pull-Secret, ArgoCD selbst, und die eine
Application, die auf das Repo zeigt. Ab dann ist Git die Quelle der
Wahrheit: ArgoCD liest das Chart aus dem Commit, den die Pipeline gerade
prüft, und gleicht den Cluster damit ab — mit `selfHeal` und `prune`, damit
der Cluster eine Ableitung des Repos ist und nicht umgekehrt. Wer deployen
will, pusht.

**Zum Cluster:** Ein echter Cloud-Cluster braucht ein Konto, kostet Geld und
läuft weiter, wenn niemand hinsieht. Ein `kind`-Cluster in der Pipeline
entsteht bei jedem Lauf neu und verschwindet danach. Er beweist das Chart,
die Terraform-Stände und die ArgoCD-Synchronisation — nicht den Betrieb.

## Entscheidung

Option C, mit kind als Beweiscluster.

Zwei Terraform-Stände statt einem, in fester Reihenfolge: `plattform/`
(Namespace, Secret, ArgoCD) und `anwendung/` (die Application). Das ist
keine Stilfrage, sondern erzwungen: Die Application ist vom Typ, den erst
ArgoCD in den Cluster bringt. Terraform plant alles vorab und kann eine
Ressource nicht planen, deren Typ es noch nicht gibt. Der Versuch, die
Application über `extraObjects` des ArgoCD-Charts mitzuliefern, scheitert
ebenso — Helm prüft alle Manifeste gegen die API, *bevor* es die CRDs
installiert. Beides wurde ausprobiert und ist im Entwicklungslog
festgehalten. Die Trennung bildet zudem ab, was im Betrieb ohnehin getrennt
ist: die Plattform, die ein Team einmal stellt, und die Anmeldung einer
Anwendung an ihr.

Die Bilder gehen in die GitHub Container Registry, mit dem Commit-Hash als
Tag. ArgoCD bekommt denselben Hash als Revision und als Bild-Tag — die
Pipeline prüft genau den Stand, den sie gebaut hat, nicht den von vorhin.

## Konsequenzen

- Die CI hat einen Job, der bei jedem Push einen Cluster hochfährt, Terraform
  in zwei Ständen anwendet, auf ArgoCD wartet und einen Pod im Cluster den
  ganzen Stapel durch den Reverse Proxy abfragen lässt. Er dauert einige
  Minuten und ist der teuerste Job der Pipeline. Das ist der Preis dafür,
  dass „wir deployen mit ArgoCD" ein Lauf ist und kein Satz.
- Schlüssel stehen in keiner Datei. Das Pull-Secret trägt das Token des
  Pipeline-Laufs, das mit ihm verfällt; lokal, mit geladenen Bildern, wird
  keins angelegt.
- Die ArgoCD-Version ist im Terraform festgenagelt. Dependabot kennt
  Helm-Charts in Terraform nicht; die Zahl hebt jemand von Hand an, und das
  steht so im Kommentar.
- Was hier *nicht* bewiesen ist: ein Cluster, der Wochen läuft; Upgrades
  unter Last; Backup und Wiederherstellung von Postgres; Netzwerkrichtlinien;
  ein Ingress mit TLS. `docs/BETRIEB.md` benennt das. Die Anforderung C4
  bleibt deshalb „teilweise belegbar" — wie S5, aus demselben Grund.

## Wann wir anders entscheiden würden

- **Mit einem Cloud-Konto.** Dann käme ein dritter Terraform-Stand vor die
  beiden hier, der den Cluster selbst anlegt (EKS, AKS, GKE) — und diese
  zwei blieben unverändert. Genau dafür sind sie vom Cluster getrennt.
- **Wenn ArgoCD im Team nicht gesetzt wäre.** Flux macht dasselbe mit
  anderem Zuschnitt. Die Trennung Plattform/Anwendung bliebe.
- **Wenn die Anwendung in mehreren Umgebungen liefe.** Dann trüge das Repo
  je Umgebung eine Values-Datei und ArgoCD je Umgebung eine Application —
  oder ein ApplicationSet. Heute gibt es eine Umgebung: den Beweiscluster.
