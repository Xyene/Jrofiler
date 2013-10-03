Jrofiler
========

The final Java solution to large programs with performance problems.

Compilation
-----------

We use [Maven](http://maven.apache.org/download.html) to handle our dependencies.

Usage
-----

Usage of Jrofiler is very simple, and resembles that of Python's  [`profile`](http://docs.python.org/2/library/profile.html) module.

The following code demonstrates the usage of Jrofiler.

```java
import tk.ivybits.profiler.Profiler;

public class Main {
   public static void main(String[] args) throws Exception {
       Profiler.start();
       doWait();
       Profiler.stop();
   }

   public static void doWait() throws InterruptedException {
       Thread.sleep(1000);
   }
}
```

The output from the above snippet, on the call of `Profiler.stop`, will print something like the following to stdout:

```
Method                                  Time (μs)           Calls          Time (μs) / Call
-------------------------------------------------------------------------------------------
Main.doWait                             1000047             1              1000047.00
HotSpotVirtualMachine.readInt           200                 2              100.00
PipedInputStream.read                   105                 4              26.25
PipedInputStream.close                  47                  1              47.00
WindowsVirtualMachine.detach            31                  1              31.00
PipedInputStream.<init>                 21                  1              21.00
```

You can also specify the `OutputStream` to write the result to be passing an instance of the latter as an argument to `Profiler.stop`.

A very important thing to note is that due to the nature of the Java transformation API Jrofiler only profiles
classes loaded *before* the `Profiler.start` call. Furthermore, only classes loaded by the system `ClassLoader` can be profiled.
Most standard library classes do not fulfill this requirement.