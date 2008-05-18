mysql abc -e "select U.cislo, U.jmeno, V.hodnota from uzivatel U, vlastnost V where V.typ_predka='U' and V.predek=U.cislo and V.typ='score'" > score_2007-06-26.csv

