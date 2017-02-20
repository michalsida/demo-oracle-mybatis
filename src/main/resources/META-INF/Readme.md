# Solves problem with starting application with oracle jdbc dependencies 

Spring configuration xml file is not compatible with XML parser definition from com.oracle.jdbc:xmlparserv2

Compare files in `src\main\resources\META-INF\services` and `xmlparserv2-12.1.0.2.jar!\META-INF\services`. 

[Solution](https://community.oracle.com/thread/1080787)

