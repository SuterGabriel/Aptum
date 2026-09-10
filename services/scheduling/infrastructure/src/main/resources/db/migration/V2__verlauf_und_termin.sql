-- V2: Behandlungsverlauf und Termine. Und die Mandanten-ID füllt ab jetzt die
-- Datenbank selbst.
--
-- default current_setting('app.mandant_id', true): Die Anwendung schreibt die
-- Spalte nie. Sie kommt aus der Sitzungsvariable, die der Transaktionsmanager
-- setzt - und WITH CHECK prüft sie trotzdem. Kein Java-Code kennt damit die
-- Mandanten-ID einer Zeile, genau wie ADR-002 es will: Die Persistenz weiß es,
-- die Domäne nicht.

alter table verordnung
    alter column mandant_id set default current_setting('app.mandant_id', true);

create table behandlungstermin (
    id            uuid primary key,
    mandant_id    text not null default current_setting('app.mandant_id', true),
    verordnung_id uuid not null references verordnung (id) on delete cascade,
    datum         date not null,
    kennzeichen   text not null
);

create index behandlungstermin_verordnung on behandlungstermin (verordnung_id);
alter table behandlungstermin enable row level security;
alter table behandlungstermin force row level security;
create policy behandlungstermin_mandant on behandlungstermin
    using      (mandant_id = current_setting('app.mandant_id', true))
    with check (mandant_id = current_setting('app.mandant_id', true));
grant select, insert, update, delete on behandlungstermin to aptum_app;

-- Ein Termin trägt Person und Raum als Kürzel. Beides sind Stammdaten, die
-- vorerst im Code liegen; die Tabelle verweist auf sie, ohne sie zu kennen.
create table termin (
    id             uuid primary key,
    mandant_id     text not null default current_setting('app.mandant_id', true),
    therapeut      text not null,
    raum           text not null,
    heilmittel     text not null,
    behandlung_von timestamptz not null,
    behandlung_bis timestamptz not null,
    check (behandlung_bis > behandlung_von)
);

create index termin_zeitraum on termin (behandlung_von, behandlung_bis);
alter table termin enable row level security;
alter table termin force row level security;
create policy termin_mandant on termin
    using      (mandant_id = current_setting('app.mandant_id', true))
    with check (mandant_id = current_setting('app.mandant_id', true));
grant select, insert, update, delete on termin to aptum_app;
