package com.nextbreakpoint.blueprint.common.test;

import com.jayway.restassured.config.LogConfig;
import com.jayway.restassured.config.RedirectConfig;
import com.jayway.restassured.config.RestAssuredConfig;
import com.jayway.restassured.config.SSLConfig;
import com.xebialabs.restito.server.StubServer;
import io.vertx.core.json.JsonObject;
import org.awaitility.Awaitility;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Scenario {
    private final ScenarioState scenarioState;

    private String githubUsername;
    private String githubPassword;

    private boolean buildDockerImages;

    private String serviceHost;
    private String stubHost;
    private String kafkaHost;
    private String consulHost;
    private String cassandraHost;

    private String httpPort;
    private String stubPort;
    private String kafkaPort;
    private String consulPort;
    private String cassandraPort;

    private StubServer stubServer;

    private RestAssuredConfig restAssuredConfig;

    private Resolver resolver = createResolver();

    private Scenario(ScenarioState scenarioState) {
        this.scenarioState = scenarioState;
    }

    private Resolver createResolver() {
        return Resolver.create(s -> {
            switch (s) {
                case "servicehost":
                    return getServiceHost();
                case "serviceport":
                    return getServicePort();
                case "stubhost":
                    return getStubHost();
                case "stubport":
                    return getStubPort();
                case "version":
                    return getVersion();
                case "namespace":
                    return getNamespace();
                default:
                    return null;
            }
        });
    }

    public void create() throws IOException, InterruptedException {
        if (scenarioState.buildImage) {
            githubUsername = TestUtils.getVariable("GITHUB_USERNAME");
            githubPassword = TestUtils.getVariable("GITHUB_PASSWORD");
        }

        buildDockerImages = scenarioState.buildImage;

        httpPort = TestUtils.getVariable("HTTP_PORT", System.getProperty("http.port", "8080"));
        stubPort = TestUtils.getVariable("STUB_PORT", System.getProperty("stub.port", "9000"));
        kafkaPort = TestUtils.getVariable("KAFKA_PORT", System.getProperty("kafka.port", "9093"));
        consulPort = TestUtils.getVariable("CONSUL_PORT", System.getProperty("consul.port", "8400"));
        cassandraPort = TestUtils.getVariable("CASSANDRA_PORT", System.getProperty("cassandra.port", "9042"));

        if (scenarioState.localhost) {
            serviceHost = "localhost";
            stubHost = "localhost";
            kafkaHost = "localhost";
            consulHost = "localhost";
            cassandraHost = "localhost";
        } else {
            serviceHost = KubeUtils.getMinikubeIp();
            stubHost = serviceHost.substring(0, serviceHost.lastIndexOf(".")) + ".1";
            kafkaHost = serviceHost;
            consulHost = serviceHost;
            cassandraHost = serviceHost;
        }

        printInfo();

        configureRestAssured();

        buildDockerImages();

        deleteNamespace();
        createNamespace();

        if (scenarioState.minikube) {
            if (scenarioState.mysql) {
                installMySQL();
                waitForMySQL();
            }

            if (scenarioState.cassandra) {
                installCassandra();
                waitForCassandra();
                exposeCassandra();
            }

            if (scenarioState.zookeeper) {
                installZookeeper();
                waitForZookeeper();
            }

            if (scenarioState.kafka) {
                installKafka();
                waitForKafka();
                exposeKafka();
            }

            if (scenarioState.consul) {
                installConsul();
                waitForConsul();
                exposeConsul();
            }
        }

        if (scenarioState.stubServer) {
            stubServer = new StubServer(Integer.parseInt(stubPort)).run();
        }

        createSecrets(scenarioState.serviceName, scenarioState.secretArgs);

        installService(scenarioState.serviceName, scenarioState.helmArgs);
        waitForService(scenarioState.serviceName);
        exposeService(scenarioState.serviceName);
    }

    public void destroy() throws IOException, InterruptedException {
        describeResources();

        printLogs(scenarioState.serviceName);

        uninstallService(scenarioState.serviceName);

        if (scenarioState.stubServer) {
            if (stubServer != null) {
                stubServer.clear();
            }
        }

        if (scenarioState.minikube) {
            if (scenarioState.mysql) {
                uninstallMySQL();
            }

            if (scenarioState.cassandra) {
                uninstallCassandra();
            }

            if (scenarioState.zookeeper) {
                uninstallZookeeper();
            }

            if (scenarioState.kafka) {
                uninstallKafka();
            }

            if (scenarioState.consul) {
                uninstallConsul();
            }
        }

        deleteNamespace();
    }

    public static ScenarioBuilder builder() {
        return new ScenarioBuilder();
    }

    private static class ScenarioState {
        public final boolean mysql;
        public final boolean cassandra;
        public final boolean kafka;
        public final boolean zookeeper;
        public final boolean consul;
        public final boolean localhost;
        public final boolean minikube;
        public final String helmPath;
        public final String version;
        public final String namespace;
        public final long timestamp;
        public final boolean buildImage;
        public final boolean stubServer;
        private final String serviceName;
        private final List<String> helmArgs;
        private final List<String> secretArgs;

        public ScenarioState(
                boolean mysql,
                boolean cassandra,
                boolean kafka,
                boolean zookeeper,
                boolean consul,
                boolean localhost,
                boolean minikube,
                String helmPath,
                String version,
                String namespace,
                long timestamp,
                boolean buildImage,
                boolean stubServer,
                String serviceName,
                List<String> helmArgs,
                List<String> secretArgs
        ) {
            this.mysql = mysql;
            this.cassandra = cassandra;
            this.kafka = kafka;
            this.zookeeper = zookeeper;
            this.consul = consul;
            this.localhost = localhost;
            this.minikube = minikube;
            this.helmPath = helmPath;
            this.version = version;
            this.namespace = namespace;
            this.timestamp = timestamp;
            this.buildImage = buildImage;
            this.stubServer = stubServer;
            this.serviceName = serviceName;
            this.helmArgs = helmArgs;
            this.secretArgs = secretArgs;
        }
    }

    public static class ScenarioBuilder {
        private boolean mysql;
        private boolean cassandra;
        private boolean kafka;
        private boolean zookeeper;
        private boolean consul;
        private boolean localhost;
        private boolean minikube;
        private String helmPath;
        private String version;
        private String namespace;
        private long timestamp;
        private boolean buildImage;
        private boolean stubServer;
        private String serviceName;
        private List<String> helmArgs;
        private List<String> secretArgs;

        private ScenarioBuilder() {}

        public ScenarioBuilder withMySQL() {
            mysql = true;
            return this;
        }

        public ScenarioBuilder withCassandra() {
            cassandra = true;
            return this;
        }

        public ScenarioBuilder withKafka() {
            kafka = true;
            return this;
        }

        public ScenarioBuilder withZookeeper() {
            zookeeper = true;
            return this;
        }

        public ScenarioBuilder withConsul() {
            consul = true;
            return this;
        }

        public ScenarioBuilder withLocalhost() {
            localhost = true;
            return this;
        }

        public ScenarioBuilder withMinikube() {
            minikube = true;
            return this;
        }

        public ScenarioBuilder withHelmPath(String helmPath) {
            this.helmPath = helmPath;
            return this;
        }

        public ScenarioBuilder withVersion(String version) {
            this.version = version;
            return this;
        }

        public ScenarioBuilder withNamespace(String namespace) {
            this.namespace = namespace;
            return this;
        }

        public ScenarioBuilder withTimestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public ScenarioBuilder withBuildImage(boolean buildImage) {
            this.buildImage = buildImage;
            return this;
        }

        public ScenarioBuilder withStubServer() {
            this.stubServer = true;
            return this;
        }

        public ScenarioBuilder withServiceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public ScenarioBuilder withHelmArgs(List<String> helmArgs) {
            this.helmArgs = helmArgs;
            return this;
        }

        public ScenarioBuilder withSecretArgs(List<String> secretArgs) {
            this.secretArgs = secretArgs;
            return this;
        }

        public Scenario build() {
            return new Scenario(new ScenarioState(
                    mysql,
                    cassandra,
                    kafka,
                    zookeeper,
                    consul,
                    localhost,
                    minikube,
                    helmPath,
                    version,
                    namespace,
                    timestamp,
                    buildImage,
                    stubServer,
                    serviceName,
                    helmArgs,
                    secretArgs
            ));
        }
    }

    private void printInfo() {
        System.out.println("Run test - " + new Date(scenarioState.timestamp));
        System.out.println("Namespace = " + scenarioState.namespace);
        System.out.println("Version = " + scenarioState.version);
        System.out.println("Build image = " + (buildDockerImages ? "Yes" : "No"));
    }

    private void buildDockerImages() throws IOException, InterruptedException {
        if (!buildDockerImages) {
            return;
        }
        KubeUtils.cleanDockerImages();
        System.out.println("Building image...");
        List<String> args = Arrays.asList(
                "--build-arg", "github_username=" + githubUsername,
                "--build-arg", "github_password=" + githubPassword
        );
        if (KubeUtils.buildDockerImage(".", "integration/" + scenarioState.serviceName + ":" + scenarioState.version, args) != 0) {
            throw new RuntimeException("Can't build image");
        }
        System.out.println("Image created");
        buildDockerImages = false;
    }

    private void configureRestAssured() {
        final SSLConfig sslConfig = new SSLConfig().allowAllHostnames().and().relaxedHTTPSValidation();
        final RedirectConfig redirectConfig = new RedirectConfig().followRedirects(false);
        final LogConfig logConfig = new LogConfig().enableLoggingOfRequestAndResponseIfValidationFails();
        restAssuredConfig = RestAssuredConfig.newConfig().redirect(redirectConfig).sslConfig(sslConfig).logConfig(logConfig);
    }

    private void printLogs(String name) throws IOException, InterruptedException {
        KubeUtils.printLogs(scenarioState.namespace, name);
    }

    private void describeResources() throws IOException, InterruptedException {
        KubeUtils.describePods(scenarioState.namespace);
    }

    private void createNamespace() throws IOException, InterruptedException {
        if (KubeUtils.createNamespace(scenarioState.namespace) != 0) {
            throw new RuntimeException("Can't create namespace");
        }
    }

    private void deleteNamespace() throws IOException, InterruptedException {
        if (KubeUtils.deleteNamespace(scenarioState.namespace) != 0) {
            System.out.println("Can't delete namespace");
        }
    }

    private void installMySQL() throws IOException, InterruptedException {
        System.out.println("Installing MySQL...");
        final List<String> args = Arrays.asList("--set=replicas=1");
        if (KubeUtils.installHelmChart(scenarioState.namespace, "integration-mysql", scenarioState.helmPath + "/mysql", args, true) != 0) {
            if (KubeUtils.upgradeHelmChart(scenarioState.namespace, "integration-mysql", scenarioState.helmPath + "/mysql", args, true) != 0) {
                throw new RuntimeException("Can't install or upgrade Helm chart");
            }
        }
        System.out.println("MySQL installed");
    }

    private void uninstallMySQL() throws IOException, InterruptedException {
        System.out.println("Uninstalling MySQL...");
        if (KubeUtils.uninstallHelmChart(scenarioState.namespace, "integration-mysql") != 0) {
            System.out.println("Can't uninstall Helm chart");
        }
        System.out.println("MySQL uninstalled");
    }

    private void waitForMySQL() {
        awaitUntilCondition(60, 10, 5, () -> isMySQLReady(scenarioState.namespace));
    }

    private static boolean isMySQLReady(String namespace) throws IOException, InterruptedException {
        String logs = KubeUtils.fetchLogs(namespace, "mysql");
        String[] lines = logs.split("\n");
        boolean databaseReady = Arrays.stream(lines).anyMatch(line -> line.contains("/usr/local/bin/docker-entrypoint.sh: running /docker-entrypoint-initdb.d/init.sql"));
        boolean serverReady = Arrays.stream(lines).anyMatch(line -> line.contains("/usr/sbin/mysqld: ready for connections.") && line.contains("socket: '/var/run/mysqld/mysqld.sock'  port: 3306"));
        return serverReady && databaseReady;
    }

    private void installZookeeper() throws IOException, InterruptedException {
        System.out.println("Installing Zookeeper...");
        final List<String> args = Arrays.asList("--set=replicas=1");
        if (KubeUtils.installHelmChart(scenarioState.namespace, "integration-zookeeper", scenarioState.helmPath + "/zookeeper", args, true) != 0) {
            if (KubeUtils.upgradeHelmChart(scenarioState.namespace, "integration-zookeeper", scenarioState.helmPath + "/zookeeper", args, true) != 0) {
                throw new RuntimeException("Can't install or upgrade Helm chart");
            }
        }
        System.out.println("Zookeeper installed");
    }

    private void uninstallZookeeper() throws IOException, InterruptedException {
        System.out.println("Uninstalling Zookeeper...");
        if (KubeUtils.uninstallHelmChart(scenarioState.namespace, "integration-zookeeper") != 0) {
            System.out.println("Can't uninstall Helm chart");
        }
        System.out.println("Zookeeper uninstalled");
    }

    private void waitForZookeeper() {
        awaitUntilCondition(60, 10, 5, () -> isZookeeperReady(scenarioState.namespace));
    }

    private static boolean isZookeeperReady(String namespace) throws IOException, InterruptedException {
        String logs = KubeUtils.fetchLogs(namespace, "zookeeper");
        String[] lines = logs.split("\n");
        boolean serverReady = Arrays.stream(lines).anyMatch(line -> line.contains("binding to port /0.0.0.0:2181"));
        return serverReady;
    }

    private void installKafka() throws IOException, InterruptedException {
        System.out.println("Installing Kafka...");
        final List<String> args = Arrays.asList("--set=replicas=1,externalName=" + kafkaHost + ",externalPort=" + kafkaPort);
        if (KubeUtils.installHelmChart(scenarioState.namespace, "integration-kafka", scenarioState.helmPath + "/kafka", args, true) != 0) {
            if (KubeUtils.upgradeHelmChart(scenarioState.namespace, "integration-kafka", scenarioState.helmPath + "/kafka", args, true) != 0) {
                throw new RuntimeException("Can't install or upgrade Helm chart");
            }
        }
        System.out.println("Kafka installed");
    }

    private void uninstallKafka() throws IOException, InterruptedException {
        System.out.println("Uninstalling Kafka...");
        if (KubeUtils.uninstallHelmChart(scenarioState.namespace, "integration-kafka") != 0) {
            System.out.println("Can't uninstall Helm chart");
        }
        System.out.println("Kafka uninstalled");
    }

    private void waitForKafka() {
        awaitUntilCondition(60, 10, 5, () -> isKafkaReady(scenarioState.namespace));
    }

    private static boolean isKafkaReady(String namespace) throws IOException, InterruptedException {
        String logs = KubeUtils.fetchLogs(namespace, "kafka");
        String[] lines = logs.split("\n");
        boolean serverReady = Arrays.stream(lines).anyMatch(line -> line.contains("[KafkaServer id=1] started (kafka.server.KafkaServer)"));
        return serverReady;
    }

    private void exposeKafka() throws IOException, InterruptedException {
        System.out.println("Exposing Kafka...");
        if (KubeUtils.exposeService(scenarioState.namespace, "kafka", Integer.parseInt(kafkaPort), 9093) != 0) {
            throw new RuntimeException("Can't expose Kafka");
        }
        System.out.println("Kafka exposed");
    }

    private void installCassandra() throws IOException, InterruptedException {
        System.out.println("Installing Cassandra...");
        final List<String> args = Arrays.asList("--set=replicas=1");
        if (KubeUtils.installHelmChart(scenarioState.namespace, "integration-cassandra", scenarioState.helmPath + "/cassandra", args, true) != 0) {
            if (KubeUtils.upgradeHelmChart(scenarioState.namespace, "integration-cassandra", scenarioState.helmPath + "/cassandra", args, true) != 0) {
                throw new RuntimeException("Can't install or upgrade Helm chart");
            }
        }
        System.out.println("Cassandra installed");
    }

    private void uninstallCassandra() throws IOException, InterruptedException {
        System.out.println("Uninstalling Cassandra...");
        if (KubeUtils.uninstallHelmChart(scenarioState.namespace, "integration-cassandra") != 0) {
            System.out.println("Can't uninstall Helm chart");
        }
        System.out.println("Cassandra uninstalled");
    }

    private void waitForCassandra() {
        awaitUntilCondition(90, 30, 10, () -> isCassandraReady(scenarioState.namespace));
    }

    private static boolean isCassandraReady(String namespace) throws IOException, InterruptedException {
        String logs = KubeUtils.fetchLogs(namespace, "cassandra");
        String[] lines = logs.split("\n");
        boolean serverReady = Arrays.stream(lines).anyMatch(line -> line.contains("Truncate of designs.designs is complete"));
        return serverReady;
    }

    private void exposeCassandra() throws IOException, InterruptedException {
        System.out.println("Exposing Cassandra...");
        if (KubeUtils.exposeService(scenarioState.namespace,"cassandra", Integer.parseInt(cassandraPort), 9042) != 0) {
            throw new RuntimeException("Can't expose Cassandra");
        }
        System.out.println("Cassandra exposed");
    }

    private void installConsul() throws IOException, InterruptedException {
        System.out.println("Installing Consul...");
        final List<String> args = Arrays.asList("--set=replicas=1,serviceName=" + serviceHost + ",servicePort=" + httpPort);
        if (KubeUtils.installHelmChart(scenarioState.namespace, "integration-consul", scenarioState.helmPath + "/consul", args, true) != 0) {
            if (KubeUtils.upgradeHelmChart(scenarioState.namespace, "integration-consul", scenarioState.helmPath + "/consul", args, true) != 0) {
                throw new RuntimeException("Can't install or upgrade Helm chart");
            }
        }
        System.out.println("Consul installed");
    }

    private void uninstallConsul() throws IOException, InterruptedException {
        System.out.println("Uninstalling Consul...");
        if (KubeUtils.uninstallHelmChart(scenarioState.namespace, "integration-consul") != 0) {
            System.out.println("Can't uninstall Helm chart");
        }
        System.out.println("Consul uninstalled");
    }

    private void waitForConsul() {
        awaitUntilCondition(60, 10, 5, () -> isConsulReady(scenarioState.namespace));
    }

    private static boolean isConsulReady(String namespace) throws IOException, InterruptedException {
        String logs = KubeUtils.fetchLogs(namespace, "consul");
        String[] lines = logs.split("\n");
        boolean serverReady = Arrays.stream(lines).anyMatch(line -> line.contains("Synced service: service=designs-sse"));
        return serverReady;
    }

    private void exposeConsul() throws IOException, InterruptedException {
        System.out.println("Exposing Consul...");
        if (KubeUtils.exposeService(scenarioState.namespace,"consul", Integer.parseInt(consulPort), 8400) != 0) {
            throw new RuntimeException("Can't expose Consul");
        }
        System.out.println("Consul exposed");
    }

    private void installService(String name, List<String> helmArgs) throws IOException, InterruptedException {
        System.out.println("Installing service...");
        final List<String> args = helmArgs.stream().map(arg -> resolver.resolve(arg)).collect(Collectors.toList());
        if (KubeUtils.installHelmChart(scenarioState.namespace, "integration-" + name, "helm", args, true) != 0) {
            if (KubeUtils.upgradeHelmChart(scenarioState.namespace, "integration-" + name, "helm", args, true) != 0) {
                throw new RuntimeException("Can't install or upgrade Helm chart");
            }
        }
        System.out.println("Service installed");
    }

    private void uninstallService(String name) throws IOException, InterruptedException {
        System.out.println("Uninstalling service...");
        if (KubeUtils.uninstallHelmChart(scenarioState.namespace, "integration-" + name) != 0) {
            System.out.println("Can't uninstall Helm chart");
        }
        System.out.println("Service uninstalled");
    }

    private void waitForService(String name) {
        awaitUntilCondition(20, 5, 1, () -> isServiceReady(scenarioState.namespace, name));
    }

    private static boolean isServiceReady(String namespace, String name) throws IOException, InterruptedException {
        String logs = KubeUtils.fetchLogs(namespace, name);
        String[] lines = logs.split("\n");
        boolean serviceReady = Arrays.stream(lines).anyMatch(line -> line.contains("Service listening on port 8080"));
        return serviceReady;
    }

    private void exposeService(String name) throws IOException, InterruptedException {
        System.out.println("Exposing service...");
        if (KubeUtils.exposeService(scenarioState.namespace, name, Integer.parseInt(httpPort), 8080) != 0) {
            throw new RuntimeException("Can't expose service");
        }
        System.out.println("Service exposed");
    }

    private void createSecrets(String name, List<String> secretArgs) throws IOException, InterruptedException {
        System.out.println("Creating secrets...");
        final List<String> args = secretArgs.stream().map(arg -> resolver.resolve(arg)).collect(Collectors.toList());
        if (KubeUtils.createSecret(scenarioState.namespace, name, args, true) != 0) {
            throw new RuntimeException("Can't create secret");
        }
        System.out.println("Secrets created");
    }

    public static void awaitUntilCondition(int timeout, int delay, int interval, Callable<Boolean> condition) {
        Awaitility.await()
                .atMost(timeout, TimeUnit.SECONDS)
                .pollDelay(delay, TimeUnit.SECONDS)
                .pollInterval(interval, TimeUnit.SECONDS)
                .until(condition);
    }

    public JsonObject createCassandraConfig() {
        final JsonObject config = new JsonObject();
        config.put("cassandra_contactPoints", cassandraHost);
        config.put("cassandra_port", cassandraPort);
        config.put("cassandra_cluster", "cassandra");
        config.put("cassandra_username", "admin");
        config.put("cassandra_password", "password");
        return config;
    }

    public JsonObject createProducerConfig() {
        final JsonObject config = new JsonObject();
        config.put("kafka_bootstrap_servers", kafkaHost + ":" + kafkaPort);
        config.put("kafka_client_id", "integration");
        return config;
    }

    public JsonObject createConsumerConfig(String group) {
        final JsonObject config = new JsonObject();
        config.put("kafka_bootstrap_servers", kafkaHost + ":" + kafkaPort);
        config.put("kafka_group_id", group);
        return config;
    }

    public RestAssuredConfig getRestAssuredConfig() {
        return restAssuredConfig;
    }

    public StubServer getStubServer() {
        return stubServer;
    }

    public String getServiceHost() {
        return serviceHost;
    }

    public String getStubHost() {
        return stubHost;
    }

    public String getKafkaHost() {
        return kafkaHost;
    }

    public String getConsulHost() {
        return consulHost;
    }

    public String getCassandraHost() {
        return cassandraHost;
    }

    public String getServicePort() {
        return httpPort;
    }

    public String getStubPort() {
        return stubPort;
    }

    public String getKafkaPort() {
        return kafkaPort;
    }

    public String getConsulPort() {
        return consulPort;
    }

    public String getCassandraPort() {
        return cassandraPort;
    }

    public String getVersion() {
        return scenarioState.version;
    }

    public String getNamespace() {
        return scenarioState.namespace;
    }

    public long getTimestamp() {
        return scenarioState.timestamp;
    }

    public static class Resolver {
        private static final String PATTERN = "\\$\\{([a-zA-Z]+[a-zA-Z0-9_]*)}";
        private final Pattern pattern = Pattern.compile(PATTERN);
        private final Function<String, String> getProperty;

        public static Resolver create(Function<String, String> getProperty) {
            return new Resolver(getProperty);
        }

        private Resolver(Function<String, String> getProperty) {
            this.getProperty = getProperty;
        }

        public String resolve(String input) {
            if (input == null) {
                return null;
            }
            final Matcher matcher = pattern.matcher(input);
            final StringBuilder sb = new StringBuilder();
            while (matcher.find()) {
                String replacement = evaluate(matcher.group(1));
                if (replacement != null) {
                    matcher.appendReplacement(sb, replacement);
                }
            }
            matcher.appendTail(sb);
            return sb.toString();
        }

        private String evaluate(String expression) {
            return getProperty.apply(expression.toLowerCase());
        }
    }
}