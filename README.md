qunit-mojo
==========

A maven plugin for quickly and easily creating, refactoring and running qunit tests

Setup:

~~~~~ xml

<project>
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>com.cj.example</groupId>
    <artifactId>qunit-mojo-example</artifactId>
    <version>0.0.1-SNAPSHOT</version>

    <build>
        <plugins>
            <plugin>
      	<groupId>com.cj.qunit.mojo</groupId>
		<artifactId>qunit-maven-plugin</artifactId>
		<version>2.0.0</version>

                <executions>
                    <execution>
                        <phase>test</phase>
                        <goals>
                            <goal>test</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
~~~~~
