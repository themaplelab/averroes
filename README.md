![Averroes logo](/logo/logo.tiff?raw=true)

# averroes
Averroes is Java bytecode generator that enables sound and precise analysis of the application part of a program without analyzing its library dependencies. It achieves that by generating a placeholder/stub library for those dependencies that models the original library code with respect to:
- class instantiations
- callbacks to application methods
- handled exceptions (caught or thrown)
- field and array accesses

## License

Averroes is available as Open Source under the [Eclipse Public License](https://www.eclipse.org/legal/epl-v10.html).

