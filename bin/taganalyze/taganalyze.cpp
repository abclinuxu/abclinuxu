#include <iostream>
#include <list>
#include <string>
#include <fstream>
#include <algorithm>
#include <sys/types.h>
#include <dirent.h>
#include <errno.h>
#include <mysql/mysql.h>

#include "taganalyze.h"

MYSQL* g_mysql = 0;

using namespace std;

int main(int argc, char** argv)
{
	const char* dir;
	
	if(argc != 2)
	{
		cerr << "Usage: taganalyze LOG_DIRECTORY\n";
		return 1;
	}
	
	dir = argv[1];
	
	list<string> logs;
	list<Tag> tags;
	list<Relation> relations;
	
	connectMysql();
	logs = gatherLogFiles(dir);
	
	if(logs.empty())
	{
		cerr << "No log files found\n";
		return 1;
	}
	
	for(list<string>::const_iterator it = logs.begin(); it != logs.end(); it++)
	{
		cerr << "Parsing " << *it << endl;
		list<string> requests;
		getLogRequests(it->c_str(), requests);
		poolUrls(requests, relations);
	}
	
	assignRids(relations);
	cerr << "Processing " << relations.size() << " relations\n";
	processData(relations, tags);
	
	cerr << "Dumping " << tags.size() << " tags\n";
	tags.sort();
	
	for(list<Tag>::const_reverse_iterator it=tags.rbegin(); it != tags.rend(); it++)
		cout << '"' << it->name << "\"," << it->views << endl;
	
	return 0;
}

list<string> gatherLogFiles(const char* dir)
{
	DIR* pdir = opendir(dir);
	dirent* ent;
	list<string> result;
	
	if(!pdir)
	{
		perror("opendir");
		exit(errno);
	}
	
	while(ent = readdir(pdir))
	{
		if(strstr(ent->d_name, "request.log"))
			result.push_back(string(dir) + '/' + ent->d_name);
	}
	
	closedir(pdir);
	return result;
}

void getLogRequests(const char* file, list<string>& out)
{
	ifstream fin(file);
	if(!fin.is_open())
	{
		cerr << "Cannot open " << file << endl;
		return;
	}
	
	while(!fin.eof())
	{
		char line[4096];
		fin.getline(line, sizeof line);
		
		if(!line[0])
			break;
		
		const char *s, *e;
		s = strstr(line, "GET ");
		
		if(!s)
			continue;
		
		e = strstr(s, " HTTP");
		if(!e)
			continue;
		
		if (e-s < 4)
			continue;
		
		string request = string(s+4, e-s-4);
		
		if(!filterRequest(request))
			out.push_back(request);
	}
}

bool filterRequest(const string& request)
{
	if(request == "/")
		return true;
	if(request.find("/EditDiscussion") != string::npos)
		return true;
	if(request.find('?') != string::npos)
		return true;
	
	for(size_t i=0;i<sizeof(BLOCKED_PREFIXES)/sizeof(BLOCKED_PREFIXES[0]);i++)
	{
		if(!request.compare(0, strlen(BLOCKED_PREFIXES[i]), BLOCKED_PREFIXES[i]))
			return true;
	}
	
	return false;
}

void poolUrls(std::list<std::string>& urls, std::list<Relation>& out)
{
	string lasturl;
	
	urls.sort();
	
	for(list<string>::const_iterator it = urls.begin(); it != urls.end(); it++)
	{
		if(lasturl == *it)
			out.back().views++;
		else
		{
			Relation rel;
			
			lasturl = rel.url = *it;
			rel.rid = 0;
			rel.views = 1;
			
			out.push_back(rel);
		}
	}
}

void assignRids(std::list<Relation>& out)
{
	for(list<Relation>::iterator it = out.begin(); it != out.end(); it++)
	{
		if(it->rid)
			continue;
		
		const char *s, *e, *p;
		s = it->url.c_str();
		e = p = s + it->url.size() - 1;
		
		while(isdigit(*p))
			p--;
		
		if(*p == '/' && p != e)
			it->rid = atoi(p+1);
	}
}

void connectMysql()
{
	ifstream config("config.txt");
	string values[4];
	
	for(int i=0;i<4;i++)
	{
		char line[100];
		config.getline(line, sizeof line);
		values[i] = line;
	}
	
	g_mysql = mysql_init(0);
	g_mysql = mysql_real_connect(g_mysql, values[0].c_str(), values[1].c_str(), values[2].c_str(), values[3].c_str(), 0, 0, 0);
	
	if(!g_mysql)
	{
		cerr << "Cannot connect to the MySQL database\n";
		exit(1);
	}
	
	mysql_query(g_mysql, "SET NAMES utf8");
}

void processData(std::list<Relation>& rels, std::list<Tag>& tags)
{
	unsigned long num = 0;
	for(list<Relation>::iterator it = rels.begin(); it != rels.end(); it++)
	{
		char query[1024];
		string url = protectQuery(it->url);
		MYSQL_RES* result;
		
		if(url.size() > 512)
			continue;
		
		if(!it->rid)
			sprintf(query, "select N.titulek from relace R, stitkovani S, stitek N where url = '%s' and S.cislo=R.potomek and S.typ=R.typ_potomka and N.id=S.stitek", url.c_str());
		else
			sprintf(query, "select N.titulek from relace R, stitkovani S, stitek N where cislo = %d and S.cislo=R.potomek and S.typ=R.typ_potomka and N.id=S.stitek", it->rid);
		
		if(++num % 10000 == 0)
			cerr << num << " (" << int( num/double(rels.size())*100) << "%) done\n";
		
		mysql_query(g_mysql, query);
		result = mysql_store_result(g_mysql);
		
		if(!result)
		{
			if(result)
				mysql_free_result(result);
			
			continue;
		}
		
		for(int i=0;i<mysql_num_rows(result);i++)
		{
			char** row = mysql_fetch_row(result);
			list<Tag>::iterator iti;
			
			for(iti = tags.begin(); iti != tags.end(); iti++)
			{
				if(iti->name == row[0])
					break;
			}
			
			if(iti == tags.end())
			{
				Tag tag;
				tag.name = row[0];
				tag.views = it->views;
				tags.push_back(tag);
			}
			else
				iti->views += it->views;
		}
		
		mysql_free_result(result);
	}
}

std::string protectQuery(std::string str)
{
	string::size_type pos = 0;
	while(true)
	{
		pos = str.find('\'', pos);
		if(pos == string::npos)
			break;
		
		str.replace(pos, 1, "\\'");
		pos += 2;
	}
	return str;
}
