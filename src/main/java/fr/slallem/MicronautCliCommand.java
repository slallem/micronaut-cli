package fr.slallem;

import io.micronaut.configuration.picocli.PicocliRunner;
import io.micronaut.context.ApplicationContext;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Command(name = "micronaut-cli", description = "...",
        mixinStandardHelpOptions = true)
public class MicronautCliCommand implements Runnable {

    @Option(names = {"-v", "--verbose"}, description = "...")
    boolean verbose;

    public static void main(String[] args) throws Exception {
        PicocliRunner.run(MicronautCliCommand.class, args);
    }

    public void run() {
        // business logic here
        if (verbose) {
            System.out.println("Hi!");
        }

        //-------------------------------------------------------------------------------------
        // Some simple way to run something asynchronously (no matter when it finishes)
        //-------------------------------------------------------------------------------------

        System.out.println("---- Demo Part 1 ----");
        System.out.println("BEFORE ASYNC");
        new Thread(() -> {
            doSomethingLong("A", 3);
        }).start();
        System.out.println("AFTER ASYNC CALL");

        // You can do other things while Job is still running
        sleep(5);

        //-------------------------------------------------------------------------------------
        // Async with CompletableFuture (Java 8+)
        // You can wait the end of job until some time is out and decide to do other things
        // if you consider the process too long ; the process still runs in background and
        // will notify its end with .whenComplete()
        // If you want to stop running job when timeout expires, you can use .orTimeout() instead
        //-------------------------------------------------------------------------------------

        System.out.println("---- Demo Part 2 ----");

        int jobDuration = 9; // Try with different values to see how it behaves (1s, 5s, 9s)
        int waitTime = 3;

        System.out.println("BEFORE ASYNC");

        CompletableFuture<Integer> future = CompletableFuture
                .supplyAsync(()-> doSomethingLong("B", jobDuration))
                .whenComplete((res, exc) -> doEndOfJob(res));

        System.out.println("AFTER ASYNC CALL");

        //Wait until async jobs terminates
        // (or time is out)

        Integer result = -1;

        //Primary wait (waits only if Job is still running)
        try {
            result = future.get(waitTime, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.out.println("interrupted exception");
        } catch (ExecutionException e) {
            System.out.println("exec exception");
        } catch (TimeoutException e) {
            System.out.println("timeout exception");
        }
        System.out.println("At this point result is " + result);

        //Another wait (waits only if Job is still running)
        try {
            result = future.get(waitTime, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.out.println("interrupted exception");
        } catch (ExecutionException e) {
            System.out.println("exec exception");
        } catch (TimeoutException e) {
            System.out.println("timeout exception");
        }
        System.out.println("At this point result is " + result);


        System.out.println("Waiting 10 secs before system exit... (let some time to finish running jobs)");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            System.out.printf("At the end Result is %s%n", future.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        System.out.println("THE REAL END");

    }

    public static void sleep(long seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            System.out.println("Sleep has been interrupted!");
        }
    }

    public Integer doSomethingLong(String data, int seconds) {
        Integer i = 0;
        System.out.printf("Job %s starts%n", data);
        for (int loop = 0; loop<seconds; loop++) {
            System.out.printf("Job %s still running...%n", data);
            sleep(1);
            i+=10;
        }
        System.out.printf("Job %s ends%n", data);
        return i;
    }

    public void doEndOfJob(Integer res) {
        System.out.printf("Job is complete and the result is %s%n", res);
    }

}
