package nl.knaw.huc.api;

public class Suggestion implements Comparable<Suggestion> {
  public String input;
  public int weight;

  public Suggestion(String input, int weight) {
    this.input = input;
    this.weight = weight;
  }

  @Override
  public int compareTo(Suggestion other) {
    return other.weight - this.weight;
  }
}
