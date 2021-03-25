module com.nextbreakpoint.blueprint.designscommandproducer {
    requires com.nextbreakpoint.blueprint.common.core;
    requires com.nextbreakpoint.blueprint.common.vertx;
    requires io.vertx.core;
    requires io.vertx.web;
    requires io.vertx.web.openapi;
    requires io.vertx.metrics.micrometer;
    requires io.vertx.tracing.opentracing;
    requires vertx.rx.java;
    requires rxjava;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires org.apache.logging.log4j;
    exports com.nextbreakpoint.blueprint.designs.operations.insert to com.fasterxml.jackson.databind;
    exports com.nextbreakpoint.blueprint.designs.operations.update to com.fasterxml.jackson.databind;
    exports com.nextbreakpoint.blueprint.designs.operations.delete to com.fasterxml.jackson.databind;
    opens com.nextbreakpoint.blueprint.designs.operations.insert to com.fasterxml.jackson.databind;
    opens com.nextbreakpoint.blueprint.designs.operations.update to com.fasterxml.jackson.databind;
    opens com.nextbreakpoint.blueprint.designs.operations.delete to com.fasterxml.jackson.databind;
    opens com.nextbreakpoint.blueprint.designs.model to com.fasterxml.jackson.databind;
}