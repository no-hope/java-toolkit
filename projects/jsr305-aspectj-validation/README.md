[![Travis status](https://api.travis-ci.org/no-hope/jsr305-aspectj-validation.png)](https://travis-ci.org/no-hope/jsr305-aspectj-validation)
# jsr305-aspectj-validation

AspectJ-powered way to bring jsr305 validations on run-time.

## How to use

```java
import javax.annotation.Nonnull;

public class Example {
    /* this object should not have null value */
    private Object o;

    public Example(@Nonnull final Object obj) {
        this.o = obj;
    }

    public void setObject(@Nonnull final Object obj) {
        this.o = obj;
    }

    @Nonnull
    public void setObject(@Nonnull final Object obj) {
        this.o = obj;
    }
}
```

### Maven

To enable @Nonnull checks you need to modify your pom.xml

1) Add build plugin:

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>aspectj-maven-plugin</artifactId>
    <configuration>
        <aspectLibraries>
            <aspectLibrary>
                <groupId>org.no-hope</groupId>
                <artifactId>jsr305-aspectj-validation</artifactId>
                <version>${validation.version}</version>
            </aspectLibrary>
        </aspectLibraries>
    </configuration>
</plugin>
```
2) Add validation dependency (you can skip it adding only `com.google.code.findbugs:jsr305` and `org.aspectj:aspectjrt`):

```xml
<dependency>
    <groupId>org.no-hope</groupId>
    <artifactId>jsr305-aspectj-validation</artifactId>
    <version>${validation.version}</version>
</dependency>
```
## What is supported?

 * @Nonnull annotation:
  * Constructor parameters
  * Method parameters/return value

## TODO

 * @Nonnul support for fields
 * @ParametersAreNonnullByDefault package-level annotation
 * Other great rules
 
