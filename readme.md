Usage: 


    <plugin>
        <groupId>com.nawab.maven.plugins</groupId>
        <artifactId>scheduledtasklister</artifactId>
        <version>1.0-SNAPSHOT</version>
    </plugin>


Run: `mvn scheduledtasklister:list-scheduled-tasks`

Sample Output:
    
    com.nawab.testboot.controllers.MyController,testFn,,-1,0 0 * * * ?
    com.nawab.testboot.controllers.MyController,delayFunction,,1000,
    com.nawab.testboot.controllers.MyController,asyncEndpoint,,-1,0/3 * * * * ?
