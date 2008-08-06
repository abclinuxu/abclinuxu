CFLAGS := -O2

taganalyze: taganalyze.o
	g++ $(CFLAGS) -lmysqlclient -otaganalyze taganalyze.o

taganalyze.o: taganalyze.cpp taganalyze.h
	g++ $(CFLAGS) -c -otaganalyze.o taganalyze.cpp

clean:
	rm -f taganalyze taganalyze.o
