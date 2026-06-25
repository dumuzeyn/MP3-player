#!/bin/sh

APP_HOME=$(dirname "$0")
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

if [ -n "$JAVA_HOME" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD=java
fi

exec "$JAVACMD" -Xmx64m -Xms64m ${JAVA_OPTS:-} ${GRADLE_OPTS:-} -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
