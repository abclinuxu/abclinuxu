-- flag indicating that given user has voted for this comment as solution
CREATE TABLE reseni (
 zaznam INT NOT NULL,                            -- id asociovaneho zaznamu
 komentar INT NOT NULL,                          -- id komentare
 kdo INT(5) NOT NULL,                            -- cislo uzivatele
 kdy DATETIME NOT NULL                           -- cas pridani
);
ALTER TABLE reseni ADD UNIQUE INDEX reseni_kdo_komentar (kdo,komentar);
ALTER TABLE reseni ADD INDEX reseni_zaznam (zaznam);

update polozka set numeric1=0 where podtyp='question';
delete from akce where typ='solved';
delete from akce where typ='notsolved';
