Vault is a secrets management system allowing you to store sensitive data which is encrypted at rest.
It’s ideal to store sensitive configuration details such as passwords, encryption keys, API keys.

1. Download Vault for your operating system
https://www.vaultproject.io/downloads.html
OR
https://releases.hashicorp.com/vault/0.8.3/vault_0.8.3_darwin_amd64.zip
$ unzip vault_0.8.3_darwin_amd64.zip

2. Launch a vault server process
vault server --dev --dev-root-token-id="00000000-0000-0000-0000-000000000000"
The command above starts Vault in development mode using in-memory storage without transport encryption

3. Store Secrets in Vault
Store application configuration in Vault using the Vault command line.
Set two environment variables to point the Vault CLI to the Vault endpoint and provide an authentication token.

In Linux,
$ export export VAULT_TOKEN="00000000-0000-0000-0000-000000000000"
$ export VAULT_ADDR="http://127.0.0.1:8200"

In Windows,
SET VAULT_TOKEN=00000000-0000-0000-0000-000000000000
SET VAULT_ADDR=http://127.0.0.1:8200

4. Store a configuration key-value pairs inside Vault
vault write secret/github github.oauth2.key=foobar
vault kv put secret/hello value=world
vault kv put secret/gs-vault-config example.username=hello example.password=world

vault kv put secret/gs-vault-config example.username=naga example.password=raja
vault kv put secret/gs-vault-config/cloud example.username=clouduser example.password=cloudpassword

5. vault status

6. Configure application
    a. configure your application with bootstrap.properties.
    b. Spring Cloud Vault is configured with the bootstrap context.
src/main/resources/bootstrap.properties
#Accessing Vault
spring.cloud.vault.token=00000000-0000-0000-0000-000000000000
spring.cloud.vault.scheme=http

#Vault Config
spring.application.name=gs-vault-config
spring.cloud.vault.token=00000000-0000-0000-0000-000000000000
spring.cloud.vault.scheme=http
spring.cloud.vault.kv.enabled=true

7.
#Accessing Vault
Create an Application class
@SpringBootApplication
public class Application implements CommandLineRunner {

    @Autowired
    private VaultTemplate vaultTemplate;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {

        // You usually would not print a secret to stdout
		VaultResponse response = vaultTemplate.read("secret/github");
		System.out.println("Value of github.oauth2.key");
        System.out.println("-------------------------------");
        System.out.println(response.getData().get("github.oauth2.key"));
        System.out.println("-------------------------------");
        System.out.println();

        // Let's encrypt some data using the Transit backend.
        VaultTransitOperations transitOperations = vaultTemplate.opsForTransit();

        // We need to setup transit first (assuming you didn't set up it yet).
        VaultSysOperations sysOperations = vaultTemplate.opsForSys();

        if (!sysOperations.getMounts().containsKey("transit/")) {

            sysOperations.mount("transit", VaultMount.create("transit"));

            transitOperations.createKey("foo-key");
        }

        // Encrypt a plain-text value
        String ciphertext = transitOperations.encrypt("foo-key", "Secure message");

        System.out.println("Encrypted value");
        System.out.println("-------------------------------");
        System.out.println(ciphertext);
        System.out.println("-------------------------------");
        System.out.println();

        // Decrypt

        String plaintext = transitOperations.decrypt("foo-key", ciphertext);

        System.out.println("Decrypted value");
        System.out.println("-------------------------------");
        System.out.println(plaintext);
        System.out.println("-------------------------------");
        System.out.println();
    }
}


7. Define MyConfiguration Vault Config class
package hello;

          import org.springframework.boot.context.properties.ConfigurationProperties;

          /**
           * @author Mark Paluch
           */
          @ConfigurationProperties("example")
          public class MyConfiguration {

              private String username;

              private String password;

              public String getUsername() {
                  return username;
              }

              public void setUsername(String username) {
                  this.username = username;
              }

              public String getPassword() {
                  return password;
              }

              public void setPassword(String password) {
                  this.password = password;
              }
          }

8. Create Application Class

Spring Cloud Vault uses VaultOperations to interact with Vault.
Properties from Vault get mapped to MyConfiguration for type-safe access.
@EnableConfigurationProperties(MyConfiguration.class) enables configuration property mapping and registers a MyConfiguration bean.

Application includes a main() method that autowires an instance of MyConfiguration.

package hello;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(MyConfiguration.class)
public class Application implements CommandLineRunner {

    private final MyConfiguration configuration;

    public Application(MyConfiguration configuration) {
        this.configuration = configuration;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) {

        Logger logger = LoggerFactory.getLogger(Application.class);

        logger.info("----------------------------------------");
        logger.info("Configuration properties");
        logger.info("        example.username is {}", configuration.getUsername());
        logger.info("        example.password is {}", configuration.getPassword());
        logger.info("----------------------------------------");
    }
}

Configuration properties are bound according to the activated profiles.
Spring Cloud Vault constructs a Vault context path from spring.application.name which is gs-vault and
appends the profile name (cloud) so enabling the cloud profile will fetch additionally configuration properties from secret/gs-vault-config/cloud.


In windows , (in command prompt) ,
SET VAULT_TOKEN=00000000-0000-0000-0000-000000000000
SET VAULT_ADDR=http://127.0.0.1:8200
vault kv put secret/gs-vault-config example.username=hello example.password=world
(There is change in creating key-value in Hashicorp Vault now. Use kv put instead of write.)

