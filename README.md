# averroes
Averroes is Java bytecode generator that enables sound and precise analysis of the application part of a program without anlayzing its library dependcies. It achieves that by generating a placeholder/stub library for those dependencies that models the original library code with respect to:
- class instantiations
- callbacks to application methods
- handled exceptions (caught or thrown)
- field and array accesses

