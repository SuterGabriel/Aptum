-- V1: Die erste Tabelle, und mit ihr die Mandantentrennung (ADR-002).
--
-- Jede Tabelle mit Mandantendaten trägt mandant_id NOT NULL. Postgres erzwingt
-- über Row Level Security, dass eine Verbindung nur Zeilen ihres Mandanten
-- sieht. Der Mandant steht in der Sitzungsvariable app.mandant_id, gesetzt je
-- Transaktion; fehlt sie, liefert current_setting NULL, der Vergleich wird NULL,
-- und die Abfrage liefert null Zeilen. Die vergessene WHERE-Klausel fällt
-- geschlossen aus, nicht offen.
--
-- Die Rolle aptum_app ist nicht Eigentümerin der Tabellen und kein Superuser.
-- Beides würde die Policy umgehen. Die Rolle wird außerhalb der Migration
-- angelegt - im Betrieb durch die Einrichtung der Datenbank, im Test durch das
-- Init-Skript des Containers. Hier bekommt sie nur ihre Rechte.

create table verordnung (
    id                   uuid    primary key,
    mandant_id           text    not null,
    ausstellungsdatum    date    not null,
    dringlicher_bedarf   boolean not null,
    diagnosegruppe       text    not null,
    verordnete_einheiten integer not null check (verordnete_einheiten > 0),
    frequenz_min         integer not null check (frequenz_min >= 1),
    frequenz_max         integer not null check (frequenz_max >= frequenz_min)
);

create index verordnung_mandant on verordnung (mandant_id);

alter table verordnung enable row level security;
-- FORCE gilt auch für den Eigentümer. Den Superuser hält nichts auf - deshalb
-- die eigene Rolle.
alter table verordnung force row level security;

-- USING filtert, was sichtbar ist. WITH CHECK verhindert, dass eine Zeile für
-- einen anderen Mandanten geschrieben wird, als die Sitzung trägt.
create policy verordnung_mandant on verordnung
    using      (mandant_id = current_setting('app.mandant_id', true))
    with check (mandant_id = current_setting('app.mandant_id', true));

grant select, insert, update, delete on verordnung to aptum_app;
