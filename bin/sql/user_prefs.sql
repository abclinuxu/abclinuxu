select count(*) from uzivatel where data like '%<weekly_summary>yes%';
select count(*) from uzivatel where data like '%<newsletter>yes%';
select count(*) from uzivatel where data like '%<emoticons>yes%';
select count(*) from uzivatel where data like '%<emoticons>no%';
select count(*) from uzivatel where data like '%<last_login_date>%';

select cislo from uzivatel where data like '%<email valid="yes">%<newsletter>yes%';


