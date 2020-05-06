package nl.knaw.huc.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ContentsTest {

  @Test
  public void toString_shouldContainHashAndAbbreviatedContents() {
    var bytes = ("Ontfangen een Missive van den Resident Mauritius , geschreven te Ontfangen een Missive van den " +
        "Nihil actum est .Hamburgh den dertienden deser loopende maandt , houdende advertentie").getBytes();
    var abbreviated = "Ontfangen een Missive van den Resident Mauritius[..]den deser loopende maandt , " +
        "houdende advertentie";
    var contents = Contents.fromBytes(bytes);
    var expected = "Contents" +
        "{" +
        "sha224=" + contents.getSha224() +
        ", contents=" + abbreviated +
        "}";

    var toTest = contents.toString();

    assertThat(toTest).isEqualTo(expected);
  }

}
