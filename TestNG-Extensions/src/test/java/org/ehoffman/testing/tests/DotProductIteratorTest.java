package org.ehoffman.testing.tests;

import static org.fest.assertions.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ehoffman.testing.fixture.DotProductIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

public class DotProductIteratorTest {

  Logger logger = LoggerFactory.getLogger(DotProductIterator.class);
  
  List<List<Integer>> chooseableElements = new ArrayList<List<Integer>>();
  List<Set<Integer>> expectedDotProduct;

  {
    chooseableElements.add(Arrays.asList(1, 2, 3));
    chooseableElements.add(Arrays.asList(4, 5));
    chooseableElements.add(Arrays.asList(6, 7, 8));
    expectedDotProduct = new ArrayList<Set<Integer>>();
    expectedDotProduct.add(new HashSet<Integer>(Arrays.asList(1, 4, 6)));
    expectedDotProduct.add(new HashSet<Integer>(Arrays.asList(2, 4, 6)));
    expectedDotProduct.add(new HashSet<Integer>(Arrays.asList(3, 4, 6)));
    expectedDotProduct.add(new HashSet<Integer>(Arrays.asList(1, 5, 6)));
    expectedDotProduct.add(new HashSet<Integer>(Arrays.asList(2, 5, 6)));
    expectedDotProduct.add(new HashSet<Integer>(Arrays.asList(3, 5, 6)));
    expectedDotProduct.add(new HashSet<Integer>(Arrays.asList(1, 4, 7)));
    expectedDotProduct.add(new HashSet<Integer>(Arrays.asList(2, 4, 7)));
    expectedDotProduct.add(new HashSet<Integer>(Arrays.asList(3, 4, 7)));
    expectedDotProduct.add(new HashSet<Integer>(Arrays.asList(1, 5, 7)));
    expectedDotProduct.add(new HashSet<Integer>(Arrays.asList(2, 5, 7)));
    expectedDotProduct.add(new HashSet<Integer>(Arrays.asList(3, 5, 7)));
    expectedDotProduct.add(new HashSet<Integer>(Arrays.asList(1, 4, 8)));
    expectedDotProduct.add(new HashSet<Integer>(Arrays.asList(2, 4, 8)));
    expectedDotProduct.add(new HashSet<Integer>(Arrays.asList(3, 4, 8)));
    expectedDotProduct.add(new HashSet<Integer>(Arrays.asList(1, 5, 8)));
    expectedDotProduct.add(new HashSet<Integer>(Arrays.asList(2, 5, 8)));
    expectedDotProduct.add(new HashSet<Integer>(Arrays.asList(3, 5, 8)));
  }

  @Test(groups="unit")
  public void dotProductIteratorTest() {
    DotProductIterator<Integer> iterator = new DotProductIterator<Integer>(chooseableElements);
    List<Set<Integer>> dotProducts = new ArrayList<Set<Integer>>();
    while (iterator.hasNext())
      dotProducts.add(iterator.next());
    assertThat(dotProducts).containsExactly(expectedDotProduct.toArray());
  }

  @Test(groups="unit")
  public void dotProductIteratorTestWithNulls() {
    List<List<Integer>> chooseableElements = new ArrayList<List<Integer>>();
    logger.info("Just using the log a bit, should appear in the Surefire Report");
    chooseableElements.add(Arrays.asList(1, 2, 3));
    chooseableElements.add(Arrays.asList(4, 5));
    chooseableElements.add(new ArrayList<Integer>());
    chooseableElements.add(Arrays.asList(6, 7, 8));
    chooseableElements.add(new ArrayList<Integer>());
    DotProductIterator<Integer> iterator = new DotProductIterator<Integer>(chooseableElements);
    List<Set<Integer>> dotProducts = new ArrayList<Set<Integer>>();
    while (iterator.hasNext())
      dotProducts.add(iterator.next());
    assertThat(dotProducts).containsExactly(expectedDotProduct.toArray());
  }
}
