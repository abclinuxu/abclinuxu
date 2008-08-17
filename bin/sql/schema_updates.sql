alter table uzivatel add column sync datetime null after prezdivka;
ALTER TABLE uzivatel ADD INDEX in_sync (sync);
