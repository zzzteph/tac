# tac
Tibero authentication check. Simple tool for bruteforce and query execution for Tibero DB.

Tibero is the name of a relational databases and database management system utilities produced and marketed by TIBERO Corporation, part of South Korean owned TmaxSoft. [wiki](https://en.wikipedia.org/wiki/Tibero)

### Options ###

-  -p|--port PORT_NUMBER (default 8629)
-   -s|--sid SID 
-   -sf |--sid_file SIDFILE - file containing sids one per line
-   -upf|--user_passf  - file containing username:password one per line
-   -up |--user_pass  - username:password for auth
-   -sl |--sleep TIMEOUT default 0
-   -e  |--execute QUERY - execute query after login



### Usage samples ###

#### Bruteforce SAUTH ####
Tibero first check if username and password exist and only if user exists, checks for SID. Default SID is **TMP**. User login is case **insensitive**.



~/Desktop/tac# ./java -jar tac.jar 127.0.0.1 -upf auth
NO SIDS PROVIDED - BRUTEFORCING USERNAME:PASSWORD
ATTACKING:127.0.0.1:8629
127.0.0.1:8629:TMP:test:test
 Login failed: invalid user name or password.   
127.0.0.1:8629:TMP:SYS:SYS
 Login failed: invalid user name or password.   
127.0.0.1:8629:TMP:TIBERO:TIBERO
 The requested DB_NAME does not match with the server DB_NAME.    
**127.0.0.1:8629:TMP:TIBERO:TIBERO POSSIBLE AUTH FOUND**


If username and password was found - **POSSIBLE AUTH FOUND** will be printed.
File auth contains pair login:password one per line:

test:test
SYS:SYS
TIBERO:TIBERO

Also it is possible to use only one pair with *login:password* like:

*./java -jar tac.jar 127.0.0.1 -up test:test*

Or scanning multiply hosts 

*./java -jar tac.jar hosts -up test:test*

File **hosts** contains addresses of DB servers with source port:
127.0.0.1:8629
127.0.0.2:8629

#### Bruteforce SID and AUTH ####
After you obtained valid creds, you can bruteforce DB SID.
*./java -jar tac.jar 127.0.0.1 -sf SIDS -up test:test*

File **SIDS** contains possible DB SIDS one per line:

TEST
DB
WEB

Or you can use only one SID ( using **-s** option )
root@kali-3:~/Desktop/tac# java -jar tac.jar -s DBA 127.0.0.1 -up TIBERO:TIBERO
LOADED SIDS:1
LOADED AUTH:1
ATTACKING:127.0.0.1:8629
127.0.0.1:8629:DBA:TIBERO:TIBERO=====>PWNED






#### Execute query ####
After you got valid connection string, you can execute queries on database or list of databases

*./java -jar tac.jar 127.0.0.1 -s DBA -up TIBERO:TIBERO-e "select username from all_users"*




Example:

*./java -jar tac.jar hosts -sf SIDS -upf  AUTH -e "select username from all_users"*

This will bruteforce SIDS and auth, and if got valid pairs execute query "select username from all_users"




##How to compile

Requirements
 
 - JDK 1.6

 
 
 Compile using eclipse, add library tibero4-jdbc.jar from libs to project. 
