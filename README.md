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
jar -jar averroes.jar <required parameters> [optional parameters]

where required parameters include:
 -a,--application-jars <path>              A list of the application JAR
                                           files separated by path separator.
 
 -j,--java-runtime-directory <directory>   The directory that contains the
                                           Java runtime environment that
                                           Averroes should model.
                                           
 -m,--main-class <class>                   The main class that runs the
                                           application when the program
                                           executes.
                                           
 -o,--output-directory <directory>         The directory to which Averroes
                                           will write any output files/folders.
                                           
 -r,--application-regex <regex>            A list of regular expressions
                                           for application packages or classes 
                                           separated by path separator. Use 
                                           <package_name>.* to include classes 
                                           in a package, <package_name>.** to 
                                           include classes in a package and all 
                                           its subpackages, ** to include the 
                                           default package, <full_class_name> to 
                                           include a single class.

and optional parameters include:
 -d,--dynamic-classes-file <file>          A file that contains a list of
                                           classes that are loaded
                                           dynamically by Averroes (e.g.,
                                           classes instantiated through
                                           reflection).
                                           
 -h,--help                                 Prints out this help message.
 
 -l,--library-jars <path>                  A list of the JAR files for
                                           library dependencies separated
                                           by path separator.
                                           
 -t,--tamiflex-facts-file <file>           A file that contains reflection
                                           facts generated for this application 
                                           in the TamiFlex format.
```

## License

Averroes is available as Open Source under the [Eclipse Public License](https://www.eclipse.org/legal/epl-v10.html).

