Demonstrate issue where a factory component with a configurationPid is not 
restored to active/resolved state after configuration changes.

To reproduce:

*   mvn clean install  -DskipIT 
*   mvn verify

The integration test fails because FactoryComp is not Active or Resolved after the configuration change. 