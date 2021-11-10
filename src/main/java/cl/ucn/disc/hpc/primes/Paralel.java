
/*
 * Copyright (c) 2021 Karina Ayabire <karina.ayabire@hotmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *
 */
package cl.ucn.disc.hpc.primes;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class to find primes numbers
 * @author Karina Ayabire Ayabire
 */
@Slf4j
public final class Paralel {
  
  private static final AtomicInteger counter = new AtomicInteger(0);
  
  /**
   * The Main
   * @param args to use
   */
  public static void main(String[] args) throws InterruptedException {
    
    // Finding the numbers of cores
    final int maxCores = Runtime.getRuntime().availableProcessors();
    final int cores    = 8;
    final int start    = 2;
    final int end      = 5 * 1000 * 1000;
    
    // The list of times
    final List<Long> times = new ArrayList<>();
    
    for (int i = 1; i <= cores; i++) {
      // Reset the counter
      counter.set(0);
      
      // Run the calculation
      long time = findPrimes(start, end, i, times);
      
      log.info("Time with {} cores: {} ms.", i, time);
    }
    
    // Remove the min
    long min = Collections.min(times);
    log.debug("The min is: {}.", min);
    times.remove(min);
    
    // Remove the max
    long max = Collections.max(times);
    log.debug("The min is: {}.", max);
    times.remove(max);
    
    // Stream magic! https://www.baeldung.com/java-8-streams
    double average = times
            .stream()
            .mapToLong(n -> n)
            .average()
            .getAsDouble();
    log.debug("The Average is:{}", average);
    
  }
  
  /**
   * Find the primes with n cores.
   *
   * @param start number.
   * @param end   number.
   * @param cores to use.
   * @return
   * @throws InterruptedException
   */
  private static long findPrimes(long start, long end, int cores, List<Long> times) throws InterruptedException {
    
    
    log.info("Finding primes from {} to {} using {} core(s).", start, end, cores);
    
    // The Executor
    final ExecutorService executor = Executors.newFixedThreadPool(cores);
    //The Chronometer
    final StopWatch sw = StopWatch.createStarted();
    
    // From ini to end
    for (long i = start; i <= end; i++) {
      
      // The number to test
      final long n = i;
      
      // Exec the Callable
      executor.submit(() -> {
        if (isPrime(n)) {
          counter.incrementAndGet();//348513,
        }
      });
    }
    
    // Shutdown the executor
    executor.shutdown();
    
    // Wait for the termination
    if (executor.awaitTermination(5, TimeUnit.MINUTES)) {
      long time = sw.getTime(TimeUnit.MILLISECONDS);
      log.info("Founded {} primes in {} ms.", counter, time);
      times.add(time);
      return time;
    }
    
    throw new RuntimeException("The computation didn't finish.");
    
  }
  
  /**
   * Detect if a number is prime
   *
   * @param n to test
   * @return true is n is prime.
   */
  private static boolean isPrime(final long n) {
    // No prime
    if (n <= 0) {
      throw new IllegalArgumentException("Error in n: can't process negative numbers");
    }
    
    // One isn't prime
    if (n == 1) {
      return false;
    }
    
    // Two is prime
    if (n == 2) {
      return true;
    }
    
    // Any number % 2 --> false
    if (n % 2 == 0) {
      return false;
    }
    
    // Testing the primality
    
    for (long i = 3; (i * i) <= n; i += 2) {
      // n is divisible by i
      if (n % i == 0) {
        return false;
      }
    }
    return true;
  }
}
