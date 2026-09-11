-- Läuft einmal beim ersten Start des Postgres-Containers, vor Flyway.
--
-- Die Anwendungsrolle muss existieren, bevor die Migration ihr Rechte gibt.
-- Sie ist weder Superuser noch Eigentümerin der Tabellen - nur so greift Row
-- Level Security (ADR-002). Dasselbe Skript wie im Testcontainer.
create role aptum_app login password 'aptum';
