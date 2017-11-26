Deobfuscate client log from GWT 

GWT Client Client Log can be obfuscated when building app for production. It can be difficult if a customer send you an error in your app to understand it easily and quickly with that. 

This app allow to de-obfuscate text file of client stacktrace from the war file or the symbol map which throw this exception.

It can be done with the CLI which can take parameters as below and with a GUI interface.

```
USAGE: gwt-client-log-deobfuscator-jar-with-dependencies.jar [-h] [-l
       <locale>] [-m <symbolmap>] -o <output> -s <stacktrace> [-u
       <useragent>] [-w <war>]
 -h,--help                      Display help
 -l,--locale <locale>           Locale used when exception was thrown
 -m,--symbolmap <symbolmap>     Symbol map to deobfuscate exception
 -o,--output <output>           Output file path for deobfuscate
                                stacktrace
 -s,--stacktrace <stacktrace>   Stack trace file path
 -u,--useragent <useragent>     User agent used when exception was thrown.
                                Authorized values are : ie8,ie9,ie10,
                                gecko1_8, safari, chrome, firefox
 -w,--war <war>                 Webapp WAR file path
```

Examples : 
* From symbol map :
```sh
java -jar -s input.log -m example.symbolMap -l fr -u safari -o output.log
```

* From WAR file :
```sh
java -jar -s input.log -m example.war -l fr -u safari -o output.log
```

