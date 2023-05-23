FROM quay.io/wildfly/wildfly-runtime-jdk11
# set an alternative directory for the mailbox
# ENV MAILBOX=/tmp/mailbox
COPY --chown=jboss:root target/server $JBOSS_HOME
RUN chmod -R ug+rwX $JBOSS_HOME