#ifndef TAGANALYZE_H
#define TAGANALYZE_H

struct Relation
{
	std::string url;
	int rid;
	int views;
	std::list<std::string> tags;
};

struct Tag
{
	std::string name;
	int views;
	
	bool operator<(const Tag& that)
	{
		return views < that.views;
	}
};

std::list<std::string> gatherLogFiles(const char* dir);
void getLogRequests(const char* file, std::list<std::string>& out);
bool filterRequest(const std::string& request);
void poolUrls(std::list<std::string>& urls, std::list<Relation>& out);
void assignRids(std::list<Relation>& out);

void connectMysql();
void processData(std::list<Relation>& rels, std::list<Tag>& tags);
std::string protectQuery(std::string str);

static const char* BLOCKED_PREFIXES[] = { "/data", "/images", "/auto", "/download", "/ikony", "/Profile", "/lide", "/Edit", "/Index" };

#endif
