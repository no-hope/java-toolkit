# About this lib

This library provides a set packages:

 * `org.nohope.test` - set of test utilities aimed to deal with common testing routines
 * `org.nohope.test.runner` - collection of junit-specific test runners
 * `org.nohope.test.stress` - simple performance measurement tool for multi-threaded environments which is useful for
  performing stress testing

## Utilities

 * `AkkaUtils` - uri/system configuration builders for [Akka project](http://akka.io/)
 * `TRandom` - a wrapper over standard set of random generators with extended features
 * `SocketUtils` - set of utilities for determinig local/remote address availability
 * `SerializationUtils` - assertions for java/jackson (de)serialization cycles

## Stress-tests framework (org.nohope.test.stress)

### Example

Consider we need to measure performance of code below:

```java
    public class Example {
        public int sleep1 = 10;
        public int sleep2 = 20;

        public long doSomething() throws InterruptedException {
            Thread.sleep(sleep1);
            return sleep2;
        }

        public void doSomethingElse(long sleepTime) throws InterruptedException {
            Thread.sleep(sleepTime);
        }
    }
```

For example we can measure parallel execution of both methods is 10 threads, where each thread (roughly) repeats
execution of following code 400 times on single `Example` instance:

```java
long value = example.doSomething();
example.doSomethingElse(value);
```

This can be done by following code:

```java
    final Example example = new Example();
    StressScenario scenario = StressScenario.of(TimerResolution.NANOSECONDS);
    StressResult result = scenario.measure(10, 400, new Action() {
        @Override
        protected void doAction(MeasureProvider p) throws Exception {
            final Long value = p.invoke("code-block1", new Get<Long>() {
                @Override
                public Long get() throws Exception {
                    return example.doSomething();
                }
            });
            p.invoke("code-block2", new Invoke() {
                @Override
                public void invoke() throws Exception {
                    example.doSomethingElse(value);
                }
            });
        }
    });
```

After stress scenario execution `StressResult` report will looks like following example:

    ====== Stress test result ========================
    Threads: .....................10
    Cycles: ......................400
    ==================================================
    ----- Stats for (name: code-block1) -----
    Operations:.............................4000
    Min operation time:.....................10.011 ms
    Max operation time:.....................14.950 ms
    Avg operation time:.....................10.089 ms
    Objective avg runtime:..................4.036 sec
    Total running time (10 workers):........40.357 sec
    Total time per thread:..................12.095 sec
    Running time per thread:................4.036 sec
    Avg wasted time per thread:.............8.059e+03 ms
    Avg thread throughput:..................9.911e+01 op/sec
    Avg throughput:.........................9.911e+02 op/sec
    Errors count:...........................0
    ----- Stats for (name: code-block2) -----
    Operations:.............................4000
    Min operation time:.....................20.014 ms
    Max operation time:.....................21.754 ms
    Avg operation time:.....................20.091 ms
    Objective avg runtime:..................8.036 sec
    Total running time (10 workers):........80.363 sec
    Total time per thread:..................12.100 sec
    Running time per thread:................8.036 sec
    Avg wasted time per thread:.............4.063e+03 ms
    Avg thread throughput:..................4.977e+01 op/sec
    Avg throughput:.........................4.977e+02 op/sec
    Errors count:...........................0
    ==================================================
    Total error count:............0
    Total running time:...........12.137 sec
    Approximate throughput:.......3.296e+02 op/sec
