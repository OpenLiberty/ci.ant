
## Background

This project uses [Sonatype OSSRH (OSS Repository Hosting)][ossrh] for publishing snapshots and releases to the Maven Central Repository. See the [Maven directions][ossrh-maven] on details how to setup, configure, and publish Maven artifacts.

## Configuration

### OSSRH credentials

In order to deploy snapshots and releases, you need to configure credential information for OSSRH in your `~/.m2/settings.xml` file:

```xml
<settings>
  ...
  <servers>
    <server>
      <id>sonatype-nexus-snapshots</id>
      <username>your-jira-id</username>
      <password>your-jira-pwd</password>
    </server>
    <server>
      <id>sonatype-nexus-staging</id>
      <username>your-jira-id</username>
      <password>your-jira-pwd</password>
    </server>
  </servers>
  ...
</settings>
```

### GPG credentials

In order to publish releases, you will need to configure GPG credentials as well. See the [Working with PGP Signatures][pgp] page for detailed instructions on setting up and working with PGP keys. Your GPG passphrase needs to be added to the `~/.m2/settings.xml` file. Depending on the platform, you might also need to specify the `gpg.executable` property in case your gpg command is different from `gpg`. 

```xml
<settings>
  ...
  <profiles>
    <profile>
      <id>sonatype-oss-release</id>
      <properties>
        <gpg.passphrase>your-gpg-pwd</gpg.passphrase>
      </properties>
    </profile>
  </profiles>
  ...
</settings>
```

## Publishing snapshots

Publishing snapshots is very easy. Just execute:

```bash
$ mvn clean deploy
```

In order to test your GPG credentials, you can also execute:

```bash
$ mvn clean deploy -Psonatype-oss-release
```

## Creating and publishing releases

This project is configured to use the [Maven Release Plugin][] to create and publish releases. Execute the following commands. You will be prompted for tag and version names. The default values are usually correct.

```bash
$ mvn release:clean
$ mvn release:prepare
$ mvn release:perform
```

Once the last step is successful, the artifacts are uploaded to OSSRH. Next, do the following steps:
 1. Login into the [Sonatype Nexux Professional web interface][ossrh-web]
 2. Click on `Staging Repositories` and in the search text-box enter `wasdev`. One or more repositories should show up.
 3. Select the right repository and press on the `Close` button. Once the repository is closed (it might take a while), the `Summary` tab for the repository should contain an URL for the Maven repository that contains the published release artifacts. Use that URL to test the release artifacts.

### Promoting to Maven Central

Once testing of the release artifacts using the staging repository is successful, log back into the [Sonatype Nexux Professional web interface][ossrh-web], find the right repository, and press the `Release` button to promote the artifacts to the Maven Central Repository. The artifacts should become available in a few hours.

### Dropping the release

If testing was unsuccessful, log back into the [Sonatype Nexux Professional web interface][ossrh-web], find the right repository, and press the `Drop` button to remove the staged artifacts. After resolving the issues found with the release, start the release process over again.


[ossrh]: http://central.sonatype.org/pages/ossrh-guide.html
[ossrh-maven]: http://central.sonatype.org/pages/apache-maven.html
[ossrh-web]: https://oss.sonatype.org/
[pgp]: http://central.sonatype.org/pages/working-with-pgp-signatures.html
[Maven Release Plugin]: http://maven.apache.org/components/maven-release/maven-release-plugin/
