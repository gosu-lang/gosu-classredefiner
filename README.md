This project is a small, self-registering Java agent that can be used to
programatically trigger redefinition of Java classes.  Use of this jar
requires that the JDK's tools.jar be on the classpath.  If you're building
a project that uses this jar, you'll want to add something like the following
(if you're using a Maven build):

<dependency>
  <groupId>com.sun</groupId>
  <artifactId>tools</artifactId>
  <version>1.7.0</version>
  <scope>system</scope>
  <systemPath>${java.home}/../lib/tools.jar</systemPath>
</dependency>

This project is licensed under the Apache Version 2.0 License.
Copyright 2012. Guidewire Software, Inc.