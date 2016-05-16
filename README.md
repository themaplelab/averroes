![Averroes logo](/logo/logo.png)

Averroes is Java bytecode generator that enables sound and precise analysis of the application part of a program without analyzing its library dependencies. It achieves that by generating a placeholder/stub library for those dependencies that models the original library code with respect to:
- class instantiations
- callbacks to application methods
- handled exceptions (caught or thrown)
- field and array accesses

## Dependencies

The code bas for Averroes is in the form of an Eclipse project. It is setup with the following dependencies:

* Project dependencies (needs to be available in your Eclipse workspace)
    - [Soot](https://github.com/Sable/soot): you need to check out the `develop` branch
    - [Probe](https://github.com/karimhamdanali/probe): you need to check out the `master` branch
* Library dependencies (ships with Averroes)
     - [bcel](https://commons.apache.org/proper/commons-bcel/)
     - [commons-cli](https://commons.apache.org/proper/commons-cli/)
     - [commons-io](https://commons.apache.org/proper/commons-io/)

## Usage

``` text
jar -jar averroes.jar -a <path> [-d <file>] [-h] -j <directory> [-l <path>] -m <class> -o <directory> -r <regex> [-t <file>]

 -a,--application-jars <path>              a list of the application JAR
                                           files separated by
                                           File.pathSeparator
 -d,--dynamic-classes-file <file>          a file that contains a list of
                                           classes that are loaded
                                           dynamically by Averroes (e.g.,
                                           classes instantiated through
                                           reflection)
 -h,--help                                 print out this help message
 -j,--java-runtime-directory <directory>   the directory that contains the
                                           Java runtime environment that
                                           Averroes should model
 -l,--library-jars <path>                  a list of the JAR files for
                                           library dependencies separated
                                           by File.pathSeparator
 -m,--main-class <class>                   the main class that runs the
                                           application when the program
                                           executes
 -o,--output-directory <directory>         the directory to which Averroes
                                           will write any output
                                           files/folders.
 -r,--application-regex <regex>            a list of regular expressions
                                           for application packages or
                                           classes separated by
                                           File.pathSeparator
 -t,--tamiflex-facts-file <file>           a file that contains reflection
                                           facts generated for this
                                           application in the TamiFlex
                                           format
```

## License

Averroes is available as Open Source under the [Eclipse Public License](https://www.eclipse.org/legal/epl-v10.html).

