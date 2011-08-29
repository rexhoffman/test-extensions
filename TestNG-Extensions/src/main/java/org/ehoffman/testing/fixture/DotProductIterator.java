package org.ehoffman.testing.fixture;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Given a list containing lists of options, where only one option may be selected from each list, iterator through every possible combination of the options.
 * 
 * @author rexhoffman
 *
 * @param <T>
 */
public class DotProductIterator<T> implements Iterator<Set<T>> {

  /**
   * The index in each option list, that indicates the options that will be returned on the a call to next().
   */
  private List<Integer> counters;
  
  /**
   * The size of each option list
   */
  private List<Integer> sizes;
  
  /**
   * Indicates whether or not the current values in {@link DotProductIterator#counters} represents a value 
   */
  private boolean       hasNext = false;
  
  /**
   * The list containing multiple lists with options in them.
   */
  private List<List<T>> chooseableElements;

  private List<List<T>> toLists(Collection<? extends Collection<T>> collectionOfCollections){
    List<List<T>> output = new ArrayList<List<T>>(collectionOfCollections.size());
    for(Collection<T> sublist : collectionOfCollections){
      output.add(new ArrayList<T>(sublist));
    }
    return output;
  }
  
  public DotProductIterator(Collection<? extends Collection<T>> collectionOfCollections) {
    this.chooseableElements = toLists(collectionOfCollections);
    counters = new ArrayList<Integer>(chooseableElements.size());
    sizes = new ArrayList<Integer>(chooseableElements.size());
    for (int i = 0; i < chooseableElements.size(); i++) {
      counters.add(0);
      sizes.add(chooseableElements.get(i).size());
      if (!hasNext && chooseableElements.get(i).size() > 0) {
        hasNext = true;
      }
    }
  }

  private void addOne() {
    boolean carryTheOne = true;
    for (int i = 0; i < counters.size() && carryTheOne; i++) {
      if (sizes.get(i) > 0) {
        counters.set(i, ((counters.get(i).intValue()) + 1) % sizes.get(i));
        if (counters.get(i) != 0)
          carryTheOne = false;
      }
    }
    hasNext = !carryTheOne;
  }

  public boolean hasNext() {
    return hasNext;
  }

  
  /**
   * Returns a list of options, where one was selected from each list in the list of lists used to construct this iterator.
   */
  public Set<T> next() {
    Set<T> output = new HashSet<T>(counters.size());
    for (int i = 0; i < counters.size(); i++) {
      if (sizes.get(i) > 0) {
        output.add(chooseableElements.get(i).get(counters.get(i)));
      }
    }
    addOne();
    return output;
  }

  /**
   * @throws UnsupportedOperationException
   */
  public void remove() {
    throw new UnsupportedOperationException();
  }

}