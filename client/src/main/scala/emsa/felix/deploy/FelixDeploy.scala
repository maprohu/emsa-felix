package emsa.felix.deploy

import java.io.File

import org.apache.maven.shared.invoker.{DefaultInvocationRequest, DefaultInvoker}
import sbt.io.IO
import sbt.io.Path._

import scala.collection.JavaConversions._
import scala.xml.{Node, NodeSeq, XML}

/**
  * Created by martonpapp on 25/06/16.
  */
object FelixDeploy {

  def perform(bundle: Bundle, target: String) = {
    runMaven(
      pom(
        Poms.singleDep(bundle),
        Poms.listDeps("target/deps.txt")
      ),
      "dependency:list"
    ) { dir =>
      val deps = dir / "target" / "deps"
      deps.mkdirs()

      val execs =
        IO
          .readLines(
            dir / "target" / "deps.txt"
          )
          .map( _.trim.split(':'))
          .collect({
            case Array(group, artifact, _, version, path) =>
              val bnd = Bundle(group, artifact, version)
              val source = new File(path)
              val copied =
                deps / source.getName

              IO.copyFile(source, copied)

              <execution>
                <id>{path}</id>
                <phase>package</phase>
                <goals>
                  <goal>deploy-file</goal>
                </goals>
                <configuration>
                  <url>{target}</url>
                  <file>{copied.getAbsolutePath}</file>
                  {bnd.toXml}
                  <packaging>jar</packaging>
                  <bundleUrl>file:/boo</bundleUrl>
                </configuration>
              </execution>
          })

      runMaven(
        pom(
          <build>
            <plugins>
              <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-deploy-plugin</artifactId>
                <version>2.8.2</version>
                <configuration>
                  <uniqueVersion>false</uniqueVersion>
                </configuration>
                <executions>
                  {execs}
                </executions>
              </plugin>
              <plugin>
                <groupId>org.apache.felix</groupId>
                <artifactId>maven-bundle-plugin</artifactId>
                <version>3.0.1</version>
                <executions>
                  {execs}
                </executions>
              </plugin>
            </plugins>
          </build>
        ),
        "package"
      )(_ => ())


    }

  }

  def runMaven(pomFileString: Node, goal : String)( andThen : File => Unit ) : Unit = {
    IO.withTemporaryDirectory { dir =>
      val pomFile = dir / "pom.xml"

      XML.save(pomFile.getAbsolutePath, pomFileString)

      val request = new DefaultInvocationRequest
      request.setPomFile(pomFile)
      request.setGoals( Seq( goal ) )
      val invoker = new DefaultInvoker

      val result = invoker.execute(request)

      if (result.getExitCode == 0) {
        andThen(dir)
      }
    }
  }

  def pom(content: Node*) = {
    <project xmlns="http://maven.apache.org/POM/4.0.0"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
      <modelVersion>4.0.0</modelVersion>

      <groupId>emsa</groupId>
      <artifactId>osgi-deployer-temp</artifactId>
      <version>1.0.0-SNAPSHOT</version>
      {content}
    </project>

  }


  object Poms {


    def dep(bundle: Bundle) = {
      <dependency>
        {bundle.toXml}
      </dependency>
    }

    def singleDep(bundle: Bundle) = {
      <dependencies>
        {dep(bundle)}
      </dependencies>
    }

    def listDeps(outputFile: String) = {
      <build>
        <plugins>
          <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-dependency-plugin</artifactId>
            <version>2.10</version>
            <configuration>
              <includeScope>runtime</includeScope>
              <outputFile>{outputFile}</outputFile>
              <outputScope>false</outputScope>
              <outputAbsoluteArtifactFilename>true</outputAbsoluteArtifactFilename>
            </configuration>
          </plugin>
        </plugins>
      </build>
    }

  }


}

case class Bundle(
  group: String,
  artifact: String,
  version: String
) {
  def toXml : NodeSeq =
    <groupId>{group}</groupId>
    <artifactId>{artifact}</artifactId>
    <version>{version}</version>

}