This POM is designed for low-level environment setup such as compiler/maven settings.

### Properties

| Property              | Description                         | Default value  |
| --------------------- |:-----------------------------------:| --------------:|
| java.major.version    |                                     | 7
| java.lang.level       |                                     | 1.${java.major.version}
| java.source.level     |                                     | ${java.lang.level}
| java.target.level     |                                     | ${java.lang.level}
| java.compiler.version |                                     | ${java.lang.level}
