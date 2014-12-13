package com.cj.qunit.mojo;

import static org.junit.Assert.*;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Test;

public class AbstractQunitMojoTest {

  @Test
  public void requireConfigPathIsUndefinedByDefault() {
    // when
    AbstractQunitMojo testSubject = new AbstractQunitMojo(){
      @Override
      public void execute() throws MojoExecutionException, MojoFailureException {
      }
    };
    
    // then
    assertEquals(null, testSubject.webPathToRequireDotJsConfig());
  }

}
