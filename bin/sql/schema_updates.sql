update uzivatel set heslo=NULL;
alter table uzivatel change heslo openid VARCHAR(255) NULL;
