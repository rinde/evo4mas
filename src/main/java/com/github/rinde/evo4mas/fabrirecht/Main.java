/**
 * 
 */
package com.github.rinde.evo4mas.fabrirecht;

import ec.Evolve;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class Main {

  public static void main(String[] args) {
    final long start = System.currentTimeMillis();
    if (args == null || args.length == 0) {
      Evolve.main(new String[] { "-file", "files/ec/fabrirechtgp.params" });
    } else {
      Evolve.main(args);
    }

    final long total = System.currentTimeMillis() - start;
    System.out.println("total running time: " + total);

  }
}
