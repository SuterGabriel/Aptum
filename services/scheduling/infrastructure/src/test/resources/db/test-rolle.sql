-- Läuft einmal beim Start des Testcontainers, vor Flyway.
-- Die Anwendungsrolle muss existieren, bevor die Migration ihr Rechte gibt -
-- und sie darf weder Superuser noch Eigentümerin sein, sonst prüft der
-- Isolationstest nichts.
create role aptum_app login password 'aptum';
